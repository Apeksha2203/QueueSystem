// VIVA GUIDE: Explicit presentation fixture loader. --dry-run rolls back; --apply backs up matching demo rows then commits.
// Credentials are read by DBConnection from an ignored local properties file, never stored in this script.
import com.queue.util.DBConnection;
import java.sql.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

class LoadPresentationData {
    static String json(String value) {
        return "\""+value.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r")+"\"";
    }
    // Snapshot demo queue records and their dependent metadata before any replacement.
    static void backup(Connection c) throws Exception {
        String filter="s.student_name LIKE '[DEMO] %' AND (s.email LIKE 'demo-queue-%@example.invalid' OR s.email LIKE 'presentation-demo-%@example.invalid')";
        Map<String,String> queries=new LinkedHashMap<>();
        queries.put("queue", "SELECT q.* FROM queue q JOIN students s ON s.student_id=q.student_id WHERE "+filter);
        queries.put("reservations", "SELECT r.* FROM queue_reservations r JOIN queue q ON q.queue_id=r.queue_id JOIN students s ON s.student_id=q.student_id WHERE "+filter);
        queries.put("events", "SELECT e.* FROM queue_events e JOIN queue q ON q.queue_id=e.queue_id JOIN students s ON s.student_id=q.student_id WHERE "+filter);
        queries.put("bookings", "SELECT b.* FROM bookings b JOIN students s ON s.student_id=b.student_id WHERE "+filter);
        List<String> tables=new ArrayList<>();
        for(var entry:queries.entrySet()) {
            List<String> rows=new ArrayList<>();
            try(Statement statement=c.createStatement();ResultSet result=statement.executeQuery(entry.getValue())) {
                var metadata=result.getMetaData();
                while(result.next()) {
                    List<String> fields=new ArrayList<>();
                    for(int i=1;i<=metadata.getColumnCount();i++) {
                        Object value=result.getObject(i);
                        fields.add(json(metadata.getColumnLabel(i))+":"+(value==null?"null":value instanceof Number?value.toString():json(value.toString())));
                    }
                    rows.add("{"+String.join(",",fields)+"}");
                }
            }
            tables.add(json(entry.getKey())+":["+String.join(",",rows)+"]");
        }
        Path path=Path.of(".runtime","deployment","demo-backup-"+System.currentTimeMillis()+".json");
        Files.createDirectories(path.getParent());
        Files.writeString(path,"{"+String.join(",",tables)+"}");
        System.out.println("Backup: "+path.toAbsolutePath());
    }
    public static void main(String[] args) throws Exception {
        boolean apply=args.length==1&&args[0].equals("--apply");
        if(!apply&&!(args.length==1&&args[0].equals("--dry-run"))) throw new IllegalArgumentException("Use --dry-run or --apply explicitly.");
        try(Connection c=DBConnection.getConnection()) {
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(DISTINCT service_name) FROM services WHERE service_name IN ('General Inquiries','Fee Payment','Document Verification')")) {
                r.next();if(r.getInt(1)!=3)throw new IllegalStateException("All three campus services must exist.");
            }
            try(Statement s=c.createStatement()){s.execute("SET time_zone='+05:30'");}
            c.setAutoCommit(false);
            try {
                // Lock service parents before the snapshot and replacement, just like normal reservations.
                try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT service_id FROM services ORDER BY service_id FOR UPDATE")){while(r.next()) {}}
                if(apply)backup(c);
                String sql=Files.readString(Path.of("database","presentation_demo.sql")).replaceAll("(?m)^--.*$", "");
                for(String part:sql.split(";")) {
                    String command=part.trim();
                    if(command.isEmpty()||command.equals("START TRANSACTION")||command.equals("COMMIT"))continue;
                    try(Statement s=c.createStatement()) {
                        if(s.execute(command))try(ResultSet r=s.getResultSet()) {
                            if(command.startsWith("SELECT sv."))while(r.next())System.out.println(r.getString(1)+" | "+r.getString(2)+" | "+r.getString(3)+" | "+r.getInt(4)+" entries | avg "+r.getString(5)+" min");
                        }
                    }
                }
                // Verify completed fixture durations agree with timestamps and waiting tickets have no completion.
                try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM queue q JOIN students st ON st.student_id=q.student_id WHERE st.email LIKE 'presentation-demo-%@example.invalid' AND ((q.status='COMPLETED' AND (q.processing_seconds NOT IN (60,180,240) OR q.processing_seconds<>TIMESTAMPDIFF(SECOND,q.started_at,q.completed_at))) OR (q.status='WAITING' AND q.completed_at IS NOT NULL))")) {
                    r.next();if(r.getInt(1)!=0)throw new IllegalStateException("Invalid demo processing sample.");
                }
                if(apply){c.commit();System.out.println("Committed presentation data.");}
                else{c.rollback();System.out.println("Dry run passed; all changes rolled back.");}
            }catch(Exception error){c.rollback();throw error;}
        }
    }
}

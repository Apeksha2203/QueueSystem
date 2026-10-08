// VIVA GUIDE: Explicit demonstration fixture seeder for older booking flows; running a fixture tool changes data and is not normal request handling.
import com.queue.util.*;
import com.queue.service.ServiceHours;
import java.sql.*;
import java.time.*;
import java.util.*;

/** Local load-test fixtures: intentionally bypass capacity admission to exercise long queues. */
class SeedDemoBookings {
 // Operation insert: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
 static int insert(Connection c,String sql,Object...args)throws SQLException {
  try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
   for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);p.executeUpdate();
   try(ResultSet r=p.getGeneratedKeys()){return r.next()?r.getInt(1):0;}
  }
 }
 // Operation number: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
 static int number(Connection c,String sql,Object...args)throws SQLException {
  try(PreparedStatement p=c.prepareStatement(sql)) {
   for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);
   try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):0;}
  }
 }
 // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
 public static void main(String[] args)throws Exception {
  LocalDate day=ServiceHours.now().toLocalDate();
  String passwordHash=Passwords.hash(UUID.randomUUID().toString());
  try(Connection c=DBConnection.getConnection()) {
   // Group related writes; commit saves them together and rollback prevents partial updates.
   c.setAutoCommit(false);
   try {
    for(String serviceName:List.of("General Inquiries","Fee Payment","Document Verification")) {
     int service=number(c,"SELECT service_id FROM services WHERE service_name=?",serviceName);
     if(service==0||!TransactionLocks.service(c,service))throw new IllegalStateException("Service missing: "+serviceName);
     int added=0;
     for(int i=1;i<=25;i++) {
      String email="demo-queue-"+day+"-"+service+"-"+i+"@example.invalid";
      int student=number(c,"SELECT student_id FROM students WHERE email=?",email);
      if(student==0)student=insert(c,"INSERT INTO students(student_name,email,password) VALUES (?,?,?)","[DEMO] "+serviceName+" Student "+String.format("%02d",i),email,passwordHash);
      if(number(c,"SELECT COUNT(*) FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE q.student_id=? AND q.service_id=? AND r.visit_date=?",student,service,day)>0)continue;
      int token=number(c,"SELECT COALESCE(MAX(token_number),0)+1 FROM queue WHERE service_id=?",service);
      int order=number(c,"SELECT COALESCE(MAX(r.queue_order),0)+1 FROM queue_reservations r JOIN queue q ON q.queue_id=r.queue_id WHERE q.service_id=? AND r.visit_date=?",service,day);
      int queue=insert(c,"INSERT INTO queue(student_id,service_id,token_number,status) VALUES (?,?,?,'WAITING')",student,service,token);
      insert(c,"INSERT INTO queue_reservations(queue_id,visit_date,queue_order) VALUES (?,?,?)",queue,day,order);
      insert(c,"INSERT INTO queue_events(queue_id,event_type) VALUES (?,'DEMO_RESERVED')",queue);added++;
     }
     System.out.println(serviceName+": added "+added+" demo reservations for "+day);
    }
    c.commit();
   }catch(Exception e){c.rollback();throw e;}
  }
 }
}

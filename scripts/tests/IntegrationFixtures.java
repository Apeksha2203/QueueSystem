// VIVA GUIDE: Explicit regression/fixture tool: inspect assertions and cleanup; database checks use their configured database. Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
import java.sql.*;
import com.queue.util.DBConnection;

class IntegrationFixtures {
  // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
  public static void main(String[] args) throws Exception {
    try (Connection c = DBConnection.getConnection()) {
      if (args[0].equals("create")) {
        int service;
        try (PreparedStatement p = c.prepareStatement("INSERT INTO services(service_name,description,average_service_time) VALUES (?, 'Temporary integration fixture',7)", Statement.RETURN_GENERATED_KEYS)) {
          p.setString(1,args[1]);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();service=r.getInt(1);}
        }
        int counter;
        try(PreparedStatement p=c.prepareStatement("INSERT INTO counters(counter_name,service_id,is_active) VALUES ('Integration desk',?,TRUE)",Statement.RETURN_GENERATED_KEYS)) {
          p.setInt(1,service);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();counter=r.getInt(1);}
        }
        try(PreparedStatement p=c.prepareStatement("INSERT INTO staff(staff_name,email,password,service_id,counter_id) VALUES ('Integration staff',?,?,?,?)")) {
          p.setString(1,args[2]);p.setString(2,System.getenv("TEST_STAFF_PASSWORD"));p.setInt(3,service);p.setInt(4,counter);p.executeUpdate();
        }
        System.out.println(service);
      } else if(args[0].equals("cleanup")) {
        int service=Integer.parseInt(args[1]);
        try(PreparedStatement check=c.prepareStatement("SELECT description FROM services WHERE service_id=?")) {
          check.setInt(1,service);try(ResultSet r=check.executeQuery()){if(!r.next()||!r.getString(1).equals("Temporary integration fixture"))throw new IllegalArgumentException("Not a test fixture");}
        }
        // Group related writes; commit saves them together and rollback prevents partial updates.
        c.setAutoCommit(false);
        try {
          for(String table:new String[]{"bookings","queue","staff","counters","services"}) {
            try(PreparedStatement p=c.prepareStatement("DELETE FROM "+table+" WHERE service_id=?")){p.setInt(1,service);p.executeUpdate();}
          }
          for(int i=2;i<args.length;i++)try(PreparedStatement p=c.prepareStatement("DELETE FROM students WHERE email=?")){p.setString(1,args[i]);p.executeUpdate();}
          c.commit();
        } catch(Exception error) {c.rollback();throw error;}
        System.out.println("Temporary test records removed.");
      }
    }
  }
}

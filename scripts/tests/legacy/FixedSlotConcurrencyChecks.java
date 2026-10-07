import com.queue.dao.QueueDAO;
import com.queue.model.Queue;
import com.queue.model.Booking;
import com.queue.service.BookingService;
import com.queue.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

/** Real MySQL contention checks; creates and removes only its own fixture rows. */
class ConcurrencyChecks {
    static final List<Integer> students=new ArrayList<>(), services=new ArrayList<>();
    static int checks;
    static void check(boolean condition,String label) {
        if(!condition) throw new AssertionError(label);
        System.out.println("PASS "+label);checks++;
    }
    static int insert(String sql,Object...values) throws Exception {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getInt(1);}
        }
    }
    static <T> List<T> race(List<Callable<T>> jobs) throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(jobs.size());
        CountDownLatch start=new CountDownLatch(1);
        try {
            List<Future<T>> pending=new ArrayList<>();
            for(Callable<T> job:jobs)pending.add(pool.submit(()->{start.await();return job.call();}));
            start.countDown();List<T> results=new ArrayList<>();
            for(Future<T> f:pending)results.add(f.get(45,TimeUnit.SECONDS));return results;
        } finally { pool.shutdownNow();pool.awaitTermination(10,TimeUnit.SECONDS); }
    }
    static Queue ticket(int student,int service) {Queue q=new Queue();q.setStudentId(student);q.setServiceId(service);return q;}
    static Booking booking(int student,int service,String time) {Booking b=new Booking();b.setStudentId(student);b.setServiceId(service);b.setBookingDate(java.sql.Date.valueOf(LocalDate.now().plusDays(2)));b.setBookingTime(Time.valueOf(time));return b;}
    public static void main(String[] args) throws Exception {
        String run=UUID.randomUUID().toString();
        try {
            for(int i=0;i<2;i++)services.add(insert("INSERT INTO services(service_name,description,average_service_time) VALUES (?,'Concurrency test fixture',5)","Concurrency "+run+" "+i));
            for(int i=0;i<8;i++)students.add(insert("INSERT INTO students(student_name,email,password) VALUES ('Concurrency fixture',?,'unused')",run+"-"+i+"@test.invalid"));
            int service=services.get(0);List<Callable<Queue>> joins=new ArrayList<>();
            for(int student:students)joins.add(()->{Queue q=ticket(student,service);if(!new QueueDAO().joinQueue(q))throw new AssertionError("join failed");return q;});
            List<Queue> joined=race(joins);
            check(joined.stream().map(Queue::getTokenNumber).distinct().count()==8,"parallel joins allocate distinct tokens");
            List<Callable<Boolean>> duplicates=new ArrayList<>();for(int i=0;i<8;i++)duplicates.add(()->new QueueDAO().joinQueue(ticket(students.get(0),service)));
            check(race(duplicates).stream().noneMatch(Boolean::booleanValue),"parallel duplicate joins cannot add another active ticket");
            List<Callable<Queue>> calls=new ArrayList<>();for(int i=0;i<8;i++)calls.add(()->new QueueDAO().callNext(service));
            List<Queue> called=race(calls);
            check(called.stream().filter(Objects::nonNull).map(Queue::getQueueId).distinct().count()==8,"parallel next calls select distinct students");
            check(new QueueDAO().callNext(service)==null,"exhausted waiting queue cannot call a student twice");
            List<Callable<String>> bookings=new ArrayList<>();for(int student:students)bookings.add(()->new BookingService().createBooking(booking(student,service,"09:00:00")));
            check(race(bookings).stream().filter("SUCCESS"::equals).count()==1,"parallel slot claims accept exactly one booking");
            List<Callable<String>> crossService=new ArrayList<>();for(int s:services)crossService.add(()->new BookingService().createBooking(booking(students.get(0),s,"09:30:00")));
            check(race(crossService).stream().filter("SUCCESS"::equals).count()==1,"parallel cross-service bookings reject a student time conflict");
            check(!"SUCCESS".equals(new BookingService().createBooking(booking(students.get(0),service,"09:15:00"))),"core booking service rejects unlisted slots");
            System.out.println(checks+" concurrency checks passed.");
        } finally {
            try(Connection c=DBConnection.getConnection()) {
                c.setAutoCommit(false);
                try {
                    for(int service:services)for(String table:List.of("queue","bookings","services"))try(PreparedStatement p=c.prepareStatement("DELETE FROM "+table+" WHERE service_id=?")){p.setInt(1,service);p.executeUpdate();}
                    for(int student:students)try(PreparedStatement p=c.prepareStatement("DELETE FROM students WHERE student_id=?")){p.setInt(1,student);p.executeUpdate();}
                    c.commit();System.out.println("Own concurrency fixtures removed.");
                } catch(Exception error){c.rollback();throw error;}
            }
        }
    }
}

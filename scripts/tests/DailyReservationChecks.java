// VIVA GUIDE: Explicit regression/fixture tool: inspect assertions and cleanup; database checks use their configured database. Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
import com.queue.service.*;
import com.queue.util.DBConnection;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

class DailyReservationChecks {
    static int checks,service,counter,otherCounter,staff,otherStaff;
    static final List<Integer> students=new ArrayList<>();
    static final LocalDate day=ServiceHours.now().toLocalDate().plusDays(2);
    // Operation check: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    static void check(boolean condition,String label){if(!condition)throw new AssertionError(label);System.out.println("PASS "+label);checks++;}
    // Operation at: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    static LocalDateTime at(int h,int m){return day.atTime(h,m);}
    // Operation insert: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    static int insert(String sql,Object...values)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getInt(1);}}}
    // Operation n: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    static int n(Map<String,Object> map,String key){return ((Number)map.get(key)).intValue();}
    // Operation fails: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    static void fails(Callable<?> work,String label)throws Exception{try{work.call();throw new AssertionError(label);}catch(IllegalArgumentException expected){check(true,label);}}
    // Operation sql: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    static void sql(String sql,Object...values)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);p.executeUpdate();}}
    // Perform call; inspect its conditions, affected rows and return value before describing success.
    static Map<String,Object> call(ReservationService r,int id)throws Exception{return r.operate(id,"call-next",0);}
    // Operation finish: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    static void finish(ReservationService r,int operator,int id)throws Exception{r.operate(operator,"start",id);r.operate(operator,"complete",id);}
    // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
    public static void main(String[] args)throws Exception {
        check(!ServiceHours.open(at(9,59))&&ServiceHours.open(at(10,0)),"opening boundary is 10 AM");
        check(!ServiceHours.open(at(13,0))&&ServiceHours.open(at(14,0)),"break and reopening boundaries");
        check(!ServiceHours.open(at(15,0)),"closing boundary is exclusive");
        check(ServiceHours.estimate(at(7,30),0).equals(at(10,0)),"first prebooking estimates 10 AM");
        check(ServiceHours.estimate(at(12,55),10).equals(at(14,5)),"estimated service skips lunch");
        check(ServiceHours.estimate(at(13,0),0).equals(at(14,0)),"lunch reservation starts at 2 PM");
        check(ServiceHours.estimate(at(14,55),5)==null,"service at closing is not promised");
        String run=UUID.randomUUID().toString();
        try {
            service=insert("INSERT INTO services(service_name,description,average_service_time) VALUES (?,'Daily reservation test fixture',5)","Daily "+run);
            counter=insert("INSERT INTO counters(counter_name,service_id,is_active) VALUES ('Daily test desk',?,TRUE)",service);
            otherCounter=insert("INSERT INTO counters(counter_name,service_id,is_active) VALUES ('Other test desk',?,FALSE)",service);
            staff=insert("INSERT INTO staff(staff_name,email,password,service_id,counter_id) VALUES ('Daily test',?,'unused',?,?)",run+"@staff.invalid",service,counter);
            otherStaff=insert("INSERT INTO staff(staff_name,email,password,service_id,counter_id) VALUES ('Other test',?,'unused',?,?)",run+"-other@staff.invalid",service,otherCounter);
            for(int i=0;i<15;i++)students.add(insert("INSERT INTO students(student_name,email,password) VALUES ('Daily test',?,'unused')",run+"-"+i+"@student.invalid"));
            ReservationService early=new ReservationService(at(7,30)),open=new ReservationService(at(10,0));
            Map<String,Object> preview=early.preview(students.get(0),service);
            check(n(preview,"position")==1&&"10:00".equals(preview.get("estimate")),"preview before opening has first position and opening estimate");
            List<Integer> tickets=new ArrayList<>();for(int i=0;i<12;i++)tickets.add(n(early.reserve(students.get(i),service),"queueId"));
            check(n(early.preview(students.get(11),service),"position")==12,"reservations retain confirmation order");
            fails(()->early.reserve(students.get(0),service),"duplicate daily reservation rejected");
            fails(()->call(early,staff),"counter cannot call before opening");
            fails(()->new ReservationService(at(13,30)).operate(staff,"call-next",0),"counter cannot call during lunch");
            fails(()->new ReservationService(at(15,0)).reserve(students.get(14),service),"reservations close at 3 PM");
            fails(()->new ReservationService(at(14,59)).reserve(students.get(14),service),"queue exceeding working hours rejected");
            int first=n(call(open,staff),"queueId");check(first==tickets.get(0),"first confirmed reservation is called first");
            fails(()->call(open,staff),"counter cannot own two current students");
            fails(()->open.operate(otherStaff,"start",first),"another operator cannot start this ticket");
            Map<String,Object> moved=open.operate(staff,"no-show",first);
            check(n(moved,"missedTurns")==1&&!Boolean.TRUE.equals(moved.get("removed")),"first miss returns student to waiting queue");
            check(n(open.preview(students.get(0),service),"position")==11,"first miss moves behind exactly ten people");
            int next=n((Map<String,Object>)moved.get("next"),"queueId");check(next==tickets.get(1),"next waiting student called in same transaction");
            fails(()->open.operate(staff,"no-show",first),"repeated stale no-show request does not penalise twice");
            finish(open,staff,next);
            for(int i=2;i<=10;i++){int id=n(open.staffQueue(staff).stream().filter(row->"CALLED".equals(row.get("status"))).findFirst().orElseThrow(),"queueId");check(id==tickets.get(i),"present student "+i+" retains order");finish(open,staff,id);}
            check(n(open.staffQueue(staff).stream().filter(row->"CALLED".equals(row.get("status"))).findFirst().orElseThrow(),"queueId")==first,"moved student receives a second turn");
            Map<String,Object> removed=open.operate(staff,"no-show",first);
            check(Boolean.TRUE.equals(removed.get("removed")),"second miss ends ticket");
            check(open.preview(students.get(0),service).get("queueId")==null,"removed ticket leaves active queue");
            check(n((Map<String,Object>)removed.get("next"),"queueId")==tickets.get(11),"second miss calls next present student");
            check(early.reserve(students.get(0),service).get("queueId")!=null,"removed student may reserve a new ticket");
            fails(()->open.cancel(students.get(0),tickets.get(11))?null:throwConflict(),"student cannot cancel another student's ticket");
            open.availability(staff,"PAUSED");finish(open,staff,tickets.get(11));open.availability(staff,"AVAILABLE");
            // Clear the remaining reservation before testing a concurrent duplicate claim.
            for(Map<String,Object> row:open.staffQueue(staff))open.cancel(n(row,"studentId"),n(row,"queueId"));
            ExecutorService pool=Executors.newFixedThreadPool(8);CountDownLatch gate=new CountDownLatch(1);
            try {
                List<Future<Boolean>> jobs=new ArrayList<>();
                for(int i=0;i<8;i++)jobs.add(pool.submit(()->{gate.await();try{early.reserve(students.get(13),service);return true;}catch(IllegalArgumentException conflict){return false;}}));
                gate.countDown();int accepted=0;for(Future<Boolean> job:jobs)if(job.get(30,TimeUnit.SECONDS))accepted++;
                check(accepted==1,"simultaneous duplicate reservations accept one ticket");
            }finally{pool.shutdownNow();pool.awaitTermination(10,TimeUnit.SECONDS);}
            open.availability(staff,"PAUSED");check("PAUSED".equals(open.availability(counter)),"pause survives outside browser session");
            check(open.preview(students.get(13),service).get("estimate")==null,"paused counter produces unknown service time");
            open.availability(staff,"AVAILABLE");check(n(call(open,staff),"queueId")>0,"resumed counter can call");
            int current=n(open.staffQueue(staff).stream().filter(row->"CALLED".equals(row.get("status"))).findFirst().orElseThrow(),"queueId");
            fails(()->open.availability(staff,"CHECKED_OUT"),"checkout blocked while a called student is assigned");
            new ReservationService(at(15,1)).operate(staff,"start",current);new ReservationService(at(15,2)).operate(staff,"complete",current);
            check(true,"existing service can finish after closing");
            int shortFirst=n(early.reserve(students.get(1),service),"queueId"),shortSecond=n(early.reserve(students.get(2),service),"queueId");
            check(n(call(open,staff),"queueId")==shortFirst,"short queue calls first reservation");
            Map<String,Object> shortMiss=open.operate(staff,"no-show",shortFirst);
            check(n(open.preview(students.get(1),service),"position")==2,"fewer than ten students moves absence to end");
            finish(open,staff,shortSecond);check(n(open.staffQueue(staff).stream().filter(row->"CALLED".equals(row.get("status"))).findFirst().orElseThrow(),"queueId")==shortFirst,"completion automatically advances to next student");
            check(open.operate(staff,"no-show",shortFirst).get("next")==null,"empty waiting queue does not fabricate another call");
            int lone=n(early.reserve(students.get(3),service),"queueId");call(open,staff);
            check(open.operate(staff,"no-show",lone).get("next")==null,"first miss with nobody behind does not immediately recall absence");
            check(open.cancel(students.get(3),lone),"own moved waiting reservation can be cancelled");
            early.reserve(students.get(4),service);early.reserve(students.get(5),service);
            open.availability(otherStaff,"AVAILABLE");
            ExecutorService two=Executors.newFixedThreadPool(2);CountDownLatch gateTwo=new CountDownLatch(1);
            try {
                Future<Map<String,Object>> a=two.submit(()->{gateTwo.await();return call(open,staff);});
                Future<Map<String,Object>> b=two.submit(()->{gateTwo.await();return call(open,otherStaff);});gateTwo.countDown();
                Map<String,Object> ca=a.get(30,TimeUnit.SECONDS),cb=b.get(30,TimeUnit.SECONDS);
                check(n(ca,"queueId")!=n(cb,"queueId"),"simultaneous counters receive distinct students");
                check(n(ca,"counterId")==counter&&n(cb,"counterId")==otherCounter,"simultaneous calls retain counter ownership");
                finish(open,staff,n(ca,"queueId"));finish(open,otherStaff,n(cb,"queueId"));
            }finally{two.shutdownNow();two.awaitTermination(10,TimeUnit.SECONDS);}
            open.availability(otherStaff,"CHECKED_OUT");
            List<Integer> lateTickets=new ArrayList<>();for(int i=0;i<7;i++)lateTickets.add(n(early.reserve(students.get(i),service),"queueId"));
            ReservationService late=new ReservationService(at(14,45)),closed=new ReservationService(at(15,0));
            Map<String,Object> risk=late.preview(students.get(6),service);
            check(Boolean.TRUE.equals(risk.get("closingRisk"))&&"15:15".equals(risk.get("projectedServiceTime")),"30 minute wait with 15 minutes left warns about closing");
            check(!Boolean.TRUE.equals(late.preview(students.get(7),service).get("canReserve")),"new reservation beyond closing rejected");
            check(late.closeWaiting(service)==0,"estimate warning does not end tickets before closing");
            check(ServiceHours.project(at(12,55),30).equals(at(14,25)),"lunch delay projects same day after reopening");
            check(Boolean.TRUE.equals(late.preview(students.get(3),service).get("closingRisk")),"estimate exactly at closing also warns");
            int called=n(call(late,staff),"queueId");open.availability(otherStaff,"AVAILABLE");
            int serving=n(call(late,otherStaff),"queueId");late.operate(otherStaff,"start",serving);
            sql("UPDATE queue_reservations SET missed_turns=1 WHERE queue_id=?",lateTickets.get(6));
            check(closed.closeWaiting(service)==5,"closing rolls only the five waiting tickets forward");
            check(closed.closeWaiting(service)==0,"closing reconciliation is idempotent");
            Map<String,Object> ended=closed.visits(students.get(6)).stream().filter(row->n(row,"id")==lateTickets.get(6)).findFirst().orElseThrow();
            check("WAITING".equals(ended.get("status"))&&ended.get("date").toString().equals(day.plusDays(1).toString())&&n(ended,"missedTurns")==1,"rollover preserves missed count and assigns tomorrow");
            check(closed.staffQueue(staff).size()==2,"called and serving tickets survive closing");
            finish(closed,staff,called);closed.operate(otherStaff,"complete",serving);
            check(closed.staffQueue(staff).isEmpty(),"both assigned students can finish after closing");
            check(new ReservationService(day.plusDays(1).atTime(7,30)).preview(students.get(2),service).get("queueId")!=null,"unserved tickets remain active next day");
            ReservationService midnight=new ReservationService(day.plusDays(1).atStartOfDay());
            Map<String,Object> midnightBooking=midnight.reserve(students.get(7),service);
            check(n(midnightBooking,"position")==6 && Boolean.TRUE.equals(midnightBooking.get("canReserve")),"midnight prebooking appends behind five carried tickets");
            // Simulate missed overnight reconciliation: reserve itself must catch up before assigning position.
            sql("UPDATE queue_reservations SET visit_date=? WHERE queue_id=?",day,lateTickets.get(2));
            Map<String,Object> afterRestart=midnight.reserve(students.get(8),service);
            check(n(afterRestart,"position")==7,"midnight reservation catches overdue tickets before allocating new order");
            sql("DELETE FROM queue WHERE service_id=? AND status='WAITING'",service);
            int morningTicket=n(early.reserve(students.get(0),service),"queueId");early.reserve(students.get(1),service);
            ReservationService morning=new ReservationService(at(12,55)),afternoon=new ReservationService(at(14,0));call(morning,staff);morning.operate(staff,"start",morningTicket);
            check(afternoon.operate(staff,"complete",morningTicket).get("next")==null,"service spanning lunch requires manual afternoon summon");
            check(n(call(afternoon,staff),"queueId")>0,"afternoon session starts with explicit summon");
            System.out.println(checks+" daily reservation checks passed.");
        } finally {
            try(Connection c=DBConnection.getConnection()){c.setAutoCommit(false);try{
                if(service>0)for(String table:List.of("queue","staff","counters","services"))try(PreparedStatement p=c.prepareStatement("DELETE FROM "+table+" WHERE service_id=?")){p.setInt(1,service);p.executeUpdate();}
                for(int student:students)try(PreparedStatement p=c.prepareStatement("DELETE FROM students WHERE student_id=?")){p.setInt(1,student);p.executeUpdate();}c.commit();System.out.println("Own daily reservation fixtures removed.");
            }catch(Exception error){c.rollback();throw error;}}
        }
    }
    // Operation throwConflict: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    static Object throwConflict(){throw new IllegalArgumentException("Not owned");}
}

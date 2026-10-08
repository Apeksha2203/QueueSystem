// VIVA GUIDE: Core transaction coordinator. Study preview, reserve, operate, callNext, cancellation, availability and closing rollover. Follow the service lock and commit/rollback boundary.
package com.queue.service;

import com.queue.util.*;
import com.queue.model.Queue;
import java.sql.*;
import java.time.*;
import java.util.*;

/** Service-day FIFO with stable tokens, transactional counter ownership and missed turns. */
public class ReservationService {
    private final LocalDateTime fixedNow;
    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public ReservationService(){fixedNow=null;}
    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public ReservationService(LocalDateTime at){fixedNow=at;}
    // Uses an injected clock for deterministic tests, otherwise the campus clock; never mixes the browser clock into authorization.
    private LocalDateTime now(){return fixedNow==null?ServiceHours.now():fixedNow;}
    private static final String ACTIVE="('WAITING','CALLED','SERVING')";
    // Binds SQL arguments in order, executes a SELECT and maps each row by column label; try-with-resources closes statement/result set.
    private List<Map<String,Object>> rows(Connection c,String sql,Object...args)throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sql)) {
            for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);
            try(ResultSet r=p.executeQuery()) {
                List<Map<String,Object>> list=new ArrayList<>();ResultSetMetaData m=r.getMetaData();
                while(r.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=m.getColumnCount();i++)row.put(m.getColumnLabel(i),r.getObject(i));list.add(row);}return list;
            }
        }
    }
    // Binds SQL arguments and executes a write on the supplied connection; the caller owns commit/rollback.
    private int update(Connection c,String sql,Object...args)throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);return p.executeUpdate();}
    }
    // Converts a numeric database-map value to int, treating a missing/null value as zero.
    private int number(Map<String,Object> row,String key){return row.get(key)==null?0:((Number)row.get(key)).intValue();}
    // Writes an audit event for this ticket; student/system actions use a null staff ID.
    private void event(Connection c,int queue,int staff,String type)throws SQLException {update(c,"INSERT INTO queue_events(queue_id,staff_id,event_type) VALUES (?,?,?)",queue,staff==0?null:staff,type);}
    // Builds the service-day position and ETA view. CALLED/SERVING tickets precede WAITING tickets; carried tickets retain their destination day.
    public Map<String,Object> preview(int student,int service)throws SQLException {
        closeWaiting(service);
        try(Connection c=DBConnection.getConnection()){return preview(c,student,service);}
    }
    // Builds the service-day position and ETA view. CALLED/SERVING tickets precede WAITING tickets; carried tickets retain their destination day.
    private Map<String,Object> preview(Connection c,int student,int service)throws SQLException {
        LocalDateTime at=now();LocalDate day=at.toLocalDate();
        LocalDateTime requestedAt=at;
        List<Map<String,Object>> future=rows(c,"SELECT r.visit_date FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE q.student_id=? AND q.service_id=? AND q.status='WAITING' AND r.visit_date>? ORDER BY r.visit_date LIMIT 1",student,service,day);
        boolean carried=!future.isEmpty();
        if(carried){day=((java.sql.Date)future.get(0).get("visit_date")).toLocalDate();at=day.atTime(10,0);}
        List<Map<String,Object>> config=rows(c,"SELECT service_name FROM services WHERE service_id=?",service);
        if(config.isEmpty())throw new IllegalArgumentException("Unknown service.");
        List<Map<String,Object>> active=rows(c,"SELECT q.queue_id,q.student_id,q.status,r.queue_order FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE q.service_id=? AND r.visit_date=? AND q.status IN "+ACTIVE+" ORDER BY r.queue_order,q.queue_id",service,day);
        int ahead=active.size();Integer existing=null;String status="WAITING";
        active.sort(Comparator.comparingInt(row -> "WAITING".equals(row.get("status"))?1:0));
        for(int i=0;i<active.size();i++)if(number(active.get(i),"student_id")==student){ahead=i;existing=number(active.get(i),"queue_id");status=(String)active.get(i).get("status");break;}
        int available=number(rows(c,"SELECT COUNT(*) n FROM counters WHERE service_id=? AND is_active=TRUE",service).get(0),"n");
        int planned=number(rows(c,"SELECT COUNT(*) n FROM counters WHERE service_id=?",service).get(0),"n");
        boolean open=ServiceHours.open(at), closed=ServiceHours.nextOpen(at)==null;
        // Use currently active counters during service; use scheduled counters for pre-opening/carryover projections.
        int capacity=open&&!carried?available:planned;
        LocalDateTime projected=capacity>0?ServiceHours.project(at,(long)Math.ceil(ahead*new WaitingTimeService().getAverageServiceTime(service)/capacity)):null;
        // An estimate at or after 3 PM is a closing risk; preserve the projection for a clear student warning.
        boolean closingRisk="WAITING".equals(status)&&projected!=null&&!projected.isBefore(day.atTime(15,0));
        LocalDateTime estimate=closingRisk?null:projected;
        if(Set.of("CALLED","SERVING").contains(status))estimate=at;
        Map<String,Object> result=new LinkedHashMap<>();result.put("position",ahead+1);result.put("ahead",ahead);result.put("queueId",existing);result.put("serviceName",config.get(0).get("service_name"));
        result.put("testClock",fixedNow==null && Math.abs(Duration.between(LocalDateTime.now(ServiceHours.ZONE),at).toSeconds())>2);result.put("campusTime",at.toLocalTime().withSecond(0).withNano(0).toString());result.put("date",day.toString());result.put("hours","10:00–13:00 / 14:00–15:00");result.put("open",open);result.put("closed",closed);result.put("activeCounters",available);
        result.put("estimate",estimate==null?null:estimate.toLocalTime().withSecond(0).withNano(0).toString());
        result.put("wait",estimate==null?null:Math.max(0,Duration.between(carried?requestedAt:at,estimate).toMinutes()));
        result.put("rescheduled",carried);
        result.put("closingRisk",closingRisk);result.put("projectedServiceTime",projected==null?null:projected.toLocalTime().withSecond(0).withNano(0).toString());
        result.put("minutesToClose",Math.max(0,Duration.between(at,day.atTime(15,0)).toMinutes()));
        result.put("warning",carried?"Your ticket has moved to "+day+". Counters open at 10 AM; your queue order is preserved.":closingRisk?"You're unlikely to be served today. Your pending ticket will move to the next day's 10 AM queue if unserved at closing.":null);
        result.put("canReserve",!closed && existing==null && (estimate!=null || (open && available==0)));
        result.put("message",existing!=null?"You already have a place in this queue.":closed?"Reservations are closed for today.":open&&available==0?"The counter has not started or is paused. Your place can be reserved; the service time is unavailable.":estimate==null?"The queue is expected to reach closing time. No further places are available.":open?"Live estimate; your confirmed position may change before you submit.":"Prebooking is open. Service starts at the next opening; estimates assume scheduled counters are staffed.");
        if(closingRisk&&existing==null)result.put("message","You're unlikely to be served today. Your estimated turn falls at or after the 3 PM closing time. Please reserve again tomorrow.");
        if(existing!=null) {
            Map<String,Object> assignment=rows(c,"SELECT r.missed_turns,c.counter_name,r.counter_id FROM queue_reservations r LEFT JOIN counters c ON c.counter_id=r.counter_id WHERE r.queue_id=?",existing).get(0);
            result.put("missedTurns",assignment.get("missed_turns"));result.put("counterId",assignment.get("counter_id"));result.put("counterName",assignment.get("counter_name"));
        }
        return result;
    }
    // Locks the service row, rechecks availability, allocates token/order and inserts ticket, reservation and audit event in one transaction.
    public Map<String,Object> reserve(int student,int service)throws SQLException {
        // Catch up after midnight or a sleeping/restarted server before assigning today's new position.
        // Carryover tickets are moved ahead first; a new booking appends after their queue_order values.
        if (now().toLocalTime().isBefore(LocalTime.of(15,0))) closeWaiting(service);
        try(Connection c=DBConnection.getConnection()) {
            // Use READ_COMMITTED and explicit transactions so service-row locks protect a consistent validation/write sequence.
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);
            try {
                // Serialize this service's operations before allocating order or changing ticket state.
                if(!TransactionLocks.service(c,service))throw new IllegalArgumentException("Unknown service.");
                Map<String,Object> preview=preview(c,student,service);
                if(!Boolean.TRUE.equals(preview.get("canReserve")))throw new IllegalArgumentException((String)preview.get("message"));
                LocalDate day=now().toLocalDate();
                // Allocate a stable service token while holding the service lock; token identity is separate from mutable daily queue order.
                int token=number(rows(c,"SELECT COALESCE(MAX(token_number),0)+1 n FROM queue WHERE service_id=?",service).get(0),"n");
                // Append to this service day's queue order; missed calls/rollover may later change order without changing the token.
                int order=number(rows(c,"SELECT COALESCE(MAX(r.queue_order),0)+1 n FROM queue_reservations r JOIN queue q ON q.queue_id=r.queue_id WHERE q.service_id=? AND r.visit_date=?",service,day).get(0),"n");
                int id;try(PreparedStatement p=c.prepareStatement("INSERT INTO queue(student_id,service_id,token_number,status) VALUES (?,?,?,'WAITING')",Statement.RETURN_GENERATED_KEYS)){p.setInt(1,student);p.setInt(2,service);p.setInt(3,token);p.executeUpdate();try(ResultSet keys=p.getGeneratedKeys()){keys.next();id=keys.getInt(1);}}
                update(c,"INSERT INTO queue_reservations(queue_id,visit_date,queue_order) VALUES (?,?,?)",id,day,order);event(c,id,0,"RESERVED");
                preview.put("queueId",id);preview.put("token",String.format("%03d",token));c.commit();return preview;
            }catch(Exception error){c.rollback();throw error;}
        }
    }
    // Reconciles pending carryovers, then returns only this student's reservations for history/upcoming display.
    public List<Map<String,Object>> visits(int student)throws SQLException {
        closeWaitingForStudent(student);
        try(Connection c=DBConnection.getConnection()) {
            return rows(c,"SELECT q.queue_id AS id,q.service_id AS serviceId,r.visit_date AS date,q.token_number AS token,q.status,r.missed_turns AS missedTurns FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE q.student_id=? ORDER BY r.visit_date DESC,q.queue_id DESC",student);
        }
    }
    // Looks up the staff member's service and returns that service's active tickets for the campus day, including assignment/contact fields.
    public List<Map<String,Object>> staffQueue(int staff)throws SQLException {
        try(Connection c=DBConnection.getConnection()){List<Map<String,Object>> assignment=rows(c,"SELECT service_id FROM staff WHERE staff_id=?",staff);if(!assignment.isEmpty())closeWaiting(number(assignment.get(0),"service_id"));}
        try(Connection c=DBConnection.getConnection()) {
            return rows(c,"SELECT q.queue_id AS queueId,q.student_id AS studentId,student.student_name AS studentName,student.email AS studentEmail,student.phone AS studentPhone,q.service_id AS serviceId,q.token_number AS tokenNumber,q.status,r.counter_id AS counterId,r.staff_id AS assignedStaffId,r.missed_turns AS missedTurns FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id JOIN students student ON student.student_id=q.student_id JOIN staff s ON s.service_id=q.service_id WHERE s.staff_id=? AND r.visit_date=? AND q.status IN "+ACTIVE+" ORDER BY CASE WHEN q.status='WAITING' THEN 1 ELSE 0 END,r.queue_order,q.queue_id",staff,now().toLocalDate());
        }
    }
    // Enforces staff service/counter ownership and valid ticket transitions under locks. First miss moves back up to ten places; second miss ends the ticket.
    public Map<String,Object> operate(int staff,String action,int queue)throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            // Use READ_COMMITTED and explicit transactions so service-row locks protect a consistent validation/write sequence.
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);
            try {
                List<Map<String,Object>> profiles=rows(c,"SELECT * FROM staff WHERE staff_id=?",staff);
                if(profiles.isEmpty())throw new IllegalArgumentException("Staff account not found.");
                int service=number(profiles.get(0),"service_id"), counter=number(profiles.get(0),"counter_id");
                // Serialize this service's operations before allocating order or changing ticket state.
                if(!TransactionLocks.service(c,service))throw new IllegalArgumentException("Service not found.");
                List<Map<String,Object>> counters=rows(c,"SELECT * FROM counters WHERE counter_id=? AND service_id=? FOR UPDATE",counter,service);
                if(counters.isEmpty())throw new IllegalArgumentException("Your counter assignment is invalid.");
                if(action.equals("call")||action.equals("call-next")) {
                    if(!ServiceHours.open(now()))throw new IllegalArgumentException("Service hours are 10 AM–1 PM and 2 PM–3 PM.");
                    if(!Boolean.TRUE.equals(counters.get(0).get("is_active")))throw new IllegalArgumentException("Make your counter available first.");
                }
                LocalDate day=now().toLocalDate();
                List<Map<String,Object>> current=rows(c,"SELECT q.queue_id FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE r.counter_id=? AND r.visit_date=? AND q.status IN ('CALLED','SERVING')",counter,day);
                if(action.equals("call-next") || action.equals("call")) {
                    if(!current.isEmpty())throw new IllegalArgumentException("Finish your current student before calling another.");
                    Map<String,Object> called=callNext(c,service,counter,staff,day,action.equals("call")?queue:0);c.commit();return called;
                }
                List<Map<String,Object>> records=rows(c,"SELECT q.*,r.missed_turns,r.queue_order FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE q.queue_id=? AND q.service_id=? AND r.counter_id=? AND r.staff_id=? AND r.visit_date=? FOR UPDATE",queue,service,counter,staff,day);
                if(records.isEmpty())throw new IllegalArgumentException("That ticket is not assigned to you today.");
                String status=(String)records.get(0).get("status");
                if(action.equals("no-show")) {
                    if(!status.equals("CALLED"))throw new IllegalArgumentException("Only a called student can miss a turn.");
                    int misses=number(records.get(0),"missed_turns")+1;
                    // Second missed call ends the ticket with NO_SHOW instead of moving it again.
                    if(misses>=2) {update(c,"UPDATE queue SET status='NO_SHOW',completed_at=CURRENT_TIMESTAMP WHERE queue_id=?",queue);update(c,"UPDATE queue_reservations SET missed_turns=? WHERE queue_id=?",misses,queue);event(c,queue,staff,"REMOVED_SECOND_MISS");}
                    else {
                        List<Map<String,Object>> waiting=rows(c,"SELECT q.queue_id FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE q.service_id=? AND r.visit_date=? AND q.status='WAITING' ORDER BY r.queue_order,q.queue_id",service,day);
                        // First missed call moves behind at most ten waiting students; never index beyond the current list.
                        int insertion=Math.min(10,waiting.size());waiting.add(insertion,Map.of("queue_id",queue));
                        // Re-index all waiting entries; called/serving entries are ahead independently.
                        int order=1;for(Map<String,Object> row:waiting)update(c,"UPDATE queue_reservations SET queue_order=? WHERE queue_id=?",order++,number(row,"queue_id"));
                        update(c,"UPDATE queue SET status='WAITING',called_at=NULL WHERE queue_id=?",queue);
                        update(c,"UPDATE queue_reservations SET missed_turns=?,counter_id=NULL,staff_id=NULL WHERE queue_id=?",misses,queue);event(c,queue,staff,"MOVED_BACK_FIRST_MISS");
                    }
                    // Do not immediately recall the absent student if nobody else is waiting.
                    Map<String,Object> next=ServiceHours.open(now())&&sameOperatingWindow(c,queue)&&Boolean.TRUE.equals(counters.get(0).get("is_active"))?callNext(c,service,counter,staff,day,0,queue):null;
                    Map<String,Object> result=new LinkedHashMap<>();result.put("missedTurns",misses);result.put("removed",misses>=2);result.put("next",next);c.commit();return result;
                }
                String target=action.equals("start")&&status.equals("CALLED")?"SERVING":action.equals("complete")&&status.equals("SERVING")?"COMPLETED":action.equals("skip")&&Set.of("CALLED","SERVING").contains(status)?"SKIPPED":null;
                if(target==null)throw new IllegalArgumentException("The ticket state changed. Refresh and try again.");
                if(target.equals("SERVING"))update(c,"UPDATE queue SET processing_clock_offset_seconds=? WHERE queue_id=?",Duration.between(LocalDateTime.now(ServiceHours.ZONE),now()).toSeconds(),queue);
                if(target.equals("COMPLETED")) {
                    Timestamp start=(Timestamp)records.get(0).get("started_at");
                    Object offset=records.get(0).get("processing_clock_offset_seconds");
                    LocalDateTime realEnd=LocalDateTime.now(ServiceHours.ZONE);
                    double minutes=start==null?0:ServiceHours.processingMinutes(start.toLocalDateTime().plusSeconds(offset==null?0:((Number)offset).longValue()),realEnd.plusSeconds(offset==null?0:((Number)offset).longValue()));
                    update(c,"UPDATE queue SET processing_seconds=? WHERE queue_id=?",minutes*60,queue);
                }
                update(c,"UPDATE queue SET status=?,"+(target.equals("SERVING")?"started_at":"completed_at")+"=CURRENT_TIMESTAMP WHERE queue_id=?",target,queue);event(c,queue,staff,target);
                Map<String,Object> result=new LinkedHashMap<>();result.put("queueId",queue);result.put("status",target);
                if(target.equals("COMPLETED") && ServiceHours.open(now()) && Boolean.TRUE.equals(counters.get(0).get("is_active"))) {
                    // A service that crosses lunch must be followed by an explicit afternoon summon.
                    if(sameOperatingWindow(c,queue))
                        result.put("next",callNext(c,service,counter,staff,day,0));
                }
                c.commit();return result;
            }catch(Exception error){c.rollback();throw error;}
        }
    }
    // Checks whether the latest summon marker matches morning/afternoon; prevents an automatic recall across the lunch boundary.
    private boolean sameOperatingWindow(Connection c,int queue)throws SQLException {
        String expected=now().toLocalTime().isBefore(LocalTime.of(13,0))?"SUMMON_MORNING":"SUMMON_AFTERNOON";
        List<Map<String,Object>> markers=rows(c,"SELECT event_type FROM queue_events WHERE queue_id=? AND event_type IN ('SUMMON_MORNING','SUMMON_AFTERNOON') ORDER BY event_id DESC LIMIT 1",queue);
        return !markers.isEmpty() && expected.equals(markers.get(0).get("event_type"));
    }
    // Selects the eligible waiting ticket in queue order and assigns it to this staff/counter; the enclosing transaction holds the service lock.
    private Map<String,Object> callNext(Connection c,int service,int counter,int staff,LocalDate day,int requested)throws SQLException{return callNext(c,service,counter,staff,day,requested,0);}
    // Selects the eligible waiting ticket in queue order and assigns it to this staff/counter; the enclosing transaction holds the service lock.
    private Map<String,Object> callNext(Connection c,int service,int counter,int staff,LocalDate day,int requested,int exclude)throws SQLException {
        List<Map<String,Object>> waiting=rows(c,"SELECT q.queue_id AS queueId,q.student_id AS studentId,q.service_id AS serviceId,q.token_number AS tokenNumber FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE q.service_id=? AND r.visit_date=? AND q.status='WAITING' AND q.queue_id<>? ORDER BY r.queue_order,q.queue_id LIMIT 1 FOR UPDATE",service,day,exclude);
        if(waiting.isEmpty()){if(requested>0)throw new IllegalArgumentException("No waiting student.");return null;}
        Map<String,Object> next=waiting.get(0);int id=number(next,"queueId");
        if(requested>0&&requested!=id)throw new IllegalArgumentException("Call the first waiting student to preserve booking order.");
        update(c,"UPDATE queue SET status='CALLED',called_at=CURRENT_TIMESTAMP WHERE queue_id=?",id);
        update(c,"UPDATE queue_reservations SET counter_id=?,staff_id=? WHERE queue_id=?",counter,staff,id);event(c,id,staff,"CALLED");event(c,id,staff,now().toLocalTime().isBefore(LocalTime.of(13,0))?"SUMMON_MORNING":"SUMMON_AFTERNOON");next.put("counterId",counter);next.put("status","CALLED");return next;
    }
    // Cancels only the authenticated student's eligible same-day WAITING reservation; other students' tickets cannot be cancelled.
    public boolean cancel(int student,int queue)throws SQLException {
        try(Connection c=DBConnection.getConnection()) {return update(c,"UPDATE queue q JOIN queue_reservations r ON r.queue_id=q.queue_id SET q.status='CANCELLED',q.completed_at=CURRENT_TIMESTAMP WHERE q.queue_id=? AND q.student_id=? AND q.status='WAITING' AND r.visit_date=?",queue,student,now().toLocalDate())==1;}
    }
    // Returns the stored counter state, falling back to is_active when no explicit state row exists.
    public boolean availability(int staff,String status)throws SQLException {
        if(!Set.of("AVAILABLE","PAUSED","CHECKED_OUT").contains(status))throw new IllegalArgumentException("Invalid counter status.");
        try(Connection c=DBConnection.getConnection()) {
            // Group related writes; commit saves them together and rollback prevents partial updates.
            c.setAutoCommit(false);
            try {
                List<Map<String,Object>> profile=rows(c,"SELECT service_id,counter_id FROM staff WHERE staff_id=?",staff);
                if(profile.isEmpty())throw new IllegalArgumentException("Staff not found.");
                int service=number(profile.get(0),"service_id"),counter=number(profile.get(0),"counter_id");
                TransactionLocks.service(c,service);
                if(status.equals("CHECKED_OUT")&&!rows(c,"SELECT q.queue_id FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE r.counter_id=? AND r.visit_date=? AND q.status IN ('CALLED','SERVING')",counter,now().toLocalDate()).isEmpty())throw new IllegalArgumentException("Finish the current student before checking out.");
                update(c,"UPDATE counters SET is_active=? WHERE counter_id=? AND service_id=?",status.equals("AVAILABLE"),counter,service);
                update(c,"INSERT INTO counter_states(counter_id,status) VALUES (?,?) ON DUPLICATE KEY UPDATE status=VALUES(status)",counter,status);c.commit();return true;
            }catch(Exception error){c.rollback();throw error;}
        }
    }
    // Returns the stored counter state, falling back to is_active when no explicit state row exists.
    public String availability(int counter)throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            List<Map<String,Object>> result=rows(c,"SELECT COALESCE(s.status,IF(c.is_active,'AVAILABLE','CHECKED_OUT')) status FROM counters c LEFT JOIN counter_states s ON s.counter_id=c.counter_id WHERE c.counter_id=?",counter);
            return result.isEmpty()?"CHECKED_OUT":(String)result.get(0).get("status");
        }
    }
    /** No absence penalty: only WAITING tickets expire, under the same service lock as calls. */
    // Carries overdue WAITING tickets to the next applicable service day, preserving their priority; closing is not a missed-call penalty.
    public int closeWaiting(int service)throws SQLException {
        LocalDateTime at=now();LocalDate day=at.toLocalDate();boolean closed=!at.toLocalTime().isBefore(LocalTime.of(15,0));
        try(Connection c=DBConnection.getConnection()) {
            // Use READ_COMMITTED and explicit transactions so service-row locks protect a consistent validation/write sequence.
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);
            try {
                // Serialize this service's operations before allocating order or changing ticket state.
                if(!TransactionLocks.service(c,service)){c.rollback();return 0;}
                List<Map<String,Object>> due=rows(c,"SELECT q.queue_id FROM queue q JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE q.service_id=? AND q.status='WAITING' AND (r.visit_date<? OR (r.visit_date=? AND ?)) ORDER BY r.visit_date,r.queue_order,q.queue_id FOR UPDATE",service,day,day,closed);
                // After closing carry to tomorrow; overdue tickets reconciled before closing target the current service day.
                LocalDate destination=closed?day.plusDays(1):day;
                int order=0;
                // Carryovers take priority over new reservations already made for the destination.
                if(!due.isEmpty())update(c,"UPDATE queue_reservations r JOIN queue q ON q.queue_id=r.queue_id SET r.queue_order=r.queue_order+? WHERE q.service_id=? AND r.visit_date=?",due.size(),service,destination);
                for(Map<String,Object> row:due){int id=number(row,"queue_id");update(c,"UPDATE queue_reservations SET visit_date=?,queue_order=?,counter_id=NULL,staff_id=NULL WHERE queue_id=?",destination,++order,id);event(c,id,0,"ROLLED_TO_NEXT_DAY");}
                c.commit();return due.size();
            }catch(Exception error){c.rollback();throw error;}
        }
    }
    // Reconciles pending tickets only for services in which this student has a WAITING record.
    private void closeWaitingForStudent(int student)throws SQLException {
        try(Connection c=DBConnection.getConnection()){for(Map<String,Object> row:rows(c,"SELECT DISTINCT service_id FROM queue WHERE student_id=? AND status='WAITING'",student))closeWaiting(number(row,"service_id"));}
    }
    // Reconciles every service with WAITING tickets; called/serving tickets are not rollover candidates.
    public void closeAllDue()throws SQLException {
        try(Connection c=DBConnection.getConnection()){for(Map<String,Object> row:rows(c,"SELECT DISTINCT service_id FROM queue WHERE status='WAITING'"))closeWaiting(number(row,"service_id"));}
    }
}

-- VIVA GUIDE: Service-day reservation metadata, staff/counter assignment, missed-turn tracking and audit/state tables. This is separate from the legacy fixed-slot bookings table.
USE campus_queue;
-- Define table and constraints for counter_states.
CREATE TABLE IF NOT EXISTS counter_states (
 counter_id INT PRIMARY KEY,
 status VARCHAR(20) NOT NULL,
    -- Foreign key links this identifier to its parent table and prevents dangling references.
 FOREIGN KEY(counter_id) REFERENCES counters(counter_id) ON DELETE CASCADE
);
-- Define table and constraints for queue_reservations.
CREATE TABLE IF NOT EXISTS queue_reservations (
 queue_id INT PRIMARY KEY,
 visit_date DATE NOT NULL,
 queue_order INT NOT NULL,
 missed_turns INT NOT NULL DEFAULT 0,
 counter_id INT NULL,
 staff_id INT NULL,
    -- Foreign key links this identifier to its parent table and prevents dangling references.
 FOREIGN KEY(queue_id) REFERENCES queue(queue_id) ON DELETE CASCADE,
    -- Foreign key links this identifier to its parent table and prevents dangling references.
 FOREIGN KEY(counter_id) REFERENCES counters(counter_id),
    -- Foreign key links this identifier to its parent table and prevents dangling references.
 FOREIGN KEY(staff_id) REFERENCES staff(staff_id),
 INDEX daily_order(visit_date,queue_order)
);
-- Define table and constraints for queue_events.
CREATE TABLE IF NOT EXISTS queue_events (
 event_id BIGINT PRIMARY KEY AUTO_INCREMENT,
 queue_id INT NOT NULL,
 staff_id INT NULL,
 event_type VARCHAR(30) NOT NULL,
 occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Foreign key links this identifier to its parent table and prevents dangling references.
 FOREIGN KEY(queue_id) REFERENCES queue(queue_id) ON DELETE CASCADE
);
-- Provision initial records in queue_reservations.
INSERT INTO queue_reservations(queue_id,visit_date,queue_order)
 SELECT q.queue_id,DATE(q.joined_at),q.token_number FROM queue q
 LEFT JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE r.queue_id IS NULL;

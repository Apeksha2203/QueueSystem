USE campus_queue;
CREATE TABLE IF NOT EXISTS counter_states (
 counter_id INT PRIMARY KEY,
 status VARCHAR(20) NOT NULL,
 FOREIGN KEY(counter_id) REFERENCES counters(counter_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS queue_reservations (
 queue_id INT PRIMARY KEY,
 visit_date DATE NOT NULL,
 queue_order INT NOT NULL,
 missed_turns INT NOT NULL DEFAULT 0,
 counter_id INT NULL,
 staff_id INT NULL,
 FOREIGN KEY(queue_id) REFERENCES queue(queue_id) ON DELETE CASCADE,
 FOREIGN KEY(counter_id) REFERENCES counters(counter_id),
 FOREIGN KEY(staff_id) REFERENCES staff(staff_id),
 INDEX daily_order(visit_date,queue_order)
);
CREATE TABLE IF NOT EXISTS queue_events (
 event_id BIGINT PRIMARY KEY AUTO_INCREMENT,
 queue_id INT NOT NULL,
 staff_id INT NULL,
 event_type VARCHAR(30) NOT NULL,
 occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(queue_id) REFERENCES queue(queue_id) ON DELETE CASCADE
);
INSERT INTO queue_reservations(queue_id,visit_date,queue_order)
 SELECT q.queue_id,DATE(q.joined_at),q.token_number FROM queue q
 LEFT JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE r.queue_id IS NULL;

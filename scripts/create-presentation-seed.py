"""Generate labelled presentation fixtures; no database credentials or usable passwords.

Yesterday's completed tickets feed the existing measured-duration ETA algorithm.
Today's tickets are WAITING bookings, appended after the current daily queue.
Only the previous demo-queue batch and this script's own labelled rows are replaced.
"""
import base64
import hashlib
import random
import secrets
from datetime import datetime, timedelta, timezone
from pathlib import Path

root = Path(__file__).resolve().parents[1]
now = datetime.now(timezone(timedelta(hours=5, minutes=30))).replace(tzinfo=None)
today = now.date()
yesterday = today - timedelta(days=1)
rng = random.Random(today.toordinal())
salt = secrets.token_bytes(16)
digest = hashlib.pbkdf2_hmac('sha256', secrets.token_bytes(32), salt, 210000)
password = 'pbkdf2$210000$' + base64.b64encode(salt).decode() + '$' + base64.b64encode(digest).decode()
names = ['Aarav', 'Diya', 'Ishaan', 'Ananya', 'Kabir', 'Meera', 'Rohan', 'Sana', 'Arjun', 'Nisha', 'Dev', 'Tara']
services = ['General Inquiries', 'Fee Payment', 'Document Verification']
lines = [
    '-- Explicit demo fixture: 36 completed yesterday; 18 waiting today. Re-running replaces this batch.',
    '-- Existing real student records and service configuration are preserved.',
    'USE campus_queue;', "SET time_zone = '+05:30';", 'START TRANSACTION;',
    "SELECT service_id FROM services WHERE service_name IN ('General Inquiries','Fee Payment','Document Verification') ORDER BY service_id FOR UPDATE;",
    "DELETE q FROM queue q JOIN students s ON s.student_id=q.student_id WHERE s.student_name LIKE '[DEMO] %' AND (s.email LIKE 'demo-queue-" + str(yesterday) + "-%@example.invalid' OR s.email LIKE 'presentation-demo-" + str(today) + "-%@example.invalid');",
    "DELETE b FROM bookings b JOIN students s ON s.student_id=b.student_id WHERE s.student_name LIKE '[DEMO] %' AND s.email LIKE 'demo-queue-" + str(yesterday) + "-%@example.invalid' AND b.booking_date='" + str(yesterday) + "';",
]
for service_index, service in enumerate(services, 1):
    lines.append(f"SET @service = (SELECT service_id FROM services WHERE service_name='{service}' ORDER BY service_id LIMIT 1);")
    durations = [1, 3, 4] * 4
    rng.shuffle(durations)
    for kind, count in [('history', 12), ('today', 6)]:
        for i in range(count):
            email = f'presentation-demo-{today}-{service_index}-{kind}-{i+1}@example.invalid'
            label = f'[DEMO] {names[i]} {kind.title()} S{service_index}'
            lines.extend([
                f"INSERT INTO students(student_name,email,password) VALUES ('{label}','{email}','{password}') ON DUPLICATE KEY UPDATE student_name=VALUES(student_name);",
                f"SET @student=(SELECT student_id FROM students WHERE email='{email}');",
                'SET @token=(SELECT COALESCE(MAX(token_number),0)+1 FROM queue WHERE service_id=@service);',
            ])
            day = yesterday if kind == 'history' else today
            lines.append(f"SET @ordering=(SELECT COALESCE(MAX(r.queue_order),0)+1 FROM queue_reservations r JOIN queue q ON q.queue_id=r.queue_id WHERE q.service_id=@service AND r.visit_date='{day}');")
            if kind == 'history':
                start = datetime.combine(yesterday, datetime.min.time()).replace(hour=10) + timedelta(minutes=i*10+rng.randint(0,3))
                end = start + timedelta(minutes=durations[i])
                joined = start - timedelta(minutes=rng.randint(3,15))
                lines.append("INSERT INTO queue(student_id,service_id,token_number,status,joined_at,called_at,started_at,completed_at,processing_seconds,processing_clock_offset_seconds) VALUES "
                             f"(@student,@service,@token,'COMPLETED','{joined}','{start}','{start}','{end}',{durations[i]*60},0);")
            else:
                # Today's samples represent bookings already waiting; never invent future completion times.
                joined = max(datetime.combine(today, datetime.min.time()), now-timedelta(minutes=60-i*8+rng.randint(0,5)))
                lines.append("INSERT INTO queue(student_id,service_id,token_number,status,joined_at) VALUES "
                             f"(@student,@service,@token,'WAITING','{joined}');")
            lines.extend([
                'SET @queue=LAST_INSERT_ID();',
                f"INSERT INTO queue_reservations(queue_id,visit_date,queue_order) VALUES (@queue,'{day}',@ordering);",
                f"INSERT INTO queue_events(queue_id,event_type,occurred_at) VALUES (@queue,'{'DEMO_COMPLETED' if kind == 'history' else 'DEMO_RESERVED'}','{end if kind == 'history' else joined}');",
            ])
lines.extend(['COMMIT;',
    "SELECT sv.service_name,q.status,r.visit_date,COUNT(*) AS demo_entries,ROUND(AVG(q.processing_seconds)/60,3) AS measured_minutes FROM queue q JOIN students st ON st.student_id=q.student_id JOIN services sv ON sv.service_id=q.service_id JOIN queue_reservations r ON r.queue_id=q.queue_id WHERE st.email LIKE 'presentation-demo-" + str(today) + "-%@example.invalid' GROUP BY sv.service_name,q.status,r.visit_date ORDER BY sv.service_name,r.visit_date;",
])
destination = root / 'database' / 'presentation_demo.sql'
destination.write_text('\n'.join(lines)+'\n', encoding='utf-8')
print(f'Created {destination}: history {yesterday}; waiting {today}.')

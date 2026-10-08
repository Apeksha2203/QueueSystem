# VIVA GUIDE: Local staff provisioning generator using standard-library PBKDF2. Generated SQL stays in ignored .runtime; plaintext input is not committed.
"""Create an ignored SQL seed; passwords are prompted locally and hashed."""
import base64
import getpass
import hashlib
import os
from pathlib import Path

accounts = [
    ('General Inquiries', 'General Inquiries Staff', 'general@staff.cq'),
    ('Fee Payment', 'Fee Payment Staff', 'fees@staff.cq'),
    ('Document Verification', 'Document Verification Staff', 'document@staff.cq'),
]
lines = ['USE campus_queue;', 'START TRANSACTION;']
for index, (service, name, email) in enumerate(accounts, 1):
    password = os.environ.get(f'CQ_SEED_PASSWORD_{index}') or getpass.getpass(f'Password for {email}: ')
    if len(password) < 6 or not any(c.isalpha() for c in password) or not any(c.isdigit() for c in password):
        raise ValueError('Passwords require at least six characters, a letter and a number.')
    # Use a different random salt per account; the SQL receives hashes, not plaintext passwords.
    salt = os.urandom(16)
    digest = hashlib.pbkdf2_hmac('sha256', password.encode(), salt, 210000, 32)
    hashed = 'pbkdf2$210000$' + base64.b64encode(salt).decode() + '$' + base64.b64encode(digest).decode()
    counter = service + ' Counter'
    lines += [
        f"SET @service_id = (SELECT service_id FROM services WHERE service_name = '{service}' ORDER BY service_id LIMIT 1);",
        f"INSERT INTO counters (counter_name, service_id, is_active) SELECT '{counter}', @service_id, TRUE WHERE @service_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM counters WHERE counter_name = '{counter}' AND service_id = @service_id);",
        f"SET @counter_id = (SELECT counter_id FROM counters WHERE counter_name = '{counter}' AND service_id = @service_id ORDER BY counter_id LIMIT 1);",
        f"INSERT INTO staff (staff_name, email, password, service_id, counter_id) SELECT '{name}', '{email}', '{hashed}', @service_id, @counter_id WHERE @counter_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM staff WHERE email = '{email}');",
    ]
lines += ['COMMIT;', 'SELECT staff_name, email, service_id, counter_id FROM staff;']
destination = Path(__file__).resolve().parents[1] / '.runtime' / 'cloud-staff-seed.sql'
destination.parent.mkdir(exist_ok=True)
destination.write_text('\n'.join(lines) + '\n', encoding='utf-8')
print(f'Created {destination}. Existing staff passwords are preserved.')

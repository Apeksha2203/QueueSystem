USE campus_queue;

-- User-approved local catalogue. Re-running does not create duplicate names.
INSERT INTO services (service_name, description, average_service_time)
SELECT 'General Inquiries', 'General questions and campus information', 10
WHERE NOT EXISTS (SELECT 1 FROM services WHERE service_name = 'General Inquiries');

INSERT INTO services (service_name, description, average_service_time)
SELECT 'Fee Payment', 'Tuition, fees, and financial transactions', 15
WHERE NOT EXISTS (SELECT 1 FROM services WHERE service_name = 'Fee Payment');

INSERT INTO services (service_name, description, average_service_time)
SELECT 'Document Verification', 'ID checks, transcript verification, and forms', 20
WHERE NOT EXISTS (SELECT 1 FROM services WHERE service_name = 'Document Verification');

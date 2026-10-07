# Campus Queue: master project and viva guide
Academic preparation edition | 8 October 2026

## How to use this guide
Read the architecture, queue rules and database chapters first. Then read the walkthrough for the module you own. Practise the SQL exercises on a scratch database. Use the numbered source appendix to rehearse explaining unfamiliar lines. This is a reference manual, not a script to memorize. The separate presentation runbook provides the demonstration order.

This edition describes the integrated repository on main. Local test fixtures and credentials are excluded. Screens and source may evolve; the line numbers in the appendix refer to the snapshot used to generate this document. Regenerate it after substantial changes.

## 1. Problem, objective and scope
Campus offices handle general inquiries, fee payments and document verification. A physical line requires students to stand at a counter and gives little visibility into their turn. Campus Queue lets a student reserve a place in a service queue, inspect the projected position and estimated time, and track the ticket while staff process the queue.

The core contribution is coordination: the student interface, staff interface and Java backend share one database. Staff actions change ticket states, and the student interface polls the backend to display the change. The application does not process bank payments, verify documents automatically, send SMS, or conduct phone calls itself.

Three services are configured: General Inquiries, Fee Payment and Document Verification. Their initial processing estimates are 10, 15 and 20 minutes. These are defaults until real completed-service samples exist; they are not appointment slots.

Explain the project in one sentence: "Our application manages same-day campus service queues with online reservation, service-specific staff access, measured processing estimates and a controlled missed-turn policy."

## 2. Live system and deployment topology
Student website: https://campusque.netlify.app/
Staff website: https://campusquestaff.netlify.app/
Backend service: https://queuesystem-1.onrender.com
Public catalogue check: https://queuesystem-1.onrender.com/api/services
Repository: https://github.com/Apeksha2203/QueueSystem

The browser downloads React HTML, CSS and JavaScript from Netlify. Requests to /api on that same Netlify origin are rewritten to the Render backend. Tomcat hosts the Java WAR on Render and uses JDBC to access MySQL on Railway. Workbench on a PC is another client of that same cloud MySQL server.

Student browser -> student Netlify /api proxy -> Render Tomcat -> Railway MySQL.
Staff browser -> staff Netlify /api proxy -> the same Render Tomcat -> the same Railway MySQL.
Workbench -> Railway public TCP proxy -> the same Railway MySQL.

GitHub stores code, not running database rows. A push triggers deployment if automatic deployment is enabled. Updating Java source does not copy local MySQL data into Railway. Export/import is a separate deliberate operation.

## 3. Technology stack and responsibilities
HTML supplies document structure. CSS controls layout, colour, spacing, typography, responsive rules and animation. JavaScript handles browser behaviour. JSX describes React component trees and is compiled into JavaScript by Vite; the browser does not execute JSX directly.

React manages components and state. The student app uses HashRouter, producing routes such as /#/bookings. The staff app uses BrowserRouter and needs a static-host fallback for routes such as /dashboard. Vite is a development server and build tool; its local proxy does not configure production routing.

Java 17 implements request handling and business rules. Jakarta Servlet 6 provides HTTP request/response/session APIs. Tomcat 10.1 is the servlet container. Maven resolves dependencies, compiles classes and packages a WAR. MySQL Connector/J is the JDBC driver. JDBC exposes Connection, PreparedStatement and ResultSet.

JSP files remain in src/main/webapp from earlier teammate modules. They are not the deployed React dashboards. Be honest if asked: "The current primary dashboards use React; JSP is retained as legacy code and is processed by Tomcat when requested." Do not describe this project as Spring Boot or claim it runs an executable Spring JAR.

## 4. Repository orientation
QueueSystemFrontend/ contains the student app, package files, public assets and Netlify config. frontend/ contains the staff app. src/main/java/com/queue/controller contains HTTP adapters. service contains business rules. dao contains database access. model contains Java data objects. util contains configuration, hashing, JSON, validation and locking helpers. filter contains legacy route ownership enforcement.

database/schema.sql creates campus_queue and the base tables. migrations/001_daily_reservations.sql adds reservation metadata, event history and counter state. migrations/002_processing_duration.sql adds measured-processing fields. seed_services.sql inserts the service catalogue without duplicate names.

scripts/ contains local startup/configuration, staff seed generation, deployment redirect generation and test tools. scripts/tests/ contains standalone validation programs. These are not automatically all executed by mvn package. Dockerfile builds the backend image. .dockerignore limits the build context. .gitignore prevents local credentials and generated runtime artifacts from being committed.

TEAM-CHANGELOG.md records implemented changes. BRANCH-INTEGRATION.md explains integration of teammate branches. CLOUD-DEPLOYMENT.md documents hosting. Earlier plans/reviews and legacy tests can describe superseded behaviour; use the current code and the explicit limitations chapter for final claims.

## 5. Service, counter and staff: three different concepts
A service is the work requested: for example Fee Payment. A counter is a physical or logical desk that serves one service. A staff member is a person/account assigned to a service and counter. Multiple counters can serve one service queue.

All three demonstration accounts have staff privileges, with different service assignments. They are not three independent global security roles. The backend reads staff_id from the authenticated session, resolves the account's service and counter, and scopes queue operations accordingly. Frontend hiding is not the authorization boundary.

There is no implemented administrator portal or administrator login in this snapshot. Assignments are provisioned in database setup scripts. An admin portal with audited assignment management is a future improvement, not a feature to demonstrate as completed.

The identifiers stored in staff.service_id and staff.counter_id must agree with counters.service_id. ReservationService validates the counter-service match before processing a ticket. Keeping both references is convenient for current queries but creates redundant data that must remain consistent.

## 6. Student journey and confirmation boundary
The public landing page explains the workflow. The student creates an account using name, email, Indian mobile number and password, then signs in. The overview shows available services and any active ticket. Selecting a service card opens the booking flow with that service selected; browsing does not create a ticket.

The booking preview shows projected position, people ahead, estimated service clock time and operating hours. Only an explicit Reserve my place submission creates queue and reservation rows. A preview can become stale when another student books first, so the backend recalculates eligibility and ordering in the reservation transaction.

The queue page shows token, service, state, position and guidance. A waiting ticket for today can be cancelled by its owner. Bookings lists active/upcoming and past records. Profile/theme preferences should not be presented as proof of server-side notification delivery.

## 7. Operating hours and prebooking
The campus timezone is Asia/Kolkata. Service is open from 10:00 inclusive to 13:00 exclusive, and from 14:00 inclusive to 15:00 exclusive. At 13:00 the lunch break begins; at 15:00 the service window closes. Registration of a new queue reservation is for today's date only.

A student may reserve at 07:30 for today's queue even though staff have not opened the desk. This establishes queue order, not a right to immediate service. Pre-opening estimates start from 10:00 and assume the scheduled counters will be staffed. At lunch the next service opening is 14:00. The system does not currently implement holidays or weekends; "next day" means the next calendar day.

Local testing previously used an ignored test-clock.local.properties file. It has been disabled for the final schedule. ServiceHours reads real campus time when no override is present. The Docker image does not copy local configuration or test-clock files. Do not change the Render web PORT to change operating hours.

## 8. Token identity, position and order
A token is a stable label within a service. ReservationService computes the next token as MAX(token_number)+1 for that service under a service lock. Tokens do not reset each day in the current implementation. The displayed number is zero-padded, such as 001. Some older staff components prefix a token with A; this is presentation, not a separate queue identity.

queue_id is the database primary key and identifies the record. token_number is a service-local display label. queue_order in queue_reservations determines FIFO order for a visit date. Position is derived from currently active tickets and can change when others complete, cancel, miss a call or when counter assignments change.

Example: token 017 can move from position 10 to position 1 without its token changing. A carried ticket also preserves its token. Do not confuse token 017 with a guarantee that sixteen people are ahead now.

## 9. Ticket state machine
The normal path is WAITING -> CALLED -> SERVING -> COMPLETED. WAITING means booked but not assigned for service. CALLED means staff has summoned and assigned the ticket to a counter. SERVING means processing has started. COMPLETED means staff explicitly completed a started service.

WAITING -> CANCELLED is permitted for the owner and today's reservation. CALLED -> WAITING occurs after the first absence. CALLED -> NO_SHOW occurs after the second absence. A staff skip can produce SKIPPED. Closing rollover preserves WAITING and updates the reservation date/order; it must not falsely mark work COMPLETED.

The backend rejects invalid transitions, such as completing WAITING or marking a SERVING student absent. The UI buttons reflect state, but the service layer rechecks state and ownership in the transaction.

## 10. Staff workflow and telephone action
At the first queue of a morning session, staff explicitly summons the next student. CALLED exposes Start and Mark absent. Start records started_at and changes the state to SERVING. Complete records completion and processing duration. During the same open operating window, completion can automatically summon the next waiting student.

An explicit summon is required after lunch before the afternoon sequence. A SUMMON_MORNING or SUMMON_AFTERNOON queue event identifies the operating window. If a service crosses lunch, automatic continuation does not silently start the afternoon queue. Automatic advancement also respects counter availability.

The telephone icon uses a tel: link with the student's normalized contact number. It asks the browser/device's calling application to handle a call. A PC may prompt for an app; the website does not initiate a server-side telephone call or know whether the student answered. Calling the phone does not change ticket state.

## 11. Missed turns and fairness
Only a CALLED student can be marked absent. On the first miss, missed_turns increments, the ticket returns to WAITING, the assignment is cleared and the ticket is inserted behind up to ten waiting students. If fewer than ten are waiting, it goes behind those available students. This is not always literally position ten: with ten people ahead, the absent student becomes the eleventh waiting ticket.

On the second miss, the ticket becomes NO_SHOW and exits the active queue. This ends that ticket; it does not permanently ban the student's account. The student does not lose their token history. The service records event types MOVED_BACK_FIRST_MISS and REMOVED_SECOND_MISS.

An absent student is excluded from immediate automatic recall when no other student is waiting. A waiting ticket rolled to tomorrow due to closing receives no additional missed-turn penalty.

## 12. Closing risk and next-day carryover
The booking preview projects the time at which the student's turn would start. If that projected time is at or after 15:00, a new booking is rejected with guidance to return tomorrow. Existing waiting students can see a closing-risk warning as queue estimates change.

At closing, pending WAITING tickets are moved to the next day's 10 AM queue. Their tokens and missed counts are preserved. Carryovers are ordered ahead of new reservations already created for the destination date. They remain WAITING; completion requires an actual staff completion action.

ClosingTimeListener runs reconciliation periodically, and reads also invoke reconciliation. After an outage, older waiting dates can be moved to the current open day's queue or the next day if already closed. The operation locks a service and is idempotent: tickets already moved are not duplicated on the next run.

CALLED and SERVING tickets are not part of waiting rollover. Finishing a current service after closing is allowed where the current date/ownership rules still permit it. Cross-midnight called/serving handling needs further validation and should not be claimed as fully resolved.

## 13. Dynamic average processing time
Average processing time is based on actual processing, not the interval from booking to completion. A booking at 07:00, a service start at 10:00 and completion at 10:03 contribute approximately three processing minutes, not three hours and three minutes.

WaitingTimeService reads the latest twenty valid COMPLETED records for that service, ordered by completed_at and queue_id. It uses processing_seconds where present, otherwise it calculates the overlap of started_at/completed_at with scheduled service windows. Only positive durations are averaged. The lower bound is one second, preserving fast demonstration measurements.

If five positive samples total fifteen minutes, the average is three minutes. Adding a sixth sample of nine minutes produces twenty-four divided by six = four minutes. Once there are more than twenty samples, the oldest sample drops out. The average is per service, not pooled across unrelated services.

When there are no usable samples, the service's configured estimate is used. Seeing 10, 15 or 20 on a newly deployed empty database is therefore expected. The public /api/services endpoint returns configuration defaults; the authenticated student overview computes dynamic averageServiceTime separately.

## 14. ETA calculation and its limits
For a booking preview, processing backlog is approximately peopleAhead * measuredAverage / eligibleCounters. ReservationService rounds the required backlog up to whole minutes and projects through service openings and the lunch break. Helpers in WaitingTimeService also expose round-based text/integer estimates; distinguish them from the actual preview calculation.

Position ten ordinarily means nine people ahead. At three minutes per person and one counter, waiting before the student's service is about twenty-seven minutes. Ten people ahead means about thirty minutes. The student's own processing time is not part of time until their service starts.

Before opening, a second reservation with a three-minute average and one counter projects about 10:03. At 12:58 with five minutes of backlog, projection skips lunch and becomes 14:03. With two counters, the calculation assumes both can process at the measured average rate.

The student app polls about every eight seconds and also refreshes when focused. The staff app polls about every seven seconds. This is polling, not WebSockets or instantaneous synchronization.

Important limit: the current estimator updates completed-service samples after completion. It does not continuously predict the remaining duration of a particularly long active service, and it is not a machine-learning model. Parallel counter speed differences are approximated. Do not promise a guaranteed arrival or completion time.

## 15. Transactions and concurrent booking
Two browsers can confirm a reservation simultaneously. Without protection, both could read the same maximum token and insert conflicting logical order. TransactionLocks locks the service's parent row with SELECT ... FOR UPDATE. All participating reservation/operation transactions acquire the same service lock, serializing changes to that service.

reserve opens a connection, sets READ_COMMITTED isolation, disables auto-commit, acquires the service lock, recomputes preview, calculates token/order, inserts queue, inserts queue_reservations, adds RESERVED event, and commits. Any exception rolls back the entire transaction. This prevents an orphan queue record from a partial insert.

operate resolves the staff's service/counter, locks the service and counter, validates current assignment/state, applies the action and related event, optionally advances the next ticket, and commits. Different services have different parent-row locks and can proceed independently.

Cancellation is currently a conditional update rather than the same multi-step transaction. A conditional WHERE on owner, status and date provides basic state protection, but comprehensive database constraints and race testing remain valuable. MAX+1 relies on all writers following the lock convention; manually bypassing the service layer can undermine it.

## 16. Database model and relationships
students: student_id primary key; name, unique email, normalized phone and password hash. services: service_id, name, description and initial average. counters: counter_id, name, service foreign key and active flag. staff: staff_id, unique email, hash and service/counter assignments.

queue: queue_id, student/service foreign keys, token, state and joined/called/started/completed timestamps. Migration 002 adds processing_seconds and processing_clock_offset_seconds. queue_reservations: one-to-one metadata keyed by queue_id, visit_date, queue_order, missed_turns and optional staff/counter assignment.

queue_events: append-only event records with event_id, queue_id, optional staff_id, event_type and occurred_at. counter_states: one row per counter containing AVAILABLE, PAUSED or CHECKED_OUT. bookings: older appointment-slot records; the current same-day reservation flow writes queue plus queue_reservations, not this legacy table.

Primary keys identify rows. Foreign keys preserve relationships. students.email and staff.email have unique constraints. NOT NULL rejects absent required values. DEFAULT supplies initial state or timestamps. The daily_order index on reservation date/order supports ordered queue retrieval but is not a unique constraint.

Deleting a queue row cascades to its reservation and events according to their foreign keys. Deleting a student who still has queue/bookings records may fail; understand child rows before deleting. Not every relationship uses ON DELETE CASCADE. staff_id in queue_events is currently not a declared foreign key.

## 17. SQL practice: read and explain
Use a scratch database or read-only queries for viva. Never experiment with unrestricted DELETE or UPDATE on teammates' live bookings. The following SELECTs are safe to run against the cloud schema.

```sql
USE campus_queue;
SHOW TABLES;
DESCRIBE students;
SHOW CREATE TABLE queue_reservations;
SELECT service_id, service_name, average_service_time FROM services;
SELECT queue_id, token_number, status FROM queue ORDER BY queue_id DESC LIMIT 10;
```

Explain USE as selecting the default schema; SHOW TABLES as listing objects; DESCRIBE as exposing columns/types/nullability/keys; SHOW CREATE TABLE as revealing exact constraints. WHERE filters rows, ORDER BY sorts, LIMIT bounds output, and selecting explicit columns avoids unnecessary sensitive fields such as password hashes.

```sql
SELECT q.queue_id, s.student_name, v.service_name,
       q.token_number, q.status, r.visit_date, r.queue_order
FROM queue q
JOIN students s ON s.student_id = q.student_id
JOIN services v ON v.service_id = q.service_id
JOIN queue_reservations r ON r.queue_id = q.queue_id
ORDER BY r.visit_date, r.queue_order, q.queue_id;
```

This INNER JOIN returns tickets with all required related records. Aliases shorten qualified names. Explain each ON clause as the relationship being matched, not a condition that invents a new relationship. A LEFT JOIN is appropriate when assignment is optional and unassigned tickets must remain visible.

```sql
SELECT v.service_name, q.status, COUNT(*) AS tickets
FROM queue q JOIN services v ON v.service_id = q.service_id
GROUP BY v.service_name, q.status
ORDER BY v.service_name, q.status;

SELECT service_id, AVG(processing_seconds) / 60 AS avg_minutes,
       COUNT(processing_seconds) AS measured_count
FROM queue WHERE status = 'COMPLETED' AND processing_seconds > 0
GROUP BY service_id HAVING COUNT(processing_seconds) >= 1;
```

COUNT(*) counts rows; COUNT(column) ignores NULL. AVG ignores NULL. WHERE runs before grouping; HAVING filters groups after aggregation. This illustrative AVG uses all matching completed records, whereas application ETA uses the most recent twenty valid samples.

## 18. SQL practice: safe inserts, updates and deletes
For live demonstration of CRUD, create a separate scratch schema. This shows DDL and DML without changing real queue records or storing a fake plaintext password in the application tables.

```sql
CREATE DATABASE IF NOT EXISTS campus_queue_viva;
USE campus_queue_viva;
CREATE TABLE IF NOT EXISTS demo_visits (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_name VARCHAR(100) NOT NULL,
  service_name VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'WAITING'
);
INSERT INTO demo_visits (student_name, service_name)
VALUES ('Viva Test Student', 'General Inquiries');
SET @demo_id = LAST_INSERT_ID();
SELECT * FROM demo_visits WHERE id = @demo_id;
UPDATE demo_visits SET status = 'CALLED' WHERE id = @demo_id;
SELECT * FROM demo_visits WHERE id = @demo_id;
DELETE FROM demo_visits WHERE id = @demo_id;
SELECT * FROM demo_visits WHERE id = @demo_id;
```

DDL creates or changes structures; DML manipulates row data; SELECT retrieves data. AUTO_INCREMENT provides a surrogate key. LAST_INSERT_ID() is connection-specific. A DELETE with a primary-key condition targets one row. Deleting a row and dropping a table are different operations.

```sql
START TRANSACTION;
INSERT INTO demo_visits (student_name, service_name)
VALUES ('Rollback Example', 'Fee Payment');
SELECT * FROM demo_visits WHERE id = LAST_INSERT_ID();
ROLLBACK;
SELECT * FROM demo_visits WHERE student_name = 'Rollback Example';
USE campus_queue;
```

Explain COMMIT as making a transaction permanent and ROLLBACK as discarding uncommitted changes. MySQL DDL can implicitly commit, so do not assume CREATE TABLE can be rolled back like an INSERT. If asked to demonstrate foreign-key rejection, use a scratch parent/child pair instead of modifying application assignments.

## 19. Authentication and validation
The student browser submits form-encoded data. StudentPortalServlet validates registration through StudentService and StudentValidation, hashes the password, inserts the student, and creates a session after successful login. Replacing the prior session reduces session fixation risk. studentId or staffId in the server session is trusted identity, not a client-supplied ID.

Passwords uses PBKDF2WithHmacSHA256 with 210000 iterations, a random sixteen-byte salt and a 256-bit derived key. The stored format includes algorithm, iteration count, salt and derived hash in Base64 components. A salt makes identical passwords produce different stored hashes. Base64 is encoding, not encryption. The password is verified by deriving the candidate hash and comparing bytes.

The project's user-requested password rule is six to 128 characters with at least one ASCII letter and number. It is deliberately simple for demonstration and is not a claim of strong production password policy. Registration accepts an Indian ten-digit number beginning 6-9, optionally +91, and normalizes it to +91 form. Email validation checks syntax and length; it does not verify ownership or restrict every account to an actual college domain.

The student cookie session expires after thirty minutes of inactivity. Frontend fetch includes credentials so the browser carries the session cookie. Logout invalidates the server session. Staff and student domains receive their own proxy-origin cookies; do not assume a login on one domain signs into the other automatically.

Legacy plaintext passwords can be migrated after a successful login in retained DAO paths. Cloud staff setup stores hashes from the beginning. Do not show stored hashes or database passwords in screenshots unnecessarily, and do not publish the real database credentials in this manual.

## 20. Security boundaries and limitations
PreparedStatement binds values using ? placeholders rather than concatenating user input into SQL. This prevents parameter values from being interpreted as SQL syntax. It does not secure arbitrary dynamically constructed table names or replace authorization checks.

Staff operations verify their assigned service, counter, student ticket, ownership and current state. Student cancellation matches the student session ID. A client that changes a serviceId cannot gain another staff account's service access merely by editing the frontend.

Student mutating requests require an X-Requested-With header in the current adapter. This is not a comprehensive CSRF design. The application does not yet claim rate limiting, password recovery, email/phone verification, MFA, comprehensive audit administration or fine-grained admin access. A publicly hosted demo with shared staff credentials should not be treated as hardened production infrastructure.

Some older endpoints retain manual JSON construction and DAO error-swallowing patterns. /api/services was corrected to return HTTP 503 on catalogue query failure rather than a misleading successful empty array. Safe console diagnostics expose exception type, SQLState and vendor code, not database credentials.

## 21. Code walkthrough: HTTP to database
For a reserve click, find BookingForm in student App.jsx. Locate studentRequest in student-api.js: it chooses GET or POST, includes credentials, uses URLSearchParams for the body, parses JSON and throws an error when HTTP or application success indicates failure.

Then open StudentPortalServlet. @WebServlet("/api/student/*") maps the route prefix. doGet/doPost dispatch to the handler. /bookings/create checks the date and invokes ReservationService.reserve with the authenticated student ID. The service locks, validates and writes. The servlet serializes the result to JSON; the frontend refreshes state to render the ticket.

For a staff completion, trace StaffDashboard.runAction -> api.completeService -> POST /api/queue/complete -> QueueServlet -> ReservationService.operate. The service checks ownership and SERVING state, calculates processing duration, updates COMPLETED, records an event, may summon next, then commits. QueueTable and ActivityFeed render the refreshed response.

For ETA, trace student overview/preview -> WaitingTimeService -> completed sample SQL -> ServiceHours processing overlap -> projection. This is distinct from AnalyticsService's dashboard statistics, some of which still use elapsed timestamps and can include pre-opening waiting.

## 22. How to explain any random line
Begin with the file's responsibility, then identify the enclosing function and input/output. Explain the statement in plain language, why it is needed, and what happens on failure. Follow the caller or callee if needed. Avoid reading punctuation aloud without meaning.

Example: p.setInt(1, serviceId) binds serviceId to the first SQL placeholder; it does not execute the query. executeQuery executes a SELECT and returns ResultSet. while(r.next()) advances through records. try-with-resources automatically closes result sets, statements and connections even after an exception.

Example: c.setAutoCommit(false) groups subsequent statements into a transaction. c.commit() persists them together. c.rollback() undoes pending statements on that connection. FOR UPDATE holds a row lock until the transaction completes; it is not a Java synchronized block.

Example: useState creates component state and a setter; useEffect synchronizes with an external process such as polling and returns cleanup; useMemo caches a derived calculation by dependencies; useCallback preserves a function identity by dependencies. An interval must be cleared on unmount to avoid background work from abandoned screens.

Example: conditional JSX selects what to display based on state; it does not authorize database access. map renders a list, and a stable key helps React preserve identity. Optional chaining accesses a field only when the value is not nullish. A CSS grid declaration distributes columns; media queries change the rule at a viewport boundary.

The source appendix includes numbered current production files and a file role map. Match a judge's selected line to this sequence: declaration -> input -> validation -> work -> result/error -> next consumer.

## 23. Local versus cloud database viewing
MySQL is the database server. Workbench is a visual client, comparable to Supabase's table/SQL editor experience but provided separately. Expand campus_queue -> Tables and open/select table rows to view them; a result grid can refresh without typing a new query every time.

Create separate Workbench connections: local 127.0.0.1:3306 and Campus Queue Cloud using Railway's public host/port. They point to different servers. Opening cloud MySQL from your PC shows online data because the PC connects to the cloud; it does not create an offline synchronized copy.

For the viva, open Campus Queue Cloud and show tables, DESCRIBE, joins and the newly created queue record. If asked to show a truly local database, use a deliberate export/import snapshot. Automatic bidirectional offline synchronization is not implemented. An offline PC cannot view the current cloud database without network access.

## 24. Cloud database setup
On Railway create a project and add MySQL. Apply pending changes and wait until running. Enable a public TCP proxy in Networking. MYSQL_PUBLIC_URL has a form such as mysql://USER:PASSWORD@PUBLIC_HOST:PUBLIC_PORT/railway. The hostname is the part after @ and before the host-port colon; the public port is the following number. The variable name itself is not a hostname.

In Workbench create a MySQL connection and enter the actual public host, public port, username and password separately. Test Connection. Do not use mysql.railway.internal from a PC or Render; that private address is for services inside Railway.

Run schema.sql, migration 001, migration 002 and seed_services.sql in order. The project creates campus_queue rather than using the default railway schema. Generate the ignored .runtime/cloud-staff-seed.sql with scripts/create-cloud-staff-seed.py and execute it on the cloud connection to create counters and staff assignments. That seed uses PBKDF2 hashes and preserves existing accounts.

There are no bank/API provider keys required for the core project. Database credentials are secrets. .env.example is a template; .env and local config files are ignored. Java reads environment variables and does not automatically load a .env file. VITE-prefixed environment values become browser-visible and must never contain database passwords.

## 25. Render deployment step by step
Create New -> Web Service and connect Apeksha2203/QueueSystem, main branch. The repository owner must grant the Render GitHub app access for a private repository; collaborator push permission alone is not sufficient. Select Docker runtime and leave Root Directory blank. Choose Free compute explicitly; do not accidentally leave a paid selection enabled.

Set PORT=8080 for Tomcat HTTP. Set QUEUE_DB_USER and QUEUE_DB_PASSWORD to database credentials. Set QUEUE_DB_URL to jdbc:mysql://PUBLIC_HOST:PUBLIC_PORT/campus_queue?connectionTimeZone=Asia/Kolkata. The MySQL public port and the Tomcat HTTP port are different numbers. Configure TLS according to the provider's supported setup; do not casually disable certificate verification to conceal a connection problem.

The Dockerfile uses Maven with Java 17 to build queue-system.war, then copies it into Tomcat as ROOT.war. It removes default webapps and disables the shutdown listener. Docker context includes pom.xml and src only; local runtime/configuration is not copied. Tomcat listens for HTTP on 8080.

After Deploy reports Live, open /api/services and confirm the three service records. A 404 at / is expected because there is no backend homepage. Live means the web process is up, not that database queries or login are necessarily correct. Check a real API and authentication flow.

## 26. Netlify deployment step by step
Create a project by importing the same GitHub repository and main branch. For student set base directory QueueSystemFrontend. For staff create a second project with base directory frontend. Each base contains netlify.toml. Build command: npm run build && node ../scripts/netlify-redirects.mjs. Publish directory: dist.

Set QUEUE_BACKEND_URL=https://queuesystem-1.onrender.com on both Netlify projects. Use the origin only, without /api/services. No database password belongs in Netlify frontend environment variables. The build script requires an HTTPS origin and generates dist/_redirects with the /api proxy first, then the SPA fallback.

An /api/* rewrite sends requests to the Render backend while the browser uses the Netlify origin. The final /* /index.html fallback allows staff BrowserRouter URLs to work on refresh. Student HashRouter handles the hash locally. The API rule must come first so API requests are not returned HTML instead of JSON.

To remove the bottom-right badge for all visitors: Project configuration -> General -> Powered by Netlify badge -> Configure -> Off -> Save. This does not require a redeploy. Merely hiding the badge as a visitor applies only to that browser, not everyone.

## 27. Deployment incident log: what went wrong and why
Incident 1: Workbench host was entered as MYSQL_PUBLIC_URL literally and port became NaN. Resolution: reveal the variable's value and split the real address into host, port, username and password. Lesson: a configuration variable name is not its value.

Incident 2: Render defaulted to a paid $7/month compute option. Resolution: select $0/month Free and verify the displayed selection before deploying. Lesson: budget and free-tier limits must be explained before provisioning.

Incident 3: root URL returned Tomcat 404. Resolution: check /api/services; the backend hosts APIs, not the React homepage. Lesson: a root-page 404 is not by itself proof of failed deployment.

Incident 4: logs showed Invalid shutdown command [HEAD / HTTP/1.1]. HTTP probes were reaching a shutdown listener. Resolution: disable Tomcat's shutdown port in the Docker image, leaving HTTP on 8080. Lesson: distinguish a management listener from the HTTP connector.

Incident 5: API returned success:true with an empty array while Workbench had three services. ServiceDAO swallowed failures. Resolution: throw a service-load exception and return HTTP 503 with a safe generic message. Console logging was then changed to stderr because context logging did not surface in the host console.

Incident 6: CommunicationsException SQLState=08S01 showed a communications failure. Comparing settings found Render JDBC used database port 8080 while the working Workbench connection used public MySQL port 29081. Resolution: fix the JDBC database port while retaining PORT=8080 for Tomcat. The API then returned all three services. This specific proxy port can change if infrastructure is recreated; always use the current provider value.

## 28. Hosting costs and demonstration reliability
Render Free web services spin down after fifteen minutes without inbound traffic; the first request after that can be slow. Free hours are shared at workspace level. A successful demo should begin by opening the backend and websites early, not waiting until a judge clicks Login. Do not use artificial traffic merely to evade provider limits.

Railway's trial grants five dollars of credit for up to thirty days; after that, its Free plan provides one dollar of monthly credit. A continuously running MySQL server is not guaranteed to fit that allowance. Netlify free plans also have usage limits. Check account billing/usage rather than promising perpetual zero-cost hosting. No paid upgrade is authorized by this manual.

References checked for this edition:
https://render.com/docs/free
https://render.com/docs/docker
https://render.com/docs/web-services
https://docs.railway.com/pricing/free-trial
https://docs.railway.com/databases/mysql
https://docs.netlify.com/manage/routing/redirects/rewrites-proxies/
https://docs.netlify.com/manage/projects/powered-by-netlify-badge/
https://tomcat.apache.org/tomcat-10.1-doc/config/server.html

## 29. Testing evidence and what remains
Local validation previously passed 63 daily reservation checks, analytics/processing-window checks, password policy checks and 29 reservation API checks. Frontend builds and Maven packaging passed. These are standalone scripts and manual checks; do not say Maven automatically executed all of them.

Cloud smoke checks during this documentation run confirmed both frontend homepages, backend catalogue and both /api proxies return successfully. All three staff accounts logged in with the expected service/counter IDs, and profile, queue and dashboard-summary requests preserved sessions. A browser registration for a labelled deployment test student reached the dashboard. Real-time prebooking preview returned 10:00 with testClock=false; a reservation received token 001 and was immediately cancelled successfully. The labelled test account and cancelled ticket remain as audit evidence. Full notes are in docs/CLOUD-VALIDATION.md.

No full cloud concurrency/absence/lunch/midnight suite has been run against production in this edition. It would require controlled fixtures and operating-window timing. Local tests exercise several of these rules using isolated fixed-time fixtures. Functional correctness and UI appearance are different verification dimensions.

## 30. Known limitations to acknowledge confidently
No administrator portal. No true offline/cloud bidirectional synchronization. No SMS/OTP/email verification or password recovery. Telephone is a device tel: link. Queue updates are periodic polling. ETA does not yet model a running job's remaining duration continuously. Initial values remain until completed positive samples exist.

Future carried WAITING tickets are visible, but cancellation currently requires visit_date=today; the UI should not promise future-ticket cancellation. Cross-midnight CALLED/SERVING handling needs refinement. Holidays/weekends are not excluded. Closure admission uses projected start, not guaranteed completion before closing. Multi-counter prediction assumes comparable rates.

Some dashboard analytics use elapsed timestamp differences and can include pre-opening wait; they are distinct from the scheduled-window ETA samples. Legacy slot-booking endpoints, JSP pages and tests remain and should be inventoried or retired before a hardened production release. Authentication error handling and manual JSON in older modules need further review. Database timezones must remain aligned with campus logic.

Present limitations as engineering scope: explain the existing boundary, why it matters and the next change needed. Do not invent features to fill a judge's question.

## 31. Viva question bank: architecture and frontend
Q: Why two frontends? A: Student and staff workflows differ, but both use the same backend and database. Separate deployments do not create separate queues.
Q: Why React? A: Component/state-based rendering suits polling queue data and reusable ticket/service UI. It is a design choice, not a requirement for the algorithm.
Q: What is JSX? A: Syntax describing element trees that a build tool compiles to JavaScript; it is not Java or JSP.
Q: What happens after clicking Reserve? A: A POST reaches the Java adapter, session identity is checked, the service transaction inserts related rows, and refreshed frontend state renders the ticket.
Q: Why useState? A: It stores values whose changes should render the UI, such as queue data or a busy flag.
Q: Why useEffect cleanup? A: It stops intervals/side effects after unmount or dependency changes.
Q: Why credentials:include? A: Session cookies must accompany requests through the proxy.
Q: What is HashRouter? A: It interprets the URL fragment locally; BrowserRouter uses actual paths that need a hosting fallback.
Q: Why not create a booking on service-card click? A: Exploration should show a preview; confirmation is the explicit write boundary.
Q: Is the UI real time? A: It refreshes by polling, not push-based WebSockets.
Q: Is the phone number verified? A: Syntax is validated and normalized; ownership is not verified.
Q: Can a frontend hidden button secure an action? A: No; the server must verify identity, assignment and state.

## 32. Viva question bank: Java, HTTP and deployment
Q: What is a servlet? A: A container-managed Java HTTP request handler with lifecycle methods such as init and doGet/doPost.
Q: What does @WebServlet do? A: It declares the URL mapping used by Tomcat.
Q: GET versus POST? A: GET retrieves representations; POST submits actions/data. Booking creation is POST.
Q: What is a session? A: Server-maintained login state associated with a cookie identifier.
Q: Why a WAR? A: This servlet application is packaged for deployment into Tomcat, unlike an executable application JAR.
Q: What does Maven do? A: Dependency resolution, compilation and packaging according to pom.xml and plugin configuration.
Q: What is JDBC? A: Java's database API; Connector/J translates its calls into MySQL protocol operations.
Q: Why PreparedStatement? A: Bind user values separately from SQL syntax and use typed parameters.
Q: Why try-with-resources? A: Deterministic resource closure even when an exception interrupts execution.
Q: Why return 503 for catalogue failure? A: Database inability is a service availability failure, not a successful empty result.
Q: Why root 404 on Render? A: No backend homepage exists; React is hosted on Netlify.
Q: Why two ports? A: HTTP 8080 belongs to Tomcat; Railway's public port belongs to MySQL transport.
Q: What does Docker add? A: A repeatable build/runtime image with Java and Tomcat configured for hosting.
Q: Does .env automatically configure Java? A: No; our utility reads process environment or an explicitly selected local properties file.
Q: Why can a teammate push but Render cannot see the repo? A: GitHub app authorization is separate from human collaborator access.

## 33. Viva question bank: SQL and queue logic
Q: Primary versus foreign key? A: A primary key identifies a row; a foreign key enforces a reference to another table.
Q: Is token the primary key? A: No; queue_id is primary key, and tokens are service-local labels.
Q: Is FIFO always preserved? A: Initial reservations follow FIFO; the documented first-miss penalty and carryover priority adjust waiting order intentionally.
Q: Why a transaction? A: Queue, reservation and event inserts must succeed or fail together.
Q: What race does a lock solve? A: Two clients computing the same next token/order before either commits.
Q: Does MAX+1 alone guarantee unique tokens? A: No; it relies on consistent service locking and would benefit from database constraints as additional defence.
Q: WHERE versus HAVING? A: WHERE filters input rows, HAVING filters aggregate groups.
Q: INNER versus LEFT JOIN? A: INNER requires matches; LEFT keeps the left row when an optional right-side row is absent.
Q: DELETE versus DROP? A: DELETE removes rows; DROP removes the object itself.
Q: What happens after two absences? A: The ticket ends as NO_SHOW; the account is not permanently banned.
Q: What happens at 15:00 to waiting students? A: They roll to the next calendar day's 10 AM queue without a miss penalty.
Q: How do prebookings affect average duration? A: Booking time is excluded; duration starts at actual service start and only scheduled processing overlap is counted.
Q: Why keep historical data? A: It supports measured averages, analytics and an event trail.
Q: Does empty legacy bookings mean nothing was booked? A: No; the current flow uses queue and queue_reservations.
Q: How does Workbench show online changes? A: It connects to the same cloud server; refresh fetches rows. It is not a synchronized local copy.
Q: What if an average jumps after a long task? A: Completed samples update the recent mean; estimates change on subsequent refreshes.
Q: Why position ten can mean twenty-seven minutes at three minutes each? A: Nine people are ahead; position and ahead differ by one.
Q: Is ETA guaranteed? A: No; staffing, task variation, absence and model limitations can change it.

## 34. Submission checklist and study allocation
Each teammate should understand the overall flow, then own a module walkthrough: student UI, staff UI, queue/service backend, or database/analytics/deployment. Never limit preparation to the branch name; main contains the integrated version, and judges may select any file.

Before presentation: pull main; confirm both URLs and cloud connection; verify staff assignments; open the backend early; select a safe test student; keep a scratch SQL schema ready; check the actual campus time. If outside open hours, demonstrate prebooking and explain the schedule rather than silently changing production rules.

Show no secret .env or password value on a projector. Bring these PDFs, the repository, a known successful API response, local test evidence and a backup set of screenshots. A backup screenshot is evidence of an earlier run, not a claim that the live service currently works.

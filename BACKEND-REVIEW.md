Update 7 October 2026: this is the historical integration review. Daily reservations, persisted counter ownership/availability, and staff-confirmed missed turns supersede the earlier slot/assignment limitations below. See DAILY-RESERVATIONS.md and PROJECT-PLAN.md for current behavior and remaining issues.

# Team backend review and student integration

Reviewed on 6 October 2026 after fetching `origin`. This is a local source review of the supplied repository, not a review of a particular pull request or a claim that all CI checks passed.

## Sources and integration baseline

| Branch | Reviewed commit | Responsibility |
| --- | --- | --- |
| queue-backend | `620ce80cbfd7d586835682256332e58e93938545` | Student DAO/service, queue state transitions, counters |
| staff-module | `1d1ab58e6ab4b7ca3a9622523f9963c5c71f1775` | Staff authentication/dashboard, merged queue and smart module |
| smart-module | `ca4bcce7dc83bbf5c9092d63aebabf2556bd38cd` | Bookings, no-show detection, wait estimates, analytics |

The staff-module branch already integrates the queue and smart modules. Its Java sources and staff/booking JSP assets are the baseline copied into the local student-ui working tree. Team service methods and staff endpoints remain responsible for business operations. Changes are local and uncommitted; no branch has been pushed or merged remotely.

## The service count is database-defined

All three branches contain table definitions but no `INSERT INTO services` seed. `ServiceDAO.getAllServices()` reads the database, and `/api/services` returns those rows. The booking JSP tries that same database query before displaying fallback options:

1. ID 1: General Inquiries
2. ID 2: Fee Payment
3. ID 3: Document Verification

Those are fallback labels, not proof of actual IDs, descriptions, durations, or the service count on a teammate's device. The proposed eight-service SQL pasted in chat was newly suggested data, not repository evidence. The user subsequently supplied and approved three local records: General Inquiries (10 minutes), Fee Payment (15 minutes), and Document Verification (20 minutes), with their descriptions. They are stored in `database/seed_services.sql` and inserted locally. Actual IDs are allocated by MySQL, rather than assumed to be 1–3. Re-running the script skips existing service names.

The initializer no longer invents service records. The four exact, unused UI-created rows were backed up to ignored `.runtime/restore-prior-ui-services.sql` and removed after checking that no queue, booking, staff, or counter referenced them. Existing team records are retained. The approved seed creates no staff accounts or counters, so wait estimates remain unavailable until actual counters are configured.

## Rules followed by the frontend

| Area | Actual team behavior | Student UI behavior |
| --- | --- | --- |
| Accounts | Student registration/login uses the students table | Real API authentication; session cookie; no local demo identity |
| Services | Database rows and configured average durations | Dynamic cards and booking choices; no four-service fallback |
| Tokens | Increasing token number within each service | Display the returned token; no fixed `024` or made-up prefix |
| Active queue | WAITING, CALLED, SERVING | Poll every 8 seconds while visible; allow viewing multiple service tickets |
| Progress | Staff call/start/complete/skip operations | No student “next turn” or local status changes |
| Queue position | One-based position includes the student | People ahead = max(position - 1, 0); zero ahead does not imply CALLED |
| Counter assignment | Queue table has no counter_id | Show service desk, without inventing counter 03 |
| Estimated wait | Rounded people-ahead × historical/configured average ÷ active counters | Use WaitingTimeService; no estimate when active count is zero |
| Bookings | Fixed 30-minute slots, 09:00–16:30, with 13:00 excluded | Request available slots; no arbitrary time picker |
| Capacity | One active booking per service and slot | Report backend conflicts |
| Duplicate booking | Student cannot book another service at the same date/time | Report backend validation |
| Cancellation | Only BOOKED entries, with student ownership when supplied | Cancel owned bookings using the session identity |
| No-shows | Default 5-minute grace; checked on booking requests | Show NO_SHOW status and arrival guidance |
| Unsupported features | No reschedule, profile update, queue cancellation, notification delivery endpoints | These controls are removed rather than simulated locally |

The new `StudentPortalServlet` is an adapter under `/api/student/*`; it calls StudentService, ServiceDAO, CounterDAO, WaitingTimeService, BookingService, and NoShowService. It handles session identity, JSON encoding, combined page data, and validates that requested services/time slots exist. It does not replace the staff workflow or introduce new booking rules.

## Review findings and remaining limitations

### P1 — Concurrent queue joins can allocate the same token

`QueueDAO.joinQueue` reads MAX(token_number) + 1, then inserts without serializing the allocation or enforcing a unique constraint. Two simultaneous joins can obtain the same token. `callNext` likewise selects without a row lock, so two staff requests can select the same waiting entry. Sequential integration tests pass; these methods need a team-reviewed transaction/locking change before concurrent deployment. The frontend cannot guarantee uniqueness.

### P1 — Booking capacity checks are not atomic with insertion

`BookingService.createBooking` checks duplicate/capacity using separate connections before insertion. Two simultaneous requests can both pass. Add transactional locking or database constraints that correctly accommodate cancelled/no-show records. Sequential conflict behavior was verified; concurrent booking integrity is not established.

### P1 — Personal legacy endpoints accepted caller-supplied identity

Queue status/join and booking status/cancel previously trusted studentId. `cancelBooking` even permits zero to bypass ownership. The new portal always derives identity from a student session, and `StudentOwnershipFilter` protects the legacy personal routes. Staff booking list access now requires a staff session. This does not amount to a complete security audit of every legacy JSP or staff endpoint.

### P2 — Pause status and wait estimates disagree

StaffServlet deliberately keeps PAUSED counters active in the database and stores pause state only in the staff session. CounterDAO counts is_active=true, so wait estimates include paused counters. This is retained team behavior; share the finding with the staff/smart-module owners before changing the availability model.

### P2 — A called student has no persisted counter assignment

`call-next` returns counterId to staff, but QueueDAO does not store it. The student status endpoint therefore cannot reliably recover the assigned counter, particularly across multiple counters. The UI must not infer one. Supporting directions to a specific counter requires a backend/schema agreement.

### P2 — DAO failures often look like empty data

Many team DAO/service methods catch database exceptions and return an empty list, false, null, or a fallback average. `getAvailableSlots` may expose slots after its database query fails. The portal checks database connectivity first and returns explicit 503 for that failure, but failure between the check and a swallowed DAO exception remains possible. Propagating typed failures would give stronger guarantees.

### P2 — Appointment attendance is not connected to queue service

The booking module provides create/cancel/status and automatic NO_SHOW, but no appointment check-in or completion flow connected to staff queue operations. A BOOKED appointment can become NO_SHOW after the grace period unless another module changes its status. The UI does not invent a check-in button.

### P2 — Authentication has mixed legacy behavior

StudentDAO now hashes newly registered student passwords with PBKDF2 and migrates legacy plaintext student passwords on successful login. StaffDAO still uses the team's existing password flow. The original StudentServlet login does not establish the new portal session; the React app uses `/api/student/login` instead. Legacy clients should migrate or share the session contract explicitly.

## Runtime and verification

React/Vite serves the frontend independently. Vite proxies `/api` to `http://127.0.0.1:8081`, configurable through `QUEUE_BACKEND_URL`. Tomcat runs the Java backend and the team's JSP modules in an isolated `.runtime/tomcat` base. Local database credentials are outside the WAR and ignored by Git. For deployment, configure an equivalent same-origin `/api` reverse proxy; Vite's development proxy is not a production web server.

Verified locally: Maven WAR build; Vite production build; live Vite proxy; 29 integration assertions for session/authentication, real queue joins, staff call/start/complete and student state updates, actual wait computation, duplicate active tickets, fixed available booking slots, capacity conflicts, owned cancellation, released slots, invalid slots, rejected unsupported rescheduling, logout, and incorrect/correct passwords. All test fixture records were removed. These checks do not cover concurrency, a teammate's production database, or every staff UI interaction.

Browser verification also completed real registration, authenticated dashboard rendering, motion pause, sign-out, and persistence of sign-out after reload. The temporary browser test account was removed. Repeat API checks with `node scripts/tests/integration-test.mjs` from the repository, with Vite, Tomcat, MySQL, and `target/dependency` available. The test creates an isolated service/counter/staff and two student accounts, then removes only those fixture records. `QUEUE_FRONTEND_URL` and `JAVA_HOME` can override local paths.

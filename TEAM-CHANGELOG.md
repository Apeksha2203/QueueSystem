# Team change log — 7 October 2026

All work is local. No teammate branch has been force-pushed or overwritten remotely. Review the current diff before merging. This document describes final behaviour, not a complete audit certification.

## Student frontend (QueueSystemFrontend)
Preserved warm visual identity, added responsive public landing and authentication links, clearer page titles, virtual queue illustration, and service-card booking previews. Exploring services does not reserve a ticket. Confirmation creates today's reservation. Slot/date selection removed. Queue view supports confirmation-based cancellation of waiting tickets. Position, estimates, missed turns and closing explanations use backend data.

## Java backend and database
Added transactional service locking, stable tokens separate from daily order, service/counter/staff assignment, queue reservation metadata and events. First staff-confirmed missed call moves behind up to ten waiting students; second ends the ticket. Shared hours are 10–13 and 14–15 Asia/Kolkata. Early booking allowed. Closing risk prevents new reservations; waiting tickets close without an absence penalty, assigned students may finish. Closing reconciliation runs every ten seconds and after relevant reads. Password hashing and session renewal added.

Database migration: database/migrations/001_daily_reservations.sql is additive and rerunnable. It retains legacy appointments and adds queue_reservations, queue_events and counter_states. Run scripts/initialize-db.ps1 after configuring local credentials. The service seed contains the three agreed services. No new database migration is needed for this staff-controls batch.

## Staff portal batch
frontend/src/App.jsx restores the authenticated staff profile from the server on reload. Backend logout invalidates the session. Network/server failures show a retry state; anonymous profile requests return to login.
frontend/src/components/QueueTable.jsx shows Start, Complete and Confirm missed call only for tickets assigned to this staff member and counter. Removed arbitrary row calling; use Call Next to preserve FIFO. Token display no longer adds an invented A prefix.
Removed Skip Student from the React portal. /api/queue/skip and /queue/skip now return 410. Integrations must use /queue/no-show for a CALLED absent student, subject to ownership checks. Old internal DAO methods remain for a later legacy-code audit.

## Local testing and deployment
config/db.local.properties and config/test-clock.local.properties are ignored, never commit them. scripts/set-test-clock.ps1 freezes today's local campus time; -Disable restores real time. Test operations write real DB records. Before deployment follow PRE-DEPLOYMENT.md. Local student server: 5173; staff React: 5176; Tomcat API: 8081.

## Still pending
Admin account/role/assignment portal, service-scoped historical analytics, complete legacy endpoint audit, restart/failure/role-matrix UI testing, configurable hours. Existing tests cover reservation rules; passing them does not establish whole-project readiness. See DAILY-RESERVATIONS.md and PROJECT-PLAN.md.

Call Next is disabled when no students wait, the counter is unavailable, or this counter already has an assigned student. Backend transactional checks remain authoritative.

Missing staff sessions now return HTTP 401 consistently, enabling reliable frontend session recovery.

Validation for this staff batch: backend WAR and staff production build passed; 19 live API checks passed, including server session restoration, logout invalidation and retired skip. Temporary fixtures removed. Full interactive staff role-matrix testing remains pending.

Staff analytics now scope metrics to the assigned service and campus service day. Average wait/service durations use completed tickets only, exclude invalid timestamp order and return null when no samples exist. No-shows count NO_SHOW queue tickets, not legacy skipped/cancelled/unserved records. Database failures return 503 instead of fabricated zero values. Average wait is elapsed booking-to-start time (including pre-opening/lunch wait), not a future ETA.

Both /analytics and /api/analytics now require a staff session and use that staff member's assigned-service daily summary. Direct anonymous access returns 401. Isolated AnalyticsChecks.java validates exact 10-minute wait, 5-minute service time, NO_SHOW counts, and cancellation/prior-day exclusion.

Student registration now requires a valid Indian mobile number (10 digits starting 6–9, optional +91), validates email syntax and field limits, and requires a 12–128 character password. Existing password login remains compatible. Phone column already exists; no migration. Staff's service-scoped queue now includes student name, email and phone. Existing accounts without phone remain readable.

Validation: backend and both frontend builds passed; 26 live API checks passed, including rejection of malformed phone/email and short passwords, and staff visibility of name/email/normalised phone. Test records removed. Validation checks syntax only; phone/email ownership verification is not implemented.

Password policy updated at user request: minimum 6, maximum 128 characters, at least one ASCII letter and one digit; matching frontend and backend checks. Supersedes the earlier 12-character policy.

Unified student/staff/landing/login branding with a shared custom SVG CQ symbol and stacked Campus / Queue wordmark, replacing inconsistent text logos. SVG is code-native and included in each app's public assets.

Service-card Make booking action is now an inset full-width button-style label (card remains one accessible button). Card durations fetch measured per-service processing averages; measurements use staff Start-to-Complete timestamps and exclude scheduled closed periods, with configured defaults when no usable samples exist. Prebooking creation time never contributes to service duration.

### Service-card and booking-preview alignment
- Centered service-card booking buttons by removing the inherited left margin and using a padded button height.
- Explore services now opens Overview and scrolls to the services section.
- Aligned booking-preview values under labels with equal reserved height.
- Verified processing-only estimates and the 08:00 prebooking / 10:03 second-position example; frontend production build passed.

### Staff telephone contact and absence wording
- Renamed Call Next to Summon next student and absence controls to Mark absent.
- Added student phone links with accessible telephone icons in the live queue and current-student panel. tel links open the device calling handler and do not change ticket status.
- Existing backend first/second missed-turn rules remain authoritative; ending a ticket does not permanently ban the student.

### Demonstration staff accounts
- Updated the three existing local staff emails to general@staff.cq, fees@staff.cq, and document@staff.cq with user-selected demo passwords stored as hashes.
- Preserved service and counter assignments. Credentials remain in ignored local runtime configuration; no admin account was created.

### Continuous counter workflow
- Completing service automatically assigns the next waiting ticket in the same transaction during the same operating session, unless the counter is paused or hours are closed.
- Morning and afternoon summons are recorded in queue_events; crossing lunch requires an explicit summon to start the afternoon session.
- Mark absent retains automatic advancement and existing first/second-miss policy. Summon button is hidden while this counter has an assigned student.
- Empty queues still require a summon when a new student arrives.

### Local long-queue demonstration fixtures
- Added scripts/SeedDemoBookings.java and seeded 25 labelled demo students/reservations per service for 2026-10-07 (75 total). Existing records are preserved.
- Seeder is idempotent per service/student/day. Demo emails use example.invalid and no real telephone numbers are assigned.
- These load-test fixtures bypass normal capacity admission deliberately so closing-risk and grouped queue visuals can be exercised. Normal booking rules remain unchanged. Remove demo fixtures before production data migration.

### Clear student queue guidance
- Replaced grouped-animation implementation text with student ID and assigned-counter guidance.
- Ticket status now clearly distinguishes booking confirmed, student called, and service in progress.

### Next-day carryover and measured ETA
- Supersedes the previous closing-expiry policy: waiting tickets retain tokens, missed counts and FIFO order when moved to the next service day at 10 AM. Carryovers precede new reservations.
- Added repeatable migration 002_processing_duration.sql. Actual start-to-completion duration is recorded within scheduled processing windows, accounting for the local testing clock offset and retaining seconds.
- ETA uses the latest 20 completed processing samples per service, with configured duration only as a no-sample fallback; early booking time is excluded.
- Fixed staff activity status labels so closed-unserved tickets are not reported as completed.
- Student tickets show rescheduled dates. 63 updated queue checks, processing checks and student build passed.
- Historical recovery is limited to the labelled 2026-10-07 demo fixtures; other historical records are preserved.

- Verified recovery: 50 labelled demo tickets now waiting on 2026-10-08 (25 Fee Payment, 25 Document Verification). General Inquiries completed fixtures remain completed; measured processing average is about 0.296 minutes.
- Upcoming bookings includes carryovers, and future carried tickets remain visible in the student ticket view.

### Dynamic service-card averages
- Service cards show measured average processing time per student, using seconds for sub-minute durations.
- Student data refreshes every eight seconds and immediately when the browser tab becomes visible/focused; requests bypass browser cache.
- Services with no completed processing samples retain their configured initial estimate. Updated landing closing guidance to reflect carryover.

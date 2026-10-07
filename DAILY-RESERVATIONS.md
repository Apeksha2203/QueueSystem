# Today's queue reservations

Student: http://127.0.0.1:5173/#/bookings. Staff: http://127.0.0.1:5176/.

All three services currently share the agreed Asia/Kolkata schedule: 10:00–13:00 and 14:00–15:00, with a 13:00–14:00 break. Reservations are for the current service day only, including before opening and during lunch. No date or time-slot selection remains. At 15:00 new reservations and calls stop; an already assigned student can still finish.

The preview shows projected position, people ahead, and estimated service time. Confirmation recalculates under a service-row database lock. Numbers can change between preview and confirmation. Estimates before opening assume existing counters are staffed; during open hours, zero active counters gives an unknown estimate rather than a false zero. Lunch is excluded from service duration. If a calculated start reaches closing, additional places are refused. Unknown counter availability allows a reservation during open hours without promising a time.

Ticket numbers stay stable and queue order is stored separately. Calls assign both counter and staff. Operators cannot hold two current students or start/complete another operator's student. A called student may be confirmed absent by staff: first miss moves behind up to ten waiting students; second miss ends the ticket. The next present student is called within the same transaction when the counter is available and hours permit. A lone absent student is not immediately recalled. Each change records a queue event. There is no automatic absence countdown.

Previous-day entries are excluded from the active queue. Existing slot appointments are retained in the database as legacy history, with new creation and slot listing retired; they are not automatically marked absent on page visits. The student visits page lists queue reservations. Staff bookings now shows today's active reservations. Full legacy-appointment archive UI and admin configuration are separate remaining tasks.

## Setup and migration

Run scripts/initialize-db.ps1 on a teammate's machine after configuring local DB credentials. It applies database/migrations/001_daily_reservations.sql. This migration is additive and rerunnable: it adds daily order/assignment metadata and events, then backfills existing queue records without deleting data. Deploy the newly built WAR only after the migration succeeds. Startup does not silently apply schema changes.

## Validation

Backend WAR and both frontend builds passed. scripts/tests/DailyReservationChecks.java covers 60 isolated database and schedule checks, including a controllable clock for early booking, lunch, closing, missed turns, short queues, concurrency, cancellation and ownership. scripts/tests/reservation-api-test.mjs checks the current live service through the student Vite proxy, using temporary fixtures cleaned up afterward. The old fixed-slot test suite predates this policy and is not the current reservation acceptance suite.

The opening schedule is centralised in ServiceHours.java; an admin hours editor is not yet implemented. Estimates are approximate and do not predict staff lateness, varying student service duration, or future absence. Global historical analytics still require a separate service-scoping improvement.

## Closing protection

A projected turn at or after 3 PM shows an unlikely-to-be-served warning and refuses new reservations. Existing tickets remain active until actual closing; estimates update as the queue advances. Lunch moves estimates past 2 PM within the same day. At 3 PM, waiting tickets become CLOSED_UNSERVED with no additional missed-turn penalty. Called and serving students can finish. A Tomcat listener reconciles every 10 seconds and catches up after restart; portal reads also reconcile due tickets. Students reserve a new ticket tomorrow; places are not carried over automatically.

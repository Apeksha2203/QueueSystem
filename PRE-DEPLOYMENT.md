# Before pushing or deploying

Local manual testing uses scripts/set-test-clock.ps1 (default 10:15 today). Change the clock to 07:30 for prebooking, 13:30 for lunch, 14:45 for closing warnings, or 15:00 for closing. The date remains the real campus date. Both student and staff operations use this clock; bookings and counter actions still write real database records. This is a frozen clock, not elapsed simulation.

Run ./scripts/set-test-clock.ps1 -Disable when testing is finished. Returning to real time after 3 PM moves waiting tickets to the next service day under the current rollover policy. Keep identified demo records out of production imports.

Before deployment:
- Disable the local clock and confirm config/test-clock.local.properties is absent on the deployment machine.
- Use a separate test database for destructive workflow tests and remove only identified test records.
- Run database migrations and the reservation database/API checks; build backend and both frontends.
- Confirm real hours, authentication and service/counter assignments.
- Review git status for credentials and test data. The clock file is ignored and never included in the WAR; no production source edit is required to restore real time.

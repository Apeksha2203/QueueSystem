# CampusQueue student interface

The canonical source is QueueSystemRepo/QueueSystemFrontend on student-ui. The outer QueueSystemFrontend folder mirrors it for the existing local development setup.

Run npm run dev in the frontend folder. Run scripts/start-backend.ps1 from the Java repository to start the team backend on port 8081. Vite proxies /api to that backend; QUEUE_BACKEND_URL overrides its destination. npm run build builds the frontend independently of Tomcat. Configure the equivalent /api reverse proxy when deploying the static production bundle. The optional build:tomcat command retains the old copy workflow.

The original cream/chocolate palette, orange/red glows, rounded panels, bundled Outfit/Inter fonts, and floating login labels remain. Georgia italic adds expressive headings. The two featured panels lead into the dynamic service cards. A custom SVG virtual queue replaces the campus map: YOU is behind the people ahead and moves toward the service desk as the actual ticket position changes. Gentle ambient motion does not advance queue state. Motion can be paused and respects reduced-motion preferences. Dialogs use native modal focus behavior.

Accounts, tickets, and bookings now use the Java backend and MySQL. The frontend stores only appearance locally; previous cq-profile, cq-ticket, and cq-bookings demo values are ignored. There is no sample catalogue fallback. Services come from the database, and service durations are labeled separately from actual queue wait estimates. Booking choices use the team's available fixed slots. Staff determine queue progress. Rescheduling, student queue cancellation, profile editing, and notification controls are absent because the team backend has no supporting operations.

Read ../BACKEND-REVIEW.md for branch provenance, business rules, review findings, and verification limitations. Maven packaging, Vite build, proxy access, and 29 live integration checks passed. The integration checks created and removed isolated test records; they do not establish concurrent transaction safety. Oxlint reports two React effect warnings for asynchronous API synchronization, with no errors.

Changes remain local and uncommitted. No remote branch has been pushed or merged.
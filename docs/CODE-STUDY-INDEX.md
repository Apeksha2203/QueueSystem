# Commented code for presentation and viva

The source now includes file-purpose comments marked VIVA GUIDE and explanations beside methods, state, API requests and important business rules. Comments explain the implementation; authentication and queue rules still run on the backend.

## Where to begin

- Student: QueueSystemFrontend/src/main.jsx -> App.jsx -> student-api.js -> queue-layout.js. Current screens are inside App.jsx. Retained pages/components are labelled as earlier implementations.
- Staff: frontend/src/main.jsx -> App.jsx -> pages/StaffLogin.jsx or StaffDashboard.jsx -> services/api.js -> reusable components.
- HTTP: src/main/java/com/queue/controller/ maps servlet URLs, checks sessions and translates service results into HTTP/JSON.
- Core rules: service/ReservationService.java handles transactional reserve, staff operations, missed turns, cancellation and closing carryover. ServiceHours.java defines campus time and operating windows; WaitingTimeService.java handles processing samples and estimates.
- Storage: dao/ binds SQL and maps result rows. model/ carries fields via getters/setters. util/ contains connection configuration, password hashing, input validation, JSON and transaction locks.
- Database: database/schema.sql defines base tables; database/migrations/ extends service-day and processing fields; seed_services.sql provisions initial service data.
- Setup/deployment: Dockerfile, pom.xml, Vite/Netlify configurations and scripts/ explain build and startup paths. They do not run merely because they exist in Git.
- Tests: scripts/tests/ and Java diagnostic files are explicit checks; Maven packaging does not automatically run every standalone check. Legacy fixed-slot checks are identified separately.
- Older JSP/JS: src/main/webapp/booking, staff and js contain retained browser/server-rendered flows. Do not describe them as the current React booking algorithm.

## Formats without inline comments

JSON must stay valid JSON. These files are explained here rather than adding unsupported comments:

- QueueSystemFrontend/package.json: student runtime dependencies (React, React DOM, React Router, Lucide, GSAP and declared Phosphor), development tools (Vite, React plugin, Oxlint and React types), and dev/build/build:tomcat/lint/preview scripts.
- frontend/package.json: staff dependency and build/dev commands; inspect dependencies separately from the student project because the bundles differ.
- Both package-lock.json files: machine-maintained exact dependency trees/integrity values. Use the manifests to explain the tool choices; avoid editing lockfiles for prose.
- QueueSystemFrontend/.oxlintrc.json: lint configuration, not runtime UI logic.

Generated src/main/webapp/student/*.html files are entry-page outputs created by QueueSystemFrontend/scripts/sync-tomcat.mjs. Study/comment the generator and React source rather than altering outputs that the next build replaces. target/, dist/, caches, node_modules and local runtime/config files are generated, downloaded or private and are not annotated.

SVG images/logos, PNG images, fonts and font licences are assets rather than application logic. index.html and the CSS explain how icons/fonts are loaded. The existing PDF guides and Markdown documentation already explain the project; this comment pass does not change those source snapshots or their line references.

Git ignore files decide which outputs/secrets stay out of the repository. .dockerignore excludes local files from the cloud build. .gitattributes treats PDFs as binary to prevent line-ending conversion from damaging them. These small repository-control files retain their existing patterns.

## How to answer a random code question

1. Read the file-purpose comment to identify the layer and caller.
2. Find the method/component comment and explain its inputs, output and responsibility.
3. Trace one important condition, request or SQL statement through the block.
4. State which layer enforces the rule. A disabled button does not enforce security; a layout coordinate does not set queue priority.
5. Distinguish current, retained and generated code before explaining the user flow.

Opening hours remain 10:00-13:00 and 14:00-15:00 Asia/Kolkata. The local test clock remains disabled. This is a comment-only source change; no business rule or credential was changed.

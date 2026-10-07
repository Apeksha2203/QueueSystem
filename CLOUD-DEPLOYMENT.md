# GitHub deployment

## Database
Run schema.sql, migrations 001 and 002, then seed_services.sql on the Railway public connection. Use the campus_queue database, not the default railway database. Local and cloud databases are separate; this setup starts with empty queues.

Generate the ignored cloud staff seed with python scripts/create-cloud-staff-seed.py. Open the resulting .runtime/cloud-staff-seed.sql in the cloud Workbench connection and run the entire script. Passwords are hashed; existing accounts are not overwritten.

## Render backend
Create a Web Service from Apeksha2203/QueueSystem, branch main, runtime Docker, repository root, Dockerfile path Dockerfile. Set PORT=8080, QUEUE_DB_USER and QUEUE_DB_PASSWORD from Railway. Set QUEUE_DB_URL to jdbc:mysql://PUBLIC_HOST:PUBLIC_PORT/campus_queue?connectionTimeZone=Asia/Kolkata. These values belong in Render environment settings, never Git. Configure transport security according to the database provider's supported TLS configuration. Do not set QUEUE_DB_CONFIG or test-clock variables in hosting.

After deployment, /api/services should return the service catalogue. Check logs for database connection failures. The image builds Java 17 and runs the WAR on Tomcat 10.1; the local runtime, test clock and credentials are excluded from the Docker context. Tomcat's shutdown listener is disabled so hosting port detection sees only the HTTP connector on 8080. Container termination uses process signals with catalina.sh run.

## Two Netlify sites
Connect the same GitHub repository and main branch for both sites. Student base directory: QueueSystemFrontend. Staff base directory: frontend. Each directory has netlify.toml with build and publish settings. Set QUEUE_BACKEND_URL to the backend HTTPS origin (for example https://YOUR-SERVICE.onrender.com) on BOTH sites before building. The build generates an /api proxy before the app routing fallback. This keeps browser requests and session cookies on each frontend's own origin.

## Validate before sharing
Register a fresh student, log in, preview and reserve a service. Confirm the row in cloud queue and queue_reservations. Log into the corresponding staff site, summon, start and complete the student; verify student polling and cloud status updates. Verify the other staff accounts cannot operate that service. Test cancellation, both absence attempts, lunch and closing rollover. Verify login persists through refresh on both sites. Do not import local demo queues merely to make the cloud look populated.

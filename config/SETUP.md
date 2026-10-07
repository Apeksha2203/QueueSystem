# Local MySQL setup

Install MySQL Community Server 8.4 LTS using the Windows MSI. In MySQL Configurator, keep TCP port 3306, enable the Windows service, and choose your own root password. MySQL Workbench is optional.

From the repository directory, run:

```powershell
./scripts/configure-db.ps1
```

Enter the password in the hidden local prompt. The generated `config/db.local.properties` is ignored by Git. It contains a local credential, so do not share it or commit it.

Then run `./scripts/initialize-db.ps1` to verify the connection and create the database and tables. It does not invent service records. Obtain the team's services/counters seed or approve a local catalogue separately. Existing data is retained. Staff accounts and active counters must be configured separately using the team's backend workflow. The initializer uses the JDK and Maven bundled in the workspace; other checkouts can pass `-JavaHome` and `-MavenCommand`.

The user-approved three-service catalogue is in `database/seed_services.sql`. Execute it after the schema to add General Inquiries (10 minutes), Fee Payment (15 minutes), and Document Verification (20 minutes). It skips names already present and does not reset IDs or change existing records. The frontend uses the IDs returned by the database.

Before starting the backend, set:

```powershell
$env:QUEUE_DB_CONFIG = (Resolve-Path ./config/db.local.properties).Path
```

Alternatively, set `QUEUE_DB_URL`, `QUEUE_DB_USER`, and `QUEUE_DB_PASSWORD` in the backend process environment. Environment values override the configuration file. Do not paste passwords into chat or include them in shell command arguments.

Each teammate can keep a separate local database configuration. Vite will call the Java backend through its `/api` proxy; the browser does not connect directly to MySQL.

Start the backend with `./scripts/start-backend.ps1`. From `QueueSystemFrontend`, run `npm install` and `npm run dev`. The current workspace frontend is available at `http://127.0.0.1:5173`. The backend WAR excludes the React assets; `npm run build` builds only the frontend. `npm run build:tomcat` is an optional legacy copy command, not required for the independent frontend.

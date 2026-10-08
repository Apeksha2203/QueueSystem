# Presentation demo data

`database/presentation_demo.sql` is an explicit sample-data operation, separate from application deployment. Pushing it to GitHub does not run it against Railway.

The supplied batch is for 7–8 October 2026 in Asia/Kolkata. It creates 12 completed tickets yesterday and six waiting bookings today for each of General Inquiries, Fee Payment and Document Verification. Completed processing times are shuffled 1, 3 and 4 minutes, recorded both in timestamps and `processing_seconds`. The sample mean for each service is 2.667 minutes. Actual ETA may also include other recent completed records because the existing algorithm uses the latest 20 samples, people ahead and active counters.

Every sample student has a `[DEMO]` label and an `example.invalid` email. Generated accounts have unknown random passwords. These are demonstration fixtures, not actual student visits. Today's reservations append to the existing daily order without changing staff availability or operating hours.

Cleanup replaces only this presentation batch and yesterday's earlier `demo-queue-YYYY-MM-DD-*` students' labelled queue rows, including those already carried over to today. Yesterday's legacy bookings for those earlier demo students are also removed. Real student records are preserved. Dependent reservation/event records are removed by their existing foreign-key cascades.

## Run against the intended database

Save a connection properties file locally with `url`, `user` and `password`; `config/db.cloud.local.properties` is ignored by Git. Use the Railway public MySQL host and port in the JDBC URL, with the `campus_queue` schema. Never put credentials into this SQL file or Git.

From the repository root, set `QUEUE_DB_CONFIG` to the intended properties file and use Java 17:

```powershell
$env:QUEUE_DB_CONFIG = (Resolve-Path 'config/db.cloud.local.properties').Path
java -cp 'target/classes;target/dependency/*' scripts/LoadPresentationData.java --dry-run
java -cp 'target/classes;target/dependency/*' scripts/LoadPresentationData.java --apply
```

The dry run executes the changes inside a transaction and rolls them back. The apply operation writes a JSON snapshot of existing labelled demo queue records, reservations, events and legacy bookings into ignored `.runtime/deployment/`, then commits the replacement together. It validates the completed duration fields before committing.

Alternatively, open the SQL file in MySQL Workbench, select the Railway connection, and execute it. The SQL itself includes a transaction but this manual route does not create the loader's JSON backup.

To prepare another day's batch, run `python scripts/create-presentation-seed.py` first. Dates come from real Asia/Kolkata time. Re-running today's batch replaces its own sample tickets, including any processed during a rehearsal; use it intentionally.

## Presenting the estimate

Explain: “Yesterday's labelled demo completions provide measured processing samples. The application calculates the average from recent completed tickets, then combines it with queue position and active service counters.” Do not describe fabricated demo samples as real historical usage.

The data operation does not mark real queues complete, change the service schedule, or enable the test clock.

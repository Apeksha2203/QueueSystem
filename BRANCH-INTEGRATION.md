# Integrated release and branch policy

Reviewed on 2026-10-08 against the fetched GitHub branches.

The integrated release includes the student React frontend (`QueueSystemFrontend/`), staff React frontend (`frontend/`), Java backend (`src/main/java/com/queue/`), database schema/migrations (`database/`), local setup scripts, tests, and teammate change notes.

## Branch review

- `staff-module`: staff React dashboard, servlet/API integration, counter status and analytics. Its files exist in the integrated release; the current versions add authentication, scoped queues, contact details, absence handling and measured estimates.
- `queue-backend`: original queue/student/staff APIs. Retained in the integrated codebase, with current operations delegated to the transactional daily reservation service.
- `smart-module`: original wait estimation, fixed-slot booking, no-show and analytics logic. Current daily FIFO, carryover and measured processing logic supersede the old fixed-slot workflow. Legacy files remain identifiable for reference.
- No branch-only file was absent from the integrated release in the comparison. Branch histories are merged while retaining the validated integrated file contents, rather than reintroducing superseded behavior.

`main` is the integration/deployment branch. At this release, the four module branches are advanced to the same integrated revision through normal fast-forward pushes. Previous commits remain in history; future module work should branch from current main and return through review.

Git branches are development snapshots, not application roles. Staff permissions come from database service/counter assignments and server-side session checks.

This integration does not publish the website, configure a cloud database, or add the pending admin portal or master study PDF.

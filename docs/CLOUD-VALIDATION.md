# Cloud smoke validation - 8 October 2026

Verified using HTTP sessions and Playwright browser navigation:
- Student homepage and staff homepage: HTTP 200.
- Render /api/services: three configured services returned.
- Both Netlify /api/services rewrites: same cloud catalogue returned.
- General, Fee Payment and Document Verification staff logins: successful, with matching service/counter IDs 1, 2 and 3.
- Staff profile, queue and dashboard-summary requests: successful after login, proving proxy session persistence. Test staff sessions logged out afterward.
- Student signup through the deployed browser form: reached /#/dashboard.
- Test student API login and /session: successful.
- Student overview: all three services, no existing active ticket.
- At real campus time about 03:25, General Inquiries preview: prebooking enabled, projected position 1, zero ahead, estimated service time 10:00, testClock=false.
- One reservation: token 001, queue ID 1, then successful immediate cancellation.

The labelled test student deployment-check-20261008@example.invalid and its cancelled ticket remain as an identifiable audit record. The synthetic number was not called. Test passwords are not included here.

No live staff start/complete/absence actions were tested outside operating hours. Full cloud concurrency, lunch, closing and midnight validation remains to be scheduled. Prior local standalone checks cover many of these rules; they are separate from this cloud smoke run.

Local test-clock override has been removed. The cloud Docker context never included it. Final hours remain 10:00-13:00 and 14:00-15:00 Asia/Kolkata; same-day prebooking before opening remains supported.

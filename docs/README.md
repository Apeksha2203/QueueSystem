# Study and presentation pack

Start with the presentation runbook for the demonstration sequence, then use the master guide for technical preparation.

- [Master PDF](../output/pdf/Campus-Queue-Master-Guide.pdf): architecture, modules, queue rules, tokens, measured ETA, SQL exercises, Java/React explanations, viva questions, deployment costs/troubleshooting, limitations, file inventory and numbered source appendix.
- [Presentation PDF](../output/pdf/Campus-Queue-Presentation-Runbook.pdf): ordered demonstration, suggested talk track, team handoffs, code/database tour, rehearsal questions and failure recovery.
- [Student frontend preparation PDF](../output/pdf/Campus-Queue-Student-Frontend-Preparation.pdf): personal speaking notes, packages, active and retained files, API flow, React concepts and viva answers.
- [Editable student frontend notes](student-frontend-preparation.md)
- [Editable master source](master-guide.md)
- [Editable presentation source](presentation-runbook.md)
- [Cloud validation evidence](CLOUD-VALIDATION.md)
- [Deployment instructions](../CLOUD-DEPLOYMENT.md)
- [Team changes](../TEAM-CHANGELOG.md)

The master appendix uses the source snapshot at generation time. Line numbers can change after edits. Earlier branches were synchronized, but newer deployment/documentation commits are on main; pull main for the final integrated version.

Regenerate with Python and reportlab:

```
python scripts/build-preparation-pdfs.py
python scripts/build-student-preparation-pdf.py
python scripts/check-preparation-pdfs.py
```

The second script also needs pypdf, pypdfium2 and Pillow; it extracts text, checks for empty pages/known credential values and renders contact sheets into ignored tmp/pdfs for visual inspection. Final PDFs stay in output/pdf and are committed. Do not put database passwords or shared login passwords into the preparation documents.

Original hours apply: 10 AM-1 PM, lunch 1-2 PM, open 2-3 PM, Asia/Kolkata. Local test clock has been disabled; cloud Docker excludes local config. Same-day prebooking remains enabled before opening.

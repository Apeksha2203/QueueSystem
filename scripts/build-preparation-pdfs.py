"""Build the two academic guides from prose plus the actual tracked source snapshot.
Requires reportlab and pypdf; rendering QA uses pypdfium2.
"""
from pathlib import Path
import re
import textwrap
import subprocess
from xml.sax.saxutils import escape
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.platypus import BaseDocTemplate, PageTemplate, Frame, Paragraph, Spacer, PageBreak, Preformatted, KeepTogether
from reportlab.platypus.tableofcontents import TableOfContents

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'output' / 'pdf'
OUT.mkdir(parents=True, exist_ok=True)
INK = colors.HexColor('#243445')
ORANGE = colors.HexColor('#e35d2f')
STYLES = getSampleStyleSheet()
STYLES.add(ParagraphStyle(name='BodyGuide', fontName='Helvetica', fontSize=10, leading=15, textColor=INK, spaceAfter=9))
STYLES.add(ParagraphStyle(name='GuideH1', fontName='Helvetica-Bold', fontSize=19, leading=24, textColor=INK, spaceAfter=14, keepWithNext=True))
STYLES.add(ParagraphStyle(name='GuideH2', fontName='Helvetica-Bold', fontSize=13, leading=18, textColor=ORANGE, spaceBefore=10, spaceAfter=8, keepWithNext=True))
STYLES.add(ParagraphStyle(name='GuideCode', fontName='Courier', fontSize=7.1, leading=10, textColor=INK, spaceAfter=10))
STYLES.add(ParagraphStyle(name='GuideSmall', fontName='Helvetica', fontSize=8.5, leading=12, textColor=INK, spaceAfter=7))

def ascii_text(value):
    replacements = {'\u2013':'-', '\u2014':'-', '\u2011':'-', '\u2019':"'", '\u2018':"'", '\u201c':'"', '\u201d':'"', '\u2026':'...', '\u2192':'->', '\u00a0':' '}
    for a,b in replacements.items(): value = value.replace(a,b)
    return value.encode('ascii', 'backslashreplace').decode('ascii')

def paragraph(text, style='BodyGuide'):
    text = escape(ascii_text(text))
    text = re.sub(r'(https://[^\s]+)', r'<link href="\1" color="#d34e26">\1</link>', text)
    return Paragraph(text, STYLES[style])

class GuideDoc(BaseDocTemplate):
    def __init__(self, filename, label, **kwargs):
        super().__init__(str(filename), pagesize=A4, leftMargin=48, rightMargin=48, topMargin=54, bottomMargin=48, **kwargs)
        self.label = label
        self.heading_number = 0
        self.addPageTemplates(PageTemplate(id='guide', frames=Frame(48,48,A4[0]-96,A4[1]-102,id='body'), onPage=self.decorate))
    def decorate(self, canvas, doc):
        canvas.saveState()
        canvas.setStrokeColor(ORANGE); canvas.setLineWidth(1)
        canvas.line(48,A4[1]-35,A4[0]-48,A4[1]-35)
        canvas.setFont('Helvetica',8);canvas.setFillColor(INK)
        canvas.drawString(48,A4[1]-28,'CAMPUS QUEUE | '+self.label)
        canvas.drawString(48,28,'Academic preparation | 8 October 2026 | Source snapshot on main')
        canvas.drawRightString(A4[0]-48,28,str(doc.page))
        canvas.restoreState()
    def beforeDocument(self):
        self.heading_number = 0
    def afterFlowable(self, flowable):
        if isinstance(flowable, Paragraph) and flowable.style.name == 'GuideH1' and self.page > 1 and flowable.getPlainText() != 'Contents':
            level = 0
            self.heading_number += 1
            key='heading-'+str(self.heading_number)
            self.canv.bookmarkPage(key)
            self.notify('TOCEntry',(level,flowable.getPlainText(),self.page,key))

def code_blocks(lines, numbered=False):
    result=[];wrapped=[]
    for i,line in enumerate(lines,1):
        line=ascii_text(line.rstrip()).replace('\t','    ')
        prefix=f'{i:4d} | ' if numbered else ''
        width=103 if numbered else 110
        pieces=textwrap.wrap(line,width=width,replace_whitespace=False,drop_whitespace=False) or ['']
        wrapped.append(prefix+pieces[0])
        wrapped.extend(('     | ' if numbered else '    ')+p for p in pieces[1:])
    for at in range(0,len(wrapped),44):
        result.append(Preformatted('\n'.join(wrapped[at:at+44]), STYLES['GuideCode']))
    return result

def prose(path, compact=False):
    lines=path.read_text(encoding='utf-8').splitlines()
    story=[];buffer=[];code=[];in_code=False
    def flush():
        if buffer: story.append(paragraph(' '.join(buffer))); buffer.clear()
    for line in lines:
        if line.startswith('```'):
            flush()
            if in_code: story.extend(code_blocks(code));code=[]
            in_code=not in_code;continue
        if in_code:code.append(line);continue
        if line.startswith('# '):continue
        if line.startswith('## '):
            flush()
            if not compact or not story: story.append(PageBreak())
            else: story.append(Spacer(1,14))
            story.append(paragraph(line[3:],'GuideH1'));continue
        if not line.strip():flush();continue
        if line.startswith('Q:') or re.match(r'^\d+\.',line):flush();story.append(paragraph(line));continue
        buffer.append(line)
    flush();return story

ROLES = {
 'ReservationService.java':'Core transaction coordinator. Study preview, reserve, operate, callNext, cancellation, availability and closing rollover. Follow the service lock and commit/rollback boundary.',
 'ServiceHours.java':'Campus-time policy and processing-window arithmetic. Study real/test time, open boundaries, next opening, lunch projection and interval overlap.',
 'WaitingTimeService.java':'Recent completed processing samples and estimate helpers. Distinguish measured average from configuration fallback and preview rounding.',
 'ClosingTimeListener.java':'Servlet lifecycle background reconciliation. A scheduled worker checks closing every ten seconds and is stopped on context destruction.',
 'StudentPortalServlet.java':'Current student HTTP adapter. Routes login/register/session/overview/preview/reserve/cancel; obtains identity from the server session.',
 'StaffServlet.java':'Staff HTTP adapter for login, profile, service queue, counter availability, summary and activity. Study service scoping and remaining legacy branches.',
 'QueueServlet.java':'Queue action HTTP adapter. Authenticates staff and delegates actions into ReservationService; retained routes require care.',
 'ServiceServlet.java':'Public catalogue adapter. Returns configured services; catches DAO failure as HTTP 503 and writes safe diagnostics to stderr.',
 'DBConnection.java':'Connection factory. Environment variables override an explicitly loaded local properties file. DriverManager opens the MySQL connection.',
 'TransactionLocks.java':'Database parent-row locks. SELECT FOR UPDATE serializes operations per service; locks depend on the surrounding transaction.',
 'StudentValidation.java':'Registration syntax and length rules. Validates name, email, Indian phone and user-requested password policy; normalizes input.',
 'Passwords.java':'Random-salt PBKDF2 hash/verification utility. Salt and derived key are Base64 encoded, not encrypted. Legacy migration support remains.',
 'Json.java':'Recursive JSON serializer for null, number, boolean, maps, lists and escaped string values.',
 'AnalyticsService.java':'SQL aggregation for dashboard reporting. Some elapsed-time metrics are different from ETA scheduled-window processing samples.',
 'StudentService.java':'Student business adapter around registration/login DAO operations and server input validation.',
 'StaffService.java':'Staff business adapter. Resolves login/profile and validates account/counter service assignments.',
 'ServiceService.java':'Thin catalogue adapter that delegates to ServiceDAO.',
 'BookingService.java':'Retained legacy fixed-slot booking logic. Do not present it as the current same-day reservation algorithm.',
 'NoShowService.java':'Retained older appointment no-show handling. Current queue absence rules are in ReservationService.',
 'BookingServlet.java':'Retained appointment HTTP surface. Current booking confirmation uses StudentPortalServlet and ReservationService.',
 'StudentServlet.java':'Older student HTTP surface. Compare with current StudentPortalServlet before claiming this is the active UI route.',
 'AnalyticsServlet.java':'Staff analytics request adapter. Explains how authenticated service-specific statistics are exposed.',
 'StudentOwnershipFilter.java':'Request filter protecting older endpoints using authenticated ownership/service checks. Not a substitute for current service validation.',
 'StudentDAO.java':'Student SQL storage/login operations, password verification and hash migration. Read PreparedStatement bindings and generated IDs.',
 'StaffDAO.java':'Staff SQL login/profile queries and password verification. Assigned service and counter originate from stored account data.',
 'ServiceDAO.java':'Catalogue SELECT and model mapping. Now throws on failure so the servlet does not fabricate an empty successful result.',
 'QueueDAO.java':'Retained queue read/DAO operations and student ticket projections. Current transactional writes are mainly in ReservationService.',
 'CounterDAO.java':'Counter lookup and state/storage operations. A counter belongs to one service.',
 'DatabaseSetup.java':'Explicit schema/migration runner for local provisioning. A deployment does not automatically execute these scripts merely by pushing Git.',
 'student-api.js':'Student fetch wrapper and shared backend hook. Includes credentials, error handling, polling and focused-tab refresh.',
 'queue-layout.js':'Pure animation layout helper. Groups large queues visually; it does not calculate authoritative server queue order.',
 'StaffDashboard.jsx':'Staff dashboard orchestration: polls endpoints, derives assigned current service, runs actions and refreshes UI state.',
 'StaffLogin.jsx':'Staff login form and error/busy handling. Uses the shared API adapter; it does not read MySQL directly.',
 'StudentPhone.jsx':'Validated telephone display and tel link. Device-mediated calling; no server telephony or queue mutation.',
 'QueueTable.jsx':'Queue rows and state/ownership-based action buttons. Server checks remain authoritative.',
 'StatCard.jsx':'Reusable summary card presentation; values come from the dashboard API.',
 'ActivityFeed.jsx':'Recent event/status list presentation. Explain display fields separately from database state transitions.',
 'netlify-redirects.mjs':'Build-time HTTPS backend-origin validation and API-first rewrite generation, followed by SPA route fallback.',
 'create-cloud-staff-seed.py':'Local staff provisioning generator using standard-library PBKDF2. Generated SQL stays in ignored .runtime; plaintext input is not committed.',
}

def role(path):
    name=path.name;rel=path.relative_to(ROOT).as_posix()
    if name=='App.jsx':return ('Current student component tree, landing/login and authenticated routes. Study BookingForm, Ticket, VirtualQueue and Experience.' if rel.startswith('QueueSystemFrontend/') else 'Staff BrowserRouter, restored login session, navigation and dashboard composition.')
    if name=='main.jsx':return 'Browser entry point: mounts the React app into the root element and imports styles. React StrictMode helps reveal development side-effect problems.'
    if name in ROLES:return ROLES[name]
    if '/model/' in rel:return 'Java data model: fields, getters and setters transport structured values between SQL mapping and application logic. It does not itself enforce all business rules.'
    if '/tests/' in rel or name.endswith('Test.java') or name.endswith('Checks.java'):return 'Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.'
    if '/pages/' in rel and rel.startswith('QueueSystemFrontend/'):return 'Older student page retained alongside the current App.jsx component implementation. Check imports before treating it as an active production screen.'
    if '/components/' in rel:return 'Reusable UI presentation component; check its imports/callers to establish whether the current App uses it.'
    if name.endswith('.css'):return 'Stylesheet controls appearance and responsiveness, not authorization or database state. The student currently imports original-identity.css; retained themes may be legacy.'
    if name.endswith('.sql'):return 'Database structure/migration/seed script. Read constraints, dependencies, repeatability and schema selection before executing.'
    if name.endswith('.jsp'):return 'Retained server-rendered JSP page. Tomcat compiles JSP into a servlet; the current primary student/staff dashboards use React instead.'
    if name.endswith('.ps1'):return 'Local PowerShell setup/start/configuration utility. Read path resolution and ignored config handling before execution.'
    return 'Supporting source or configuration. Check the entry point or caller, inputs, outputs and failure handling when explaining this file.'

def explain(line):
    s=line.strip()
    if 'FOR UPDATE' in s:return 'This SQL requests row locks. Explain which row is locked and where commit or rollback releases it.'
    if 'setAutoCommit(false)' in s:return 'Begins explicit transaction grouping; subsequent writes need a commit or rollback.'
    if '.rollback()' in s:return 'Discards uncommitted database changes after failure; avoids partial related-record updates.'
    if '.commit()' in s:return 'Persists the transaction together. Trace every related write that occurs before this boundary.'
    if 'PreparedStatement' in s or re.search(r'\.set(Int|String|Object|Boolean|Double)\(',s):return 'Creates or binds a parameterized SQL statement. Placeholder numbering starts at one.'
    if 'executeQuery' in s:return 'Executes a row-returning query and obtains a ResultSet that must be iterated and closed.'
    if 'executeUpdate' in s:return 'Executes a write and returns affected-row count; it does not alone guarantee the full business transaction succeeded.'
    if 'getGeneratedKeys' in s:return 'Obtains the database-generated key for the inserted row, used by its related reservation/event records.'
    if '@WebServlet' in s:return 'Registers this servlet URL mapping; explain which browser/API caller uses the route.'
    if 'getSession' in s or 'session.setAttribute' in s:return 'Uses server-side login state. Distinguish session identity from untrusted request parameters.'
    if 'getParameter' in s:return 'Reads client-supplied input; validation and authorization are needed before using it.'
    if 'useEffect' in s or 'setInterval' in s:return 'Synchronizes UI with polling or another side effect. Explain dependencies and timer cleanup.'
    if 'useState' in s:return 'Declares component state and a setter; changes cause React to render the relevant UI.'
    if 'fetch(' in s:return 'Sends an HTTP request. Explain method, credentials, body encoding and handling of unsuccessful responses.'
    if 'credentials:' in s:return 'Includes cookie credentials so the backend can restore the authenticated session through the proxy.'
    if 'pbkdf2' in s.lower() or 'PBEKeySpec' in s:return 'Password key derivation: random salt plus iteration count slows guessing; this is hashing rather than reversible encryption.'
    if 'try(' in s or 'try (' in s:return 'try-with-resources closes database/IO resources automatically when the block ends.'
    if 'catch' in s:return 'Failure path: explain whether it propagates, rolls back, logs safely, or risks hiding the error.'
    if 'return' in s and ('Map' in s or 'json' in s.lower()):return 'Builds the response consumed by another layer. Identify which fields the frontend renders.'
    return None

def tracked_files():
    git=['git','-c','safe.directory='+str(ROOT).replace('\\','/'),'ls-files']
    names=subprocess.check_output(git,cwd=ROOT,text=True).splitlines()
    allowed={'.java','.jsx','.js','.css','.sql','.jsp','.ps1','.mjs','.py','.xml','.toml','.html'}
    return [ROOT/n for n in names if Path(n).suffix in allowed and not n.startswith(('docs/','output/')) and '/public/fonts/' not in n and (ROOT/n).is_file()]

def append_source(story):
    files=tracked_files()
    story += [PageBreak(),paragraph('35. File inventory and responsibility map','GuideH1'),paragraph('This inventory includes retained legacy files and standalone diagnostics. A file being present is not proof it is executed by the current React flow. Follow the imports, servlet mappings and documented current path.')]
    for path in files:
        rel=path.relative_to(ROOT).as_posix()
        story.append(paragraph(rel,'GuideH2'));story.append(paragraph(role(path),'GuideSmall'))
    story += [PageBreak(),paragraph('36. Numbered source and guided line explanations','GuideH1'),paragraph('Exact production-source snapshot, with source line numbers. Long lines wrap visually but retain their original number. Source punctuation and non-ASCII symbols are escaped for print readability. Explanations highlight meaningful lines; the source remains authoritative.')]
    selected=[p for p in files if (str(p.relative_to(ROOT)).replace('\\','/').startswith('src/main/java/') and not p.name.endswith('Test.java') and p.name not in ('DBTest.java','DBConnectionExample.java','TestServlet.java')) or p.relative_to(ROOT).as_posix() in ['QueueSystemFrontend/src/App.jsx','QueueSystemFrontend/src/main.jsx','QueueSystemFrontend/src/student-api.js','QueueSystemFrontend/src/queue-layout.js','frontend/src/App.jsx','frontend/src/main.jsx','frontend/src/services/api.js'] or p.relative_to(ROOT).as_posix().startswith(('frontend/src/components/','frontend/src/pages/')) or p.name in ('Dockerfile','netlify-redirects.mjs')]
    if ROOT/'Dockerfile' not in selected:selected.append(ROOT/'Dockerfile')
    for path in selected:
        rel=path.relative_to(ROOT).as_posix();lines=path.read_text(encoding='utf-8-sig').splitlines()
        story += [PageBreak(),paragraph(rel,'GuideH1'),paragraph(role(path))]
        notes=[]
        for i,line in enumerate(lines,1):
            note=explain(line)
            if note and len(notes)<8:notes.append(f'Line {i}: {note}')
        if notes:
            story.append(paragraph('Read these lines first','GuideH2'))
            story.extend(paragraph(n,'GuideSmall') for n in notes)
        story.append(paragraph('Numbered source','GuideH2'))
        story.extend(code_blocks(lines,numbered=True))
    story += [PageBreak(),paragraph('37. HTML, CSS and SQL source practice','GuideH1')]
    extra=['QueueSystemFrontend/index.html','frontend/index.html','database/schema.sql','database/migrations/001_daily_reservations.sql','database/migrations/002_processing_duration.sql','database/seed_services.sql','pom.xml','QueueSystemFrontend/netlify.toml','frontend/netlify.toml']
    for rel in extra:
        path=ROOT/rel
        story.append(paragraph(rel,'GuideH2'));story.append(paragraph(role(path),'GuideSmall'));story.extend(code_blocks(path.read_text(encoding='utf-8-sig').splitlines(),True))
    for rel in ['QueueSystemFrontend/src/styles/original-identity.css','frontend/src/style.css']:
        lines=(ROOT/rel).read_text(encoding='utf-8-sig').splitlines()
        story.append(paragraph(rel+' - CSS orientation excerpt','GuideH2'))
        story.append(paragraph('The full stylesheet stays in the repository. Explain selectors, cascade, variables, box sizing, flex/grid, media queries and focus styles using this opening excerpt, then navigate to the actual selected rule.'))
        story.extend(code_blocks(lines[:90],True))

def build(name,title,subtitle,source,appendix=False):
    doc=GuideDoc(OUT/name,subtitle,title=title,author='Campus Queue project team')
    cover=[Spacer(1,105),paragraph('CAMPUS QUEUE','GuideH1'),Spacer(1,12),paragraph(title,'GuideH1'),paragraph(subtitle),Spacer(1,20),paragraph('Prepared for project demonstration, code review and academic viva.'),paragraph('8 October 2026'),Spacer(1,25),paragraph('Verified source, practical SQL and honest implementation boundaries. Credentials are deliberately excluded.'),PageBreak(),paragraph('Contents','GuideH1')]
    toc=TableOfContents();toc.levelStyles=[ParagraphStyle(name='TOC0',fontName='Helvetica',fontSize=9.2,leading=13,spaceBefore=5,textColor=INK),ParagraphStyle(name='TOC1',fontName='Helvetica',fontSize=8,leading=11,leftIndent=12,textColor=INK)]
    story=cover+[toc]+prose(source,compact=not appendix)
    if appendix:append_source(story)
    doc.multiBuild(story)
    print(OUT/name)

if __name__=='__main__':
    build('Campus-Queue-Master-Guide.pdf','Project, code, database and viva master guide','Detailed technical reference',ROOT/'docs/master-guide.md',True)
    build('Campus-Queue-Presentation-Runbook.pdf','Presentation flow and rehearsal runbook','Practical demonstration sequence',ROOT/'docs/presentation-runbook.md')

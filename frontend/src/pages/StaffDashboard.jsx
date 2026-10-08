// VIVA GUIDE: Staff dashboard orchestration: polls endpoints, derives assigned current service, runs actions and refreshes UI state.
import StudentPhone from '../components/StudentPhone'
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { api } from '../services/api'
import StatCard from '../components/StatCard'
import QueueTable from '../components/QueueTable'
import ActivityFeed from '../components/ActivityFeed'

const pageLabels = {
  dashboard: ['STAFF DASHBOARD', 'Dashboard'],
  queue: ['QUEUE MANAGEMENT', 'Queue Management'],
  bookings: ['BOOKINGS', 'Bookings'],
  analytics: ['ANALYTICS', 'Analytics'],
}

// Coordinate staff summary, queue, counter and activity data; backend responses determine state and available actions.
export default function StaffDashboard({ staff, onLogout, page = 'dashboard', onNavigate }) {
  // Staff dashboard metrics returned by the service-scoped summary endpoint.
  const [summary, setSummary] = useState(null)
  // Staff service queue returned by the backend; ticket assignment controls visible actions.
  const [queue, setQueue] = useState([])
  // Availability and assignment details for the signed-in staff counter.
  const [counter, setCounter] = useState(null)
  // Recent service activity displayed by ActivityFeed.
  const [activity, setActivity] = useState([])
  // An action is pending; disable related controls to reduce repeated submissions.
  const [busy, setBusy] = useState(false)
  // User-facing request/form error; empty text means there is no current error banner.
  const [error, setError] = useState('')
  // Time when the latest staff data load finished, shown to the operator.
  const [lastUpdated, setLastUpdated] = useState(null)
  const loading = useRef(false)

  const load = useCallback(async (silent = false) => {
    // Cloud reads may exceed the poll interval; never stack another four requests on the unfinished load.
    if (loading.current) return
    loading.current = true
    try {
      if (!silent) setError('')
      // Load independent endpoints concurrently, then apply their returned data to UI state.
      const [summaryResult, queueResult, counterResult, activityResult] = await Promise.all([
        api.summary(), api.queue(), api.counterStatus(), api.recentActivity(),
      ])
      setSummary(summaryResult.data)
      setQueue(queueResult.data || [])
      setCounter(counterResult.data)
      setActivity(activityResult.data || [])
      setLastUpdated(new Date())
      setError('')
    } catch (err) {
      if (err.status === 401) {
        onLogout()
        return
      }
      setError(err.message)
    } finally { loading.current = false }
  }, [onLogout])

  // React side effect: inspect dependencies and cleanup to avoid stale requests, duplicate timers or leftover animations.
  useEffect(() => {
    load()
    // Polling refreshes the server view periodically; the effect cleanup must clear this timer.
    // Refresh summary, queue, availability and activity every seven seconds; cleanup stops polling.
    const timer = setInterval(() => { if (!document.hidden) load(true) }, 10000)
    return () => clearInterval(timer)
  }, [load])

  // Select this operator's assigned SERVING ticket first, otherwise their CALLED ticket; another counter's ticket is not current.
  const currentService = useMemo(() => queue.find(q => q.counterId === staff.counterId && q.assignedStaffId === staff.staffId && q.status === 'SERVING') || queue.find(q => q.counterId === staff.counterId && q.assignedStaffId === staff.staffId && q.status === 'CALLED'), [queue, staff.counterId, staff.staffId])
  const waiting = queue.filter(q => q.status === 'WAITING').length

  // Disable repeated UI actions while awaiting the server, then reload dashboard data; finally clears busy state.
  async function runAction(action) {
    try {
      setBusy(true)
      setError('')
      await action()
      await load(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const setCounterStatus = (status) => runAction(async () => {
    const result = await api.setCounterStatus(status)
    setCounter(result.data)
  })

  const statusClass = counter?.status?.toLowerCase() || 'unknown'
  const [eyebrow, titleLabel] = pageLabels[page] || pageLabels.dashboard
  const firstName = staff.staffName.split(' ')[0]

  const nav = (target) => onNavigate(target)

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="side-brand cq-brand" aria-label="Campus Queue"><img className="cq-symbol" src="/campus-queue-logo.svg" alt=""/><span className="cq-wordmark"><span>Campus</span><strong>Queue</strong></span></div>
        <nav>
          <div className="nav-section">STAFF CONSOLE</div>
          <button className={`nav-item ${page === 'dashboard' ? 'active' : ''}`} onClick={() => nav('dashboard')}><span>▦</span> Dashboard</button>
          <button className={`nav-item ${page === 'queue' ? 'active' : ''}`} onClick={() => nav('queue')}><span>◷</span> Queue Management</button>
          <button className={`nav-item ${page === 'bookings' ? 'active' : ''}`} onClick={() => nav('bookings')}><span>▣</span> Bookings</button>
          <button className={`nav-item ${page === 'analytics' ? 'active' : ''}`} onClick={() => nav('analytics')}><span>◒</span> Analytics</button>
        </nav>
        <div className="side-bottom">
          <div className="session-label">SIGNED IN AS</div>
          <strong>{staff.staffName}</strong>
          <span>{staff.email}</span>
          <button className="logout-btn" onClick={onLogout}>Sign out</button>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <div className="eyebrow">{eyebrow}</div>
            <h1>{page === 'dashboard' ? `Good ${new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 18 ? 'afternoon' : 'evening'}, ${firstName}.` : titleLabel}</h1>
          </div>
          <div className="topbar-right">
            <div className="live-pill"><span /> Live</div>
            <div className="clock">{new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
          </div>
        </header>

        {error && <div className="error-banner">{error}</div>}

        <section className="context-bar">
          <div><span>Service</span><strong>{counter?.serviceName || `Service #${staff.serviceId}`}</strong></div>
          <div><span>Counter</span><strong>{counter?.counterName || `Counter #${staff.counterId}`}</strong></div>
          <div><span>Last refresh</span><strong>{lastUpdated ? lastUpdated.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : 'Loading…'}</strong></div>
          <div className="counter-control">
            <span>Counter status</span>
            <select value={counter?.status || ''} onChange={(e) => setCounterStatus(e.target.value)} disabled={busy || !counter}>
              <option value="AVAILABLE">Available</option>
              <option value="PAUSED">Paused</option>
              <option value="CHECKED_OUT">Checked out</option>
            </select>
          </div>
        </section>

        {page === 'dashboard' && (
          <DashboardView staff={staff} summary={summary} queue={queue} waiting={waiting} counter={counter} activity={activity} busy={busy} runAction={runAction} currentService={currentService} statusClass={statusClass} />
        )}

        {page === 'queue' && (
          <QueueManagementView staff={staff} queue={queue} waiting={waiting} counter={counter} busy={busy} runAction={runAction} />
        )}

        {page === 'bookings' && <BookingsView summary={summary} queue={queue} />}

        {page === 'analytics' && <AnalyticsView summary={summary} activity={activity} queue={queue} />}
      </main>
    </div>
  )
}

// Named helper DashboardView: read its arguments and return value; callers determine whether it renders UI or performs an action.
function DashboardView({ staff, summary, queue, waiting, counter, activity, busy, runAction, currentService, statusClass }) {
  return <>
    <section className="stats-grid">
      <StatCard label="Students Waiting" value={summary?.studentsWaiting ?? waiting} icon="◉" />
      <StatCard label="Served Today" value={summary?.servedToday} icon="✓" />
      <StatCard label="Avg. Wait" value={summary?.averageWaitMinutes} suffix=" min" icon="◷" />
      <StatCard label="Avg. Service" value={summary?.averageServiceMinutes} suffix=" min" icon="↗" />
      <StatCard label="No-shows" value={summary?.noShowsToday} icon="!" />
      <StatCard label="Peak Hour" value={summary?.peakHour || '—'} icon="⌁" />
    </section>

    <section className="workspace-grid">
      <div className="panel queue-panel">
        <div className="panel-head">
          <div><h2>Live Queue</h2><p>{queue.length} active entries · {waiting} waiting</p></div>
          {!queue.some(q=>q.counterId===staff.counterId && ['CALLED','SERVING'].includes(q.status)) && <button className="primary-btn" disabled={busy || counter?.status !== 'AVAILABLE' || !queue.some(q=>q.status==='WAITING') || queue.some(q=>q.counterId===staff.counterId && ['CALLED','SERVING'].includes(q.status))} onClick={() => runAction(api.callNext)}>Summon next student</button>}
        </div>
        <QueueTable staff={staff} queue={queue} busy={busy} onCall={(id) => runAction(() => api.callStudent(id))} onStart={(id) => runAction(() => api.startService(id))} onComplete={(id) => runAction(() => api.completeService(id))} onNoShow={(id) => {if(window.confirm('Mark this student absent? First absence moves the ticket back up to ten places; second absence ends this ticket.'))runAction(() => api.noShow(id))}} />
      </div>
      <div className="right-column">
        <div className="panel current-panel">
          <div className="panel-head"><div><h2>Current Service</h2><p>Counter assignment</p></div><span className={`status status-${statusClass}`}>{counter?.status || '—'}</span></div>
          {currentService ? <div className="current-card"><div className="current-token">A{String(currentService.tokenNumber).padStart(3, '0')}</div><div><span>Queue #{currentService.queueId}</span><strong>{currentService.status === 'SERVING' ? 'Service in progress' : 'Student called'}</strong></div></div> : <div className="empty-state">No student is currently being served.</div>}
          {currentService && <div className="current-student"><strong>{currentService.studentName || `Student #${currentService.studentId}`}</strong><div>{currentService.studentEmail}</div><StudentPhone phone={currentService.studentPhone} name={currentService.studentName} /></div>}
          <div className="current-actions">
            {currentService?.status === 'CALLED' && <button className="primary-btn full" disabled={busy} onClick={() => runAction(() => api.startService(currentService.queueId))}>Start Service</button>}
            {currentService?.status === 'SERVING' && <button className="primary-btn full" disabled={busy} onClick={() => runAction(() => api.completeService(currentService.queueId))}>Complete Service</button>}
            {currentService?.status === 'CALLED' && <button className="secondary-btn full" disabled={busy} onClick={() => { if(window.confirm('Mark this student absent? First absence moves the ticket back up to ten places; second absence ends this ticket.'))runAction(() => api.noShow(currentService.queueId)) }}>Mark absent</button>}

          </div>
        </div>
        <div className="panel activity-panel"><div className="panel-head"><div><h2>Recent Activity</h2><p>Latest queue actions</p></div></div><ActivityFeed items={activity} /></div>
      </div>
    </section>

    <section className="bottom-grid">
      <div className="mini-panel"><span>Today Reservations</span><strong>{summary?.upcomingBookings ?? '—'}</strong><small>for this service</small></div>
      <div className="mini-panel"><span>Counter Assignment</span><strong>{counter?.counterName || '—'}</strong><small>{counter?.serviceName || ''}</small></div>
      <div className="mini-panel"><span>System</span><strong>Connected</strong><small>Auto-refresh every 7 seconds</small></div>
    </section>
  </>
}

// Named helper QueueManagementView: read its arguments and return value; callers determine whether it renders UI or performs an action.
function QueueManagementView({ staff, queue, waiting, counter, busy, runAction }) {
  return <>
    <section className="stats-grid compact-stats">
      <StatCard label="Active Entries" value={queue.length} icon="◷" />
      <StatCard label="Waiting" value={waiting} icon="◉" />
      <StatCard label="Called" value={queue.filter(q => q.status === 'CALLED').length} icon="↗" />
      <StatCard label="Serving" value={queue.filter(q => q.status === 'SERVING').length} icon="✓" />
    </section>
    <section className="panel queue-panel full-panel">
      <div className="panel-head"><div><h2>Queue Management</h2><p>Manage the live queue for {counter?.serviceName || 'your assigned service'}.</p></div>{!queue.some(q=>q.counterId===staff.counterId && ['CALLED','SERVING'].includes(q.status)) && <button className="primary-btn" disabled={busy || counter?.status !== 'AVAILABLE' || !queue.some(q=>q.status==='WAITING') || queue.some(q=>q.counterId===staff.counterId && ['CALLED','SERVING'].includes(q.status))} onClick={() => runAction(api.callNext)}>Summon next student</button>}</div>
      {queue.length ? <QueueTable staff={staff} queue={queue} busy={busy} onCall={(id) => runAction(() => api.callStudent(id))} onStart={(id) => runAction(() => api.startService(id))} onComplete={(id) => runAction(() => api.completeService(id))} onNoShow={(id) => {if(window.confirm('Mark this student absent? First absence moves the ticket back up to ten places; second absence ends this ticket.'))runAction(() => api.noShow(id))}} /> : <div className="empty-state large-empty"><strong>No students are currently in the live queue.</strong><span>When a student joins this service, their token will appear here automatically.</span></div>}
    </section>
  </>
}

// Named helper BookingsView: read its arguments and return value; callers determine whether it renders UI or performs an action.
function BookingsView({ summary, queue }) {
  return <section className="page-grid">
    <div className="panel info-panel">
      <div className="panel-head"><div><h2>Today Reservations</h2><p>Bookings assigned to your service.</p></div><span className="metric-badge">{summary?.upcomingBookings ?? 0}</span></div>
      <div className="table-wrap"><table><thead><tr><th>Token</th><th>Student</th><th>Status</th><th>Missed calls</th></tr></thead><tbody>{queue.map(q=><tr key={q.queueId}><td>{String(q.tokenNumber).padStart(3,"0")}</td><td>Student #{q.studentId}</td><td>{q.status}</td><td>{q.missedTurns}</td></tr>)}</tbody></table>{!queue.length && <div className="empty-state">No active reservations for today.</div>}</div>
    </div>
    <div className="panel info-panel"><h2>Booking workflow</h2><div className="workflow-list"><div><b>1</b><span>Student reserves a place in today's queue.</span></div><div><b>2</b><span>Reservations follow confirmation order within the service.</span></div><div><b>3</b><span>Counters open 10 AM–1 PM and 2–3 PM.</span></div><div><b>4</b><span>First missed call moves back; second missed call ends the ticket.</span></div></div></div>
  </section>
}

function AnalyticsView({ summary, activity, queue }) {
  return <>
    <section className="stats-grid">
      <StatCard label="Students Served Today" value={summary?.servedToday} icon="✓" />
      <StatCard label="Average Wait" value={summary?.averageWaitMinutes} suffix=" min" icon="◷" />
      <StatCard label="Average Service" value={summary?.averageServiceMinutes} suffix=" min" icon="↗" />
      <StatCard label="No-shows Today" value={summary?.noShowsToday} icon="!" />
      <StatCard label="Peak Hour" value={summary?.peakHour || '—'} icon="⌁" />
      <StatCard label="Current Queue" value={queue.length} icon="◉" />
    </section>
    <section className="page-grid">
      <div className="panel info-panel"><h2>Operational Snapshot</h2><div className="analytics-rows"><div><span>Students waiting</span><strong>{summary?.studentsWaiting ?? 0}</strong></div><div><span>Upcoming bookings</span><strong>{summary?.upcomingBookings ?? 0}</strong></div><div><span>Recent recorded actions</span><strong>{activity.length}</strong></div><div><span>Refresh interval</span><strong>7 sec</strong></div></div></div>
      <div className="panel info-panel"><h2>Recent Activity</h2><ActivityFeed items={activity} /></div>
    </section>
  </>
}

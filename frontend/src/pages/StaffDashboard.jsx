import { useCallback, useEffect, useMemo, useState } from 'react'
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

export default function StaffDashboard({ staff, onLogout, page = 'dashboard', onNavigate }) {
  const [summary, setSummary] = useState(null)
  const [queue, setQueue] = useState([])
  const [counter, setCounter] = useState(null)
  const [activity, setActivity] = useState([])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [lastUpdated, setLastUpdated] = useState(null)

  const load = useCallback(async (silent = false) => {
    try {
      if (!silent) setError('')
      const [summaryResult, queueResult, counterResult, activityResult] = await Promise.all([
        api.summary(), api.queue(), api.counterStatus(), api.recentActivity(),
      ])
      setSummary(summaryResult.data)
      setQueue(queueResult.data || [])
      setCounter(counterResult.data)
      setActivity(activityResult.data || [])
      setLastUpdated(new Date())
    } catch (err) {
      if (err.message === 'Staff is not logged in') {
        onLogout()
        return
      }
      setError(err.message)
    }
  }, [onLogout])

  useEffect(() => {
    load()
    const timer = setInterval(() => load(true), 7000)
    return () => clearInterval(timer)
  }, [load])

  const currentService = useMemo(() => queue.find(q => q.status === 'SERVING') || queue.find(q => q.status === 'CALLED'), [queue])
  const waiting = queue.filter(q => q.status === 'WAITING').length

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
        <div className="side-brand"><span className="brand-mark small">CQ</span><span>Campus Queue</span></div>
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
          <DashboardView summary={summary} queue={queue} waiting={waiting} counter={counter} activity={activity} busy={busy} runAction={runAction} currentService={currentService} statusClass={statusClass} />
        )}

        {page === 'queue' && (
          <QueueManagementView queue={queue} waiting={waiting} counter={counter} busy={busy} runAction={runAction} />
        )}

        {page === 'bookings' && <BookingsView summary={summary} />}

        {page === 'analytics' && <AnalyticsView summary={summary} activity={activity} queue={queue} />}
      </main>
    </div>
  )
}

function DashboardView({ summary, queue, waiting, counter, activity, busy, runAction, currentService, statusClass }) {
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
          <button className="primary-btn" disabled={busy || counter?.status !== 'AVAILABLE'} onClick={() => runAction(api.callNext)}>Call Next</button>
        </div>
        <QueueTable queue={queue} busy={busy} onCall={(id) => runAction(() => api.callStudent(id))} onStart={(id) => runAction(() => api.startService(id))} onComplete={(id) => runAction(() => api.completeService(id))} onSkip={(id) => runAction(() => api.skipStudent(id))} />
      </div>
      <div className="right-column">
        <div className="panel current-panel">
          <div className="panel-head"><div><h2>Current Service</h2><p>Counter assignment</p></div><span className={`status status-${statusClass}`}>{counter?.status || '—'}</span></div>
          {currentService ? <div className="current-card"><div className="current-token">A{String(currentService.tokenNumber).padStart(3, '0')}</div><div><span>Queue #{currentService.queueId}</span><strong>{currentService.status === 'SERVING' ? 'Service in progress' : 'Student called'}</strong></div></div> : <div className="empty-state">No student is currently being served.</div>}
          <div className="current-actions">
            {currentService?.status === 'CALLED' && <button className="primary-btn full" disabled={busy} onClick={() => runAction(() => api.startService(currentService.queueId))}>Start Service</button>}
            {currentService?.status === 'SERVING' && <button className="primary-btn full" disabled={busy} onClick={() => runAction(() => api.completeService(currentService.queueId))}>Complete Service</button>}
            {currentService && <button className="secondary-btn full" disabled={busy} onClick={() => runAction(() => api.skipStudent(currentService.queueId))}>Skip Student</button>}
          </div>
        </div>
        <div className="panel activity-panel"><div className="panel-head"><div><h2>Recent Activity</h2><p>Latest queue actions</p></div></div><ActivityFeed items={activity} /></div>
      </div>
    </section>

    <section className="bottom-grid">
      <div className="mini-panel"><span>Upcoming Bookings</span><strong>{summary?.upcomingBookings ?? '—'}</strong><small>for this service</small></div>
      <div className="mini-panel"><span>Counter Assignment</span><strong>{counter?.counterName || '—'}</strong><small>{counter?.serviceName || ''}</small></div>
      <div className="mini-panel"><span>System</span><strong>Connected</strong><small>Auto-refresh every 7 seconds</small></div>
    </section>
  </>
}

function QueueManagementView({ queue, waiting, counter, busy, runAction }) {
  return <>
    <section className="stats-grid compact-stats">
      <StatCard label="Active Entries" value={queue.length} icon="◷" />
      <StatCard label="Waiting" value={waiting} icon="◉" />
      <StatCard label="Called" value={queue.filter(q => q.status === 'CALLED').length} icon="↗" />
      <StatCard label="Serving" value={queue.filter(q => q.status === 'SERVING').length} icon="✓" />
    </section>
    <section className="panel queue-panel full-panel">
      <div className="panel-head"><div><h2>Queue Management</h2><p>Manage the live queue for {counter?.serviceName || 'your assigned service'}.</p></div><button className="primary-btn" disabled={busy || counter?.status !== 'AVAILABLE'} onClick={() => runAction(api.callNext)}>Call Next</button></div>
      {queue.length ? <QueueTable queue={queue} busy={busy} onCall={(id) => runAction(() => api.callStudent(id))} onStart={(id) => runAction(() => api.startService(id))} onComplete={(id) => runAction(() => api.completeService(id))} onSkip={(id) => runAction(() => api.skipStudent(id))} /> : <div className="empty-state large-empty"><strong>No students are currently in the live queue.</strong><span>When a student joins this service, their token will appear here automatically.</span></div>}
    </section>
  </>
}

function BookingsView({ summary }) {
  return <section className="page-grid">
    <div className="panel info-panel">
      <div className="panel-head"><div><h2>Upcoming Bookings</h2><p>Bookings assigned to your service.</p></div><span className="metric-badge">{summary?.upcomingBookings ?? 0}</span></div>
      <div className="empty-state large-empty"><strong>{summary?.upcomingBookings ? `${summary.upcomingBookings} upcoming booking${summary.upcomingBookings === 1 ? '' : 's'}` : 'No upcoming bookings'}</strong><span>The current staff API exposes the upcoming-booking count on the dashboard. Detailed booking records remain managed by the booking module.</span></div>
    </div>
    <div className="panel info-panel"><h2>Booking workflow</h2><div className="workflow-list"><div><b>1</b><span>Student creates a booking.</span></div><div><b>2</b><span>Booking is assigned to a service.</span></div><div><b>3</b><span>Upcoming count is reflected on the staff dashboard.</span></div><div><b>4</b><span>No-show and slot-release logic is handled by the smart module.</span></div></div></div>
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

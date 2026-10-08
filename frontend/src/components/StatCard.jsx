// VIVA GUIDE: Reusable summary card presentation; values come from the dashboard API.
// Render a reusable metric card using the value passed from dashboard data.
export default function StatCard({label,value,suffix='',icon}) {
  return <div className="stat-card"><span className="stat-icon">{icon}</span><div><span className="eyebrow">{label}</span><strong>{value ?? '—'}<small>{suffix}</small></strong></div></div>
}

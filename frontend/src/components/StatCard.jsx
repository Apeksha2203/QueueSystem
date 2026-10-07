export default function StatCard({label,value,suffix='',icon}) {
  return <div className="stat-card"><span className="stat-icon">{icon}</span><div><span className="eyebrow">{label}</span><strong>{value ?? '—'}<small>{suffix}</small></strong></div></div>
}

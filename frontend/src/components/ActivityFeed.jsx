export default function ActivityFeed({items=[]}) {
  return <div className="activity-feed">{items.length ? items.map((item,index)=><div className="activity-item" key={`${item.queueId}-${item.action}-${index}`}><strong>A{String(item.tokenNumber ?? '').padStart(3,'0')}</strong><span>{item.action?.replaceAll('_',' ')}</span><small>{item.timestamp}</small></div>) : <div className="empty-state">No recent activity.</div>}</div>
}

// VIVA GUIDE: Queue rows and state/ownership-based action buttons. Server checks remain authoritative.
import StudentPhone from './StudentPhone'
// Render queue rows and eligible action buttons based on status and assignment; hiding buttons does not replace backend checks.
export default function QueueTable({queue,busy,staff,onStart,onComplete,onNoShow}) {
  return <div className="table-wrap"><table><thead><tr><th>Token</th><th>Queue ID</th><th>Student</th><th>Status</th><th>Action</th></tr></thead><tbody>{queue.map(q=>{
    const owned=q.counterId===staff?.counterId&&q.assignedStaffId===staff?.staffId;
    return <tr key={q.queueId}><td><strong>{String(q.tokenNumber).padStart(3,'0')}</strong></td><td>{q.queueId}</td><td><strong>{q.studentName||`Student #${q.studentId}`}</strong><div>{q.studentEmail}</div><div><StudentPhone phone={q.studentPhone} name={q.studentName} /></div></td><td>{q.status}</td><td>{owned&&q.status==='CALLED'&&<><button disabled={busy} onClick={()=>onStart(q.queueId)}>Start</button><button disabled={busy} onClick={()=>onNoShow(q.queueId)}>Mark absent</button></>}{owned&&q.status==='SERVING'&&<button disabled={busy} onClick={()=>onComplete(q.queueId)}>Complete</button>}{!owned&&q.status!=='WAITING'&&<span>Assigned to another operator</span>}{q.status==='WAITING'&&<span>Waiting to be summoned</span>}</td></tr>;
  })}</tbody></table>{!queue.length&&<div className="empty-state">No active students in the queue.</div>}</div>
}

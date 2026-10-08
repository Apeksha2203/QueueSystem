// VIVA GUIDE: Validated telephone display and tel link. Device-mediated calling; no server telephony or queue mutation.
// Validate/display a phone number with a tel link; the device places the call, not the queue server.
export default function StudentPhone({ phone, name = 'student' }) {
  const number = String(phone || '').replace(/[\s()-]/g, '')
  if (!/^\+?[0-9]{10,15}$/.test(number)) return <span>Phone not provided</span>
  return <span className="student-phone"><span>{phone}</span><a className="phone-action" href={`tel:${number}`} aria-label={`Phone ${name}`} title={`Phone ${name}`}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true"><path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1 1 .4 2 .7 2.9a2 2 0 0 1-.5 2.1L8 10a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.5c.9.3 1.9.6 2.9.7a2 2 0 0 1 1.7 2z"/></svg></a></span>
}

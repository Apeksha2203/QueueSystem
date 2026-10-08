// VIVA GUIDE: Staff login form and error/busy handling. Uses the shared API adapter; it does not read MySQL directly.
import { useState } from 'react'
import { api } from '../services/api'

// Render the staff sign-in form and surface request errors; assigned service/counter come from the backend profile.
export default function StaffLogin({ onLogin }) {
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [email, setEmail] = useState('')
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [password, setPassword] = useState('')
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [showPassword, setShowPassword] = useState(false)
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [loading, setLoading] = useState(false)
  // User-facing request/form error; empty text means there is no current error banner.
  const [error, setError] = useState('')

 // Named helper submit: read its arguments and return value; callers determine whether it renders UI or performs an action.
 async function submit(event) {
  event.preventDefault()
  setError('')

  if (!email.trim() || !password) {
    setError('Enter both email and password.')
    return
  }

  try {
    setLoading(true)

    // Step 1: authenticate and create the backend session
    await api.login(email.trim(), password)

    // Step 2: get the logged-in staff member from that session
    const profile = await api.profile()

    // Step 3: pass the real staff data to App.jsx
    onLogin(profile.data)

  } catch (err) {
    setError(err.message)
  } finally {
    setLoading(false)
  }
}

  return (
    <main className="login-page">
      <section className="login-card">
        <div className="cq-brand" aria-label="Campus Queue"><img className="cq-symbol" src="/campus-queue-logo.svg" alt=""/><span className="cq-wordmark"><span>Campus</span><strong>Queue</strong></span></div>
        <div className="eyebrow">CAMPUS QUEUE</div>
        <h1>Staff Operations</h1>
        <p className="login-subtitle">Sign in to manage your assigned service counter.</p>
        <form onSubmit={submit}>
          <label>Email</label>
          <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" placeholder="staff@college.edu" autoComplete="username" />
          <label>Password</label>
          <div className="password-wrap">
            <input value={password} onChange={(e) => setPassword(e.target.value)} type={showPassword ? 'text' : 'password'} placeholder="Enter password" autoComplete="current-password" />
            <button type="button" onClick={() => setShowPassword(v => !v)}>{showPassword ? 'Hide' : 'Show'}</button>
          </div>
          {error && <div className="error-box">{error}</div>}
          <button className="login-btn" disabled={loading}>{loading ? 'Signing in…' : 'Sign in'}</button>
        </form>
        <div className="login-footer">Secure session-based staff access</div>
      </section>
    </main>
  )
}

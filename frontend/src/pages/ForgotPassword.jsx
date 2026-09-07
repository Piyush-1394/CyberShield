import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [done, setDone] = useState(false)
  const [error, setError] = useState('')

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    try {
      await api.post('/api/auth/forgot-password', { email })
      setDone(true)
    } catch (err) {
      setError(err.response?.data?.message || 'Request failed')
    }
  }

  return (
    <div className="auth-shell">
      <form className="auth-card" onSubmit={onSubmit}>
        <div className="brand-mark"><span className="dot" /> CyberShield AI</div>
        <h2 style={{ marginTop: 16 }}>Forgot password</h2>
        <p className="muted">A reset token is logged by the API EmailService (check server logs). Then use Reset password.</p>
        <div className="field"><label>Email</label><input value={email} onChange={(e) => setEmail(e.target.value)} required /></div>
        {error && <p className="error">{error}</p>}
        {done && <p>If that account exists, a token was issued. Continue to reset.</p>}
        <button className="btn">Send reset token</button>
        <p className="muted" style={{ marginTop: 12 }}>
          <Link to="/reset-password">I have a token</Link> · <Link to="/login">Back to login</Link>
        </p>
      </form>
    </div>
  )
}

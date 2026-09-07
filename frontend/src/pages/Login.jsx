import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Login() {
  const { login } = useAuth()
  const nav = useNavigate()
  const [email, setEmail] = useState('admin@abc.university')
  const [password, setPassword] = useState('Admin#2026Ai')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function onSubmit(e) {
    e.preventDefault()
    setBusy(true); setError('')
    try {
      await login(email.trim(), password)
      nav('/app/overview')
    } catch (err) {
      const msg = err.response?.data?.message || (err.message === 'Network Error' ? 'Cannot connect to backend server. Please check CORS/network.' : err.message) || 'Login failed'
      setError(msg)
    } finally { setBusy(false) }
  }

  return (
    <div className="auth-shell">
      <form className="auth-card" onSubmit={onSubmit}>
        <div className="brand-mark"><span className="dot" /> CyberShield AI</div>
        <h2 style={{ marginTop: 16 }}>Sign in</h2>
        <p className="muted">ABC University demo: admin@abc.university / Admin#2026Ai</p>
        <div className="field"><label>Email</label><input value={email} onChange={(e) => setEmail(e.target.value)} /></div>
        <div className="field"><label>Password</label><input type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></div>
        {error && <p className="error">{error}</p>}
        <button className="btn" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
        <p className="muted" style={{ marginTop: 12 }}>
          <Link to="/forgot-password">Forgot password?</Link>
          {' · '}
          <Link to="/register">Register</Link>
        </p>
      </form>
    </div>
  )
}

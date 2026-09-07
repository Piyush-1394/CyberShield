import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Register() {
  const { register } = useAuth()
  const nav = useNavigate()
  const [form, setForm] = useState({ organizationName: '', fullName: '', email: '', password: '', inviteToken: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const payload = {
        ...form,
        organizationName: form.organizationName?.trim(),
        fullName: form.fullName?.trim(),
        email: form.email?.trim(),
        inviteToken: form.inviteToken?.trim() || undefined,
      }
      await register(payload)
      nav('/app/overview')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
    } finally { setBusy(false) }
  }

  return (
    <div className="auth-shell">
      <form className="auth-card" onSubmit={onSubmit}>
        <div className="brand-mark"><span className="dot" /> CyberShield AI</div>
        <h2 style={{ marginTop: 16 }}>Create organization</h2>
        <p className="muted">Self-serve signup, or paste an invite token to join an existing org.</p>
        <div className="field"><label>Organization</label><input value={form.organizationName} onChange={set('organizationName')} /></div>
        <div className="field"><label>Full name</label><input value={form.fullName} onChange={set('fullName')} required /></div>
        <div className="field"><label>Email</label><input value={form.email} onChange={set('email')} required /></div>
        <div className="field"><label>Password</label><input type="password" value={form.password} onChange={set('password')} required /></div>
        <p className="muted" style={{ fontSize: 12 }}>Min 10 chars, upper + lower + digit + special</p>
        <div className="field"><label>Invite token (optional)</label><input value={form.inviteToken} onChange={set('inviteToken')} /></div>
        {error && <p className="error">{error}</p>}
        <button className="btn" disabled={busy}>{busy ? 'Creating…' : 'Create account'}</button>
        <p className="muted" style={{ marginTop: 12 }}><Link to="/login">Back to login</Link></p>
      </form>
    </div>
  )
}

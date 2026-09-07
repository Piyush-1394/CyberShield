import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api/client'

export default function ResetPassword() {
  const nav = useNavigate()
  const [token, setToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState('')

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    try {
      await api.post('/api/auth/reset-password', { token, newPassword })
      nav('/login')
    } catch (err) {
      setError(err.response?.data?.message || 'Reset failed')
    }
  }

  return (
    <div className="auth-shell">
      <form className="auth-card" onSubmit={onSubmit}>
        <div className="brand-mark"><span className="dot" /> CyberShield AI</div>
        <h2 style={{ marginTop: 16 }}>Reset password</h2>
        <div className="field"><label>Token</label><input value={token} onChange={(e) => setToken(e.target.value)} required /></div>
        <div className="field"><label>New password</label><input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required /></div>
        {error && <p className="error">{error}</p>}
        <button className="btn">Reset password</button>
        <p className="muted" style={{ marginTop: 12 }}><Link to="/login">Back to login</Link></p>
      </form>
    </div>
  )
}

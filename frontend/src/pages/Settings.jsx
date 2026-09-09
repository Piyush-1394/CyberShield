import { useNotifications, useOrg, useUsers } from '../api/hooks'
import { ErrorState, PageHeader } from '../components/ui'
import { api } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AdminOnly } from '../auth/ProtectedRoute'
import { useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'

export default function Settings() {
  const org = useOrg()
  const users = useUsers()
  const prefs = useNotifications()
  const { user, isAdmin, setUser } = useAuth()
  const qc = useQueryClient()
  const [orgForm, setOrgForm] = useState({ name: '', logoUrl: '', budgetAvailable: '' })
  const [profile, setProfile] = useState({ fullName: '', email: '' })
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '' })
  const [invite, setInvite] = useState({ email: '', role: 'VIEWER' })
  const [inviteToken, setInviteToken] = useState('')
  const [orgSaved, setOrgSaved] = useState(false)

  useEffect(() => {
    if (org.data) setOrgForm({
      name: org.data.name,
      logoUrl: org.data.logoUrl || '',
      budgetAvailable: org.data.budgetAvailable ?? ''
    })
  }, [org.data])
  useEffect(() => {
    if (user) setProfile({ fullName: user.fullName, email: user.email })
  }, [user])

  if (org.isError) return <ErrorState error={org.error} retry={org.refetch} />

  return (
    <>
      <PageHeader title="Settings" subtitle="Organization, team, notifications, and your profile." />
      <div className="grid cols-2">
        <form className="card" onSubmit={async (e) => {
          e.preventDefault()
          await api.put('/api/settings/organization', {
            ...orgForm,
            budgetAvailable: orgForm.budgetAvailable ? Number(orgForm.budgetAvailable) : 0
          })
          qc.invalidateQueries({ queryKey: ['org'] })
          qc.invalidateQueries({ queryKey: ['invest-rec'] })
          qc.invalidateQueries({ queryKey: ['invest-proj'] })
          setOrgSaved(true)
          setTimeout(() => setOrgSaved(false), 3000)
        }}>
          <h3>Organization</h3>
          <div className="field"><label>Name</label><input disabled={!isAdmin} value={orgForm.name} onChange={(e) => setOrgForm({ ...orgForm, name: e.target.value })} /></div>
          <div className="field"><label>Logo URL</label><input disabled={!isAdmin} value={orgForm.logoUrl} onChange={(e) => setOrgForm({ ...orgForm, logoUrl: e.target.value })} /></div>
          <div className="field">
            <label>Budget Available (₹)</label>
            <input
              disabled={!isAdmin}
              type="number"
              min="0"
              step="50000"
              value={orgForm.budgetAvailable}
              onChange={(e) => setOrgForm({ ...orgForm, budgetAvailable: e.target.value })}
            />
          </div>
          <p className="muted">Allocated to date: ₹{Number(org.data?.budgetAllocated || 0).toLocaleString('en-IN')}</p>
          {orgSaved && <p style={{ color: '#10b981', fontSize: 13 }}>✓ Organization settings saved successfully!</p>}
          <AdminOnly><button className="btn">Save organization</button></AdminOnly>
        </form>
        <form className="card" onSubmit={async (e) => {
          e.preventDefault()
          const { data } = await api.put('/api/settings/profile', profile)
          setUser(data)
        }}>
          <h3>Your profile</h3>
          <div className="field"><label>Name</label><input value={profile.fullName} onChange={(e) => setProfile({ ...profile, fullName: e.target.value })} /></div>
          <div className="field"><label>Email</label><input value={profile.email} onChange={(e) => setProfile({ ...profile, email: e.target.value })} /></div>
          <button className="btn">Save profile</button>
        </form>
        <form className="card" onSubmit={async (e) => {
          e.preventDefault()
          await api.post('/api/auth/change-password', pw)
          setPw({ currentPassword: '', newPassword: '' })
        }}>
          <h3>Change password</h3>
          <div className="field"><label>Current</label><input type="password" value={pw.currentPassword} onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })} /></div>
          <div className="field"><label>New password</label><input type="password" value={pw.newPassword} onChange={(e) => setPw({ ...pw, newPassword: e.target.value })} /></div>
          <button className="btn secondary">Update password</button>
        </form>
      </div>
      <AdminOnly>
        <div className="card">
          <h3>Team</h3>
          <table className="table">
            <thead><tr><th>Name</th><th>Email</th><th>Role</th><th></th></tr></thead>
            <tbody>
              {(users.data || []).map((u) => (
                <tr key={u.id}>
                  <td>{u.fullName}</td><td>{u.email}</td>
                  <td>
                    <select value={u.role} onChange={async (e) => {
                      await api.patch(`/api/users/${u.id}/role`, { role: e.target.value })
                      qc.invalidateQueries({ queryKey: ['users'] })
                    }}>
                      {['ADMIN','ANALYST','VIEWER'].map((r) => <option key={r}>{r}</option>)}
                    </select>
                  </td>
                  <td><button className="btn danger" onClick={async () => { await api.delete(`/api/users/${u.id}`); qc.invalidateQueries({ queryKey: ['users'] }) }}>Remove</button></td>
                </tr>
              ))}
            </tbody>
          </table>
          <form className="row wrap" onSubmit={async (e) => {
            e.preventDefault()
            const { data } = await api.post('/api/users/invite', invite)
            setInviteToken(data.inviteToken)
          }}>
            <input placeholder="invite email" value={invite.email} onChange={(e) => setInvite({ ...invite, email: e.target.value })} />
            <select value={invite.role} onChange={(e) => setInvite({ ...invite, role: e.target.value })}>
              {['ADMIN','ANALYST','VIEWER'].map((r) => <option key={r}>{r}</option>)}
            </select>
            <button className="btn">Invite</button>
          </form>
          {inviteToken && <p className="muted">Invite token (also logged by EmailService): {inviteToken}</p>}
        </div>
      </AdminOnly>
      <form className="card" onSubmit={async (e) => {
        e.preventDefault()
        await api.put('/api/settings/notifications', prefs.data)
        qc.invalidateQueries({ queryKey: ['prefs'] })
      }}>
        <h3>Notifications</h3>
        {prefs.data && ['emailAlerts','inAppAlerts','weeklyDigest'].map((k) => (
          <label key={k} className="row" style={{ marginBottom: 8 }}>
            <input type="checkbox" checked={prefs.data[k]} onChange={(e) => {
              qc.setQueryData(['prefs'], { ...prefs.data, [k]: e.target.checked })
            }} /> {k}
          </label>
        ))}
        <button className="btn secondary">Save preferences</button>
      </form>
    </>
  )
}

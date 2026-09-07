import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { useAlerts, useMarkAllRead, useUnread } from '../api/hooks'
import { SeverityBadge } from '../components/ui'

const NAV = [
  ['/app/overview', 'Overview'],
  ['/app/assets', 'Assets'],
  ['/app/vulnerabilities', 'Vulnerabilities'],
  ['/app/threat-intel', 'Threat Intelligence'],
  ['/app/risk-analysis', 'Risk Analysis'],
  ['/app/investment', 'Investment Optimizer'],
  ['/app/whatif', 'What-if Analysis'],
  ['/app/alerts', 'Alerts'],
  ['/app/billing', 'Billing & Payments'],
  ['/app/reports', 'Reports'],
  ['/app/settings', 'Settings'],
]

export default function AppLayout() {
  const { user, logout } = useAuth()
  const nav = useNavigate()
  const unread = useUnread()
  const alerts = useAlerts({ page: 0, limit: 5 })
  const markAll = useMarkAllRead()
  const [bell, setBell] = useState(false)
  const [pay, setPay] = useState(false)
  const [profile, setProfile] = useState(false)

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-mark"><span className="dot" /> CyberShield AI</div>
        <div className="muted" style={{ fontSize: 12 }}>{user?.organizationName}</div>
        <nav className="nav">
          {NAV.map(([to, label]) => (
            <NavLink key={to} to={to} className={({ isActive }) => (isActive ? 'active' : '')}>
              {label}
              {to === '/app/alerts' && unread.data?.count > 0 && <span className="badge crit">{unread.data.count}</span>}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="main">
        <header className="topbar">
          <div>
            <div className="muted" style={{ fontSize: 12 }}>Cyber risk quantification</div>
            <strong>{user?.fullName}</strong> <span className="badge">{user?.role}</span>
          </div>
          <div className="row">
            <div className="rel">
              <button className="btn secondary" onClick={() => { setBell((v) => !v); setPay(false); setProfile(false) }}>
                Alerts {unread.data?.count ? `(${unread.data.count})` : ''}
              </button>
              {bell && (
                <div className="menu">
                  {(alerts.data?.content || []).map((a) => (
                    <div key={a.id} style={{ padding: 8 }}>
                      <SeverityBadge value={a.severity} /> {a.title}
                    </div>
                  ))}
                  <button onClick={() => markAll.mutate()}>Mark all as read</button>
                  <button onClick={() => { setBell(false); nav('/app/alerts') }}>View all alerts</button>
                </div>
              )}
            </div>
            <div className="rel">
              <button className="btn secondary" onClick={() => { setPay((v) => !v); setBell(false); setProfile(false) }}>Payments</button>
              {pay && (
                <div className="menu">
                  <button onClick={() => { setPay(false); nav('/app/billing') }}>Manage Billing →</button>
                </div>
              )}
            </div>
            <div className="rel">
              <button className="btn secondary" onClick={() => { setProfile((v) => !v); setBell(false); setPay(false) }}>
                {(user?.fullName || '?')[0]}
              </button>
              {profile && (
                <div className="menu">
                  <button onClick={() => { setProfile(false); nav('/app/settings') }}>Profile</button>
                  <button onClick={() => { setProfile(false); nav('/app/settings') }}>Settings</button>
                  <button onClick={async () => { await logout(); nav('/login') }}>Logout</button>
                </div>
              )}
            </div>
          </div>
        </header>
        <div className="content">
          <Outlet />
        </div>
      </div>
    </div>
  )
}

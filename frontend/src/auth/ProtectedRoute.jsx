import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

export function ProtectedRoute() {
  const { user, ready } = useAuth()
  if (!ready) return <div className="content muted">Restoring session…</div>
  if (!user) return <Navigate to="/login" replace />
  return <Outlet />
}

export function AdminOnly({ children }) {
  const { isAdmin } = useAuth()
  if (!isAdmin) return null
  return children
}

export function WriterOnly({ children }) {
  const { canWrite } = useAuth()
  if (!canWrite) return null
  return children
}

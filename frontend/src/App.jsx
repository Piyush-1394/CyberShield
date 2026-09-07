import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './auth/ProtectedRoute'
import AppLayout from './layout/AppLayout'
import Login from './pages/Login'
import Register from './pages/Register'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import Overview from './pages/Overview'
import Assets from './pages/Assets'
import Vulnerabilities from './pages/Vulnerabilities'
import ThreatIntel from './pages/ThreatIntel'
import RiskAnalysis from './pages/RiskAnalysis'
import Investment from './pages/Investment'
import WhatIf from './pages/WhatIf'
import Alerts from './pages/Alerts'
import Billing from './pages/Billing'
import Reports from './pages/Reports'
import Settings from './pages/Settings'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/app" element={<AppLayout />}>
          <Route index element={<Navigate to="overview" replace />} />
          <Route path="overview" element={<Overview />} />
          <Route path="assets" element={<Assets />} />
          <Route path="vulnerabilities" element={<Vulnerabilities />} />
          <Route path="threat-intel" element={<ThreatIntel />} />
          <Route path="risk-analysis" element={<RiskAnalysis />} />
          <Route path="investment" element={<Investment />} />
          <Route path="whatif" element={<WhatIf />} />
          <Route path="alerts" element={<Alerts />} />
          <Route path="billing" element={<Billing />} />
          <Route path="reports" element={<Reports />} />
          <Route path="settings" element={<Settings />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/app/overview" replace />} />
    </Routes>
  )
}

import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { api, clearTokens, setTokens, silentRefresh, getRefreshToken } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [ready, setReady] = useState(false)

  useEffect(() => {
    silentRefresh()
      .then((data) => setUser(data.user))
      .catch(() => setUser(null))
      .finally(() => setReady(true))
  }, [])

  useEffect(() => {
    const handler = () => setUser(null)
    window.addEventListener('auth-expired', handler)
    return () => window.removeEventListener('auth-expired', handler)
  }, [])

  const value = useMemo(() => ({
    user,
    ready,
    isAdmin: user?.role === 'ADMIN',
    canWrite: user?.role === 'ADMIN' || user?.role === 'ANALYST',
    async login(email, password) {
      const { data } = await api.post('/api/auth/login', { email, password })
      setTokens(data.accessToken, data.refreshToken)
      setUser(data.user)
      return data.user
    },
    async register(payload) {
      const { data } = await api.post('/api/auth/register', payload)
      setTokens(data.accessToken, data.refreshToken)
      setUser(data.user)
      return data.user
    },
    async logout() {
      try { await api.post('/api/auth/logout', { refreshToken: getRefreshToken() }) } catch { /* ignore */ }
      clearTokens()
      setUser(null)
    },
    setUser,
  }), [user, ready])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}

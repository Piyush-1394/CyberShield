import axios from 'axios'

// Use ?? (not ||) so an intentionally empty string ("" = same-origin, used
// when the backend serves the built frontend itself or via Vite proxy) isn't overridden.
let rawBaseURL = (import.meta.env.VITE_API_BASE_URL ?? '').trim()
if (rawBaseURL && !rawBaseURL.startsWith('http://') && !rawBaseURL.startsWith('https://')) {
  rawBaseURL = `https://${rawBaseURL}`
}
const baseURL = rawBaseURL.endsWith('/') ? rawBaseURL.slice(0, -1) : rawBaseURL

export const api = axios.create({
  baseURL,
  withCredentials: true,
})

let accessToken = null
let refreshCall = null

function loadRefreshToken() {
  try {
    return localStorage.getItem('cybershield_rt')
  } catch {
    return null
  }
}

function saveRefreshToken(token) {
  try {
    if (token) {
      localStorage.setItem('cybershield_rt', token)
    } else {
      localStorage.removeItem('cybershield_rt')
    }
  } catch {}
}

export function setTokens(access, refresh) {
  accessToken = access
  if (refresh) saveRefreshToken(refresh)
}

export function clearTokens() {
  accessToken = null
  saveRefreshToken(null)
}

export function getAccessToken() {
  return accessToken
}

export function getRefreshToken() {
  return loadRefreshToken()
}

api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config
    if (error.response?.status === 401 && !original?._retry && !String(original?.url || '').includes('/api/auth/')) {
      original._retry = true
      try {
        const tokens = await silentRefresh()
        original.headers.Authorization = `Bearer ${tokens.accessToken}`
        return api(original)
      } catch {
        clearTokens()
        window.dispatchEvent(new Event('auth-expired'))
      }
    }
    return Promise.reject(error)
  },
)

export async function silentRefresh() {
  if (!refreshCall) {
    const rt = loadRefreshToken()
    refreshCall = api
      .post('/api/auth/refresh', rt ? { refreshToken: rt } : {})
      .then((res) => {
        setTokens(res.data.accessToken, res.data.refreshToken)
        return res.data
      })
      .finally(() => {
        refreshCall = null
      })
  }
  return refreshCall
}

export function inr(value) {
  if (value == null) return '—'
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value)
}

export function downloadBlob(data, filename, type) {
  const url = URL.createObjectURL(new Blob([data], { type }))
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

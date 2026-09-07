import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './client'

export const useMe = () => useQuery({ queryKey: ['me'], queryFn: () => api.get('/api/auth/me').then((r) => r.data) })
export const useOrg = () => useQuery({ queryKey: ['org'], queryFn: () => api.get('/api/organizations/me').then((r) => r.data) })
export const useRiskOverview = () => useQuery({ queryKey: ['risk-overview'], queryFn: () => api.get('/api/risk/overview').then((r) => r.data) })
export const useRiskDistribution = () => useQuery({ queryKey: ['risk-dist'], queryFn: () => api.get('/api/risk/distribution').then((r) => r.data) })
export const useRiskTrend = (days) => useQuery({ queryKey: ['risk-trend', days], queryFn: () => api.get('/api/risk/trend', { params: { days } }).then((r) => r.data) })
export const useAiSummary = () => useQuery({ queryKey: ['ai-summary'], queryFn: () => api.get('/api/risk/ai-summary').then((r) => r.data) })
export const useTopAssets = () => useQuery({ queryKey: ['top-assets'], queryFn: () => api.get('/api/assets/top', { params: { limit: 5 } }).then((r) => r.data) })
export const useAssets = (params) => useQuery({ queryKey: ['assets', params], queryFn: () => api.get('/api/assets', { params }).then((r) => r.data) })
export const useAsset = (id) => useQuery({ queryKey: ['asset', id], enabled: !!id, queryFn: () => api.get(`/api/assets/${id}`).then((r) => r.data) })
export const useVulns = (params) => useQuery({ queryKey: ['vulns', params], queryFn: () => api.get('/api/vulnerabilities', { params }).then((r) => r.data) })
export const useThreats = (params) => useQuery({ queryKey: ['threats', params], queryFn: () => api.get('/api/threat-intel', { params }).then((r) => r.data) })
export const useRecommendations = () => useQuery({ queryKey: ['invest-rec'], queryFn: () => api.get('/api/investment/recommendations').then((r) => r.data) })
export const useProjection = () => useQuery({ queryKey: ['invest-proj'], queryFn: () => api.get('/api/investment/projection').then((r) => r.data) })
export const useScenarios = () => useQuery({ queryKey: ['scenarios'], queryFn: () => api.get('/api/whatif/scenarios').then((r) => r.data) })
export const useAlerts = (params) => useQuery({ queryKey: ['alerts', params], queryFn: () => api.get('/api/alerts', { params }).then((r) => r.data) })
export const useUnread = () => useQuery({ queryKey: ['unread'], queryFn: () => api.get('/api/alerts/unread-count').then((r) => r.data) })
export const usePlans = () => useQuery({ queryKey: ['plans'], queryFn: () => api.get('/api/billing/plans').then((r) => r.data) })
export const useCurrentPlan = () => useQuery({ queryKey: ['plan'], queryFn: () => api.get('/api/billing/plan').then((r) => r.data) })
export const usePaymentMethods = () => useQuery({ queryKey: ['pm'], queryFn: () => api.get('/api/billing/payment-methods').then((r) => r.data) })
export const useInvoices = () => useQuery({ queryKey: ['invoices'], queryFn: () => api.get('/api/billing/invoices').then((r) => r.data) })
export const useReports = () => useQuery({ queryKey: ['reports'], queryFn: () => api.get('/api/reports').then((r) => r.data) })
export const useUsers = () => useQuery({ queryKey: ['users'], queryFn: () => api.get('/api/users').then((r) => r.data) })
export const useNotifications = () => useQuery({ queryKey: ['prefs'], queryFn: () => api.get('/api/settings/notifications').then((r) => r.data) })

export function useInvalidate() {
  const qc = useQueryClient()
  return () => qc.invalidateQueries()
}

export const usePatchVuln = () => {
  const inv = useInvalidate()
  return useMutation({ mutationFn: (id) => api.post(`/api/vulnerabilities/${id}/patch`), onSuccess: inv })
}
export const useApplyInvestment = () => {
  const inv = useInvalidate()
  return useMutation({ mutationFn: (actionIds) => api.post('/api/investment/apply', { actionIds }).then((r) => r.data), onSuccess: inv })
}
export const useSimulate = () => useMutation({ mutationFn: (body) => api.post('/api/whatif/simulate', body).then((r) => r.data) })
export const useMarkAllRead = () => {
  const inv = useInvalidate()
  return useMutation({ mutationFn: () => api.post('/api/alerts/read-all'), onSuccess: inv })
}

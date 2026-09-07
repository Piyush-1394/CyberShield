import { useCurrentPlan, useInvoices, usePaymentMethods, usePlans } from '../api/hooks'
import { ErrorState, PageHeader, SkeletonRows } from '../components/ui'
import { api, downloadBlob, inr } from '../api/client'
import { AdminOnly } from '../auth/ProtectedRoute'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'

function loadRazorpay() {
  if (window.Razorpay) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.src = 'https://checkout.razorpay.com/v1/checkout.js'
    s.onload = resolve
    s.onerror = reject
    document.body.appendChild(s)
  })
}

export default function Billing() {
  const plan = useCurrentPlan()
  const plans = usePlans()
  const methods = usePaymentMethods()
  const invoices = useInvoices()
  const gateway = useQuery({ queryKey: ['gw'], queryFn: () => api.get('/api/billing/gateway-config').then((r) => r.data) })
  const qc = useQueryClient()
  const [add, setAdd] = useState(false)
  const [form, setForm] = useState({ brand: 'Visa', last4: '1111', methodType: 'card', gatewayToken: '' })
  const [busy, setBusy] = useState(false)
  if (plan.isError) return <ErrorState error={plan.error} retry={plan.refetch} />

  useEffect(() => {
    if (gateway.data && !gateway.data.mockMode) loadRazorpay().catch(() => {})
  }, [gateway.data])

  async function persistMethod(payload) {
    await api.post('/api/billing/payment-methods', payload)
    setAdd(false)
    qc.invalidateQueries({ queryKey: ['pm'] })
  }

  async function addMethod(e) {
    e.preventDefault()
    setBusy(true)
    try {
      if (gateway.data && !gateway.data.mockMode && form.methodType === 'card') {
        await loadRazorpay()
        await new Promise((resolve, reject) => {
          const rzp = new window.Razorpay({
            key: gateway.data.keyId,
            amount: 100,
            currency: 'INR',
            name: 'CyberShield AI',
            description: 'Tokenize payment method (test mode)',
            handler: async (response) => {
              try {
                await persistMethod({ ...form, gatewayToken: response.razorpay_payment_id })
                resolve()
              } catch (err) { reject(err) }
            },
            modal: { ondismiss: () => resolve() },
          })
          rzp.open()
        })
      } else {
        await persistMethod(form)
      }
    } finally { setBusy(false) }
  }

  return (
    <>
      <PageHeader title="Billing & Payments" subtitle="Plans, payment methods, and invoice history (₹)." />
      {gateway.data?.mockMode && (
        <p className="muted">Razorpay keys are unset — adding a method stores a mock token. Set RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET for Checkout test mode.</p>
      )}
      <div className="grid cols-2">
        <div className="card">
          <h3>Current plan</h3>
          {plan.isLoading ? <SkeletonRows /> : (
            <p><strong>{plan.data?.plan?.name}</strong> · {inr(plan.data?.plan?.priceMonthly)} / mo · renews {plan.data?.renewalDate}</p>
          )}
          <AdminOnly>
            <h3>Switch plan</h3>
            <div className="row wrap">
              {(plans.data || []).map((p) => (
                <button key={p.id} className="btn secondary" onClick={async () => {
                  await api.post(`/api/billing/plans/${p.id}/switch`)
                  qc.invalidateQueries()
                }}>{p.name} {inr(p.priceMonthly)}</button>
              ))}
            </div>
          </AdminOnly>
        </div>
        <div className="card">
          <div className="row space">
            <h3>Payment methods</h3>
            <AdminOnly><button className="btn" onClick={() => setAdd(true)}>Add payment method</button></AdminOnly>
          </div>
          {(methods.data || []).map((m) => (
            <div key={m.id} className="row space">
              <span>{m.brand} ···· {m.last4} {m.isDefault ? '(default)' : ''} ({m.methodType})</span>
              <AdminOnly>
                <span className="row">
                  <button className="btn ghost" onClick={async () => { await api.post(`/api/billing/payment-methods/${m.id}/select`); qc.invalidateQueries({ queryKey: ['pm'] }) }}>Select</button>
                  <button className="btn danger" onClick={async () => { await api.delete(`/api/billing/payment-methods/${m.id}`); qc.invalidateQueries({ queryKey: ['pm'] }) }}>Delete</button>
                </span>
              </AdminOnly>
            </div>
          ))}
          {add && (
            <form onSubmit={addMethod}>
              <div className="field"><label>Brand</label><input value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} /></div>
              <div className="field"><label>Last 4</label><input value={form.last4} onChange={(e) => setForm({ ...form, last4: e.target.value })} /></div>
              <div className="field"><label>Type</label>
                <select value={form.methodType} onChange={(e) => setForm({ ...form, methodType: e.target.value })}>
                  <option value="card">card</option><option value="upi">upi</option>
                </select>
              </div>
              {gateway.data?.mockMode && (
                <div className="field"><label>Gateway token (optional)</label><input value={form.gatewayToken} onChange={(e) => setForm({ ...form, gatewayToken: e.target.value })} /></div>
              )}
              <button className="btn" disabled={busy}>{busy ? 'Processing…' : 'Save'}</button>
            </form>
          )}
        </div>
      </div>
      <div className="card">
        <h3>Billing history</h3>
        {invoices.isLoading ? <SkeletonRows /> : (
          <table className="table">
            <thead><tr><th>Number</th><th>Amount</th><th>Status</th><th>Issued</th><th></th></tr></thead>
            <tbody>
              {(invoices.data?.content || []).map((i) => (
                <tr key={i.id}>
                  <td>{i.invoiceNumber}</td><td>{inr(i.amount)}</td><td>{i.status}</td><td>{i.issuedAt}</td>
                  <td><button className="btn ghost" onClick={async () => {
                    const res = await api.get(`/api/billing/invoices/${i.id}/pdf`, { responseType: 'blob' })
                    downloadBlob(res.data, `${i.invoiceNumber}.pdf`, 'application/pdf')
                  }}>Download</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}

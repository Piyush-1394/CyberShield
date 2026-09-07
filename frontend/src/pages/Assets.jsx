import { useState } from 'react'
import { useAssets } from '../api/hooks'
import { ErrorState, PageHeader, SeverityBadge, SkeletonRows } from '../components/ui'
import { api, inr } from '../api/client'
import { WriterOnly } from '../auth/ProtectedRoute'
import { useQueryClient } from '@tanstack/react-query'

const empty = { name: '', type: 'Application', category: 'Applications', criticality: 'HIGH', financialExposure: 100000, owner: '' }

export default function Assets() {
  const [params, setParams] = useState({ page: 0, size: 10, sort: 'riskScore,desc' })
  const q = useAssets(params)
  const [selected, setSelected] = useState(null)
  const [form, setForm] = useState(null)
  const qc = useQueryClient()

  if (q.isError) return <ErrorState error={q.error} retry={q.refetch} />

  async function save(e) {
    e.preventDefault()
    if (form.id) await api.put(`/api/assets/${form.id}`, form)
    else await api.post('/api/assets', form)
    setForm(null)
    qc.invalidateQueries({ queryKey: ['assets'] })
    qc.invalidateQueries({ queryKey: ['risk-overview'] })
  }

  return (
    <>
      <PageHeader title="Assets" subtitle="Inventory, criticality, and residual risk."
        actions={<WriterOnly><button className="btn" onClick={() => setForm({ ...empty })}>Add Asset</button></WriterOnly>} />
      <div className="filters">
        <input placeholder="Type" onBlur={(e) => setParams({ ...params, type: e.target.value || undefined, page: 0 })} />
        <select onChange={(e) => setParams({ ...params, criticality: e.target.value || undefined, page: 0 })}>
          <option value="">All criticality</option>
          {['CRITICAL','HIGH','MEDIUM','LOW'].map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>
      <div className="card">
        {q.isLoading ? <SkeletonRows n={6} /> : (
          <table className="table">
            <thead><tr><th>Name</th><th>Type</th><th>Criticality</th><th>Risk</th><th>Exposure</th><th>Owner</th></tr></thead>
            <tbody>
              {(q.data?.content || []).map((a) => (
                <tr key={a.id} className="clickable" onClick={() => setSelected(a)}>
                  <td>{a.name}</td><td>{a.type}</td><td><SeverityBadge value={a.criticality} /></td>
                  <td>{a.riskScore}</td><td>{inr(a.financialExposure)}</td><td>{a.owner}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <div className="row space" style={{ marginTop: 12 }}>
          <button className="btn secondary" disabled={params.page === 0} onClick={() => setParams({ ...params, page: params.page - 1 })}>Prev</button>
          <span className="muted">Page {params.page + 1} / {q.data?.totalPages || 1}</span>
          <button className="btn secondary" disabled={params.page + 1 >= (q.data?.totalPages || 1)} onClick={() => setParams({ ...params, page: params.page + 1 })}>Next</button>
        </div>
      </div>
      {selected && (
        <>
          <div className="overlay" onClick={() => setSelected(null)} />
          <aside className="drawer">
            <h3>{selected.name}</h3>
            <p className="muted">{selected.type} · {selected.category}</p>
            <p>Risk {selected.riskScore} · {inr(selected.financialExposure)}</p>
            <p>Owner {selected.owner || '—'} · Status {selected.status}</p>
            <WriterOnly>
              <button className="btn secondary" onClick={() => { setForm(selected); setSelected(null) }}>Edit</button>
            </WriterOnly>
          </aside>
        </>
      )}
      {form && (
        <form className="card" onSubmit={save}>
          <h3>{form.id ? 'Edit asset' : 'Add asset'}</h3>
          {['name','type','category','owner'].map((k) => (
            <div className="field" key={k}><label>{k}</label><input value={form[k] || ''} onChange={(e) => setForm({ ...form, [k]: e.target.value })} /></div>
          ))}
          <div className="field"><label>criticality</label>
            <select value={form.criticality} onChange={(e) => setForm({ ...form, criticality: e.target.value })}>
              {['CRITICAL','HIGH','MEDIUM','LOW'].map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="field"><label>financialExposure</label>
            <input type="number" value={form.financialExposure} onChange={(e) => setForm({ ...form, financialExposure: Number(e.target.value) })} />
          </div>
          <div className="row"><button className="btn">Save</button><button type="button" className="btn secondary" onClick={() => setForm(null)}>Cancel</button></div>
        </form>
      )}
    </>
  )
}

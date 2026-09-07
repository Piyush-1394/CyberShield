import { useState } from 'react'
import { useAssets, usePatchVuln, useVulns } from '../api/hooks'
import { ErrorState, PageHeader, SeverityBadge, SkeletonRows } from '../components/ui'
import { WriterOnly } from '../auth/ProtectedRoute'
import { api } from '../api/client'
import { useQueryClient } from '@tanstack/react-query'

const empty = {
  title: '', description: '', severity: 'HIGH', cveId: '', assetId: '', status: 'OPEN',
  discoveredDate: new Date().toISOString().slice(0, 10), remediationDeadline: '',
}

export default function Vulnerabilities() {
  const [params, setParams] = useState({ page: 0, size: 20 })
  const q = useVulns(params)
  const assets = useAssets({ page: 0, size: 100, sort: 'name,asc' })
  const patch = usePatchVuln()
  const [detail, setDetail] = useState(null)
  const [form, setForm] = useState(null)
  const qc = useQueryClient()

  if (q.isError) return <ErrorState error={q.error} retry={q.refetch} />

  async function save(e) {
    e.preventDefault()
    const payload = { ...form, assetId: Number(form.assetId), remediationDeadline: form.remediationDeadline || null }
    await api.post('/api/vulnerabilities', payload)
    setForm(null)
    qc.invalidateQueries({ queryKey: ['vulns'] })
    qc.invalidateQueries({ queryKey: ['risk-overview'] })
  }

  return (
    <>
      <PageHeader title="Vulnerabilities" subtitle="Open findings driving residual asset risk."
        actions={<WriterOnly><button className="btn" onClick={() => setForm({ ...empty, assetId: assets.data?.content?.[0]?.id || '' })}>Add vulnerability</button></WriterOnly>} />
      <div className="filters">
        <select onChange={(e) => setParams({ ...params, severity: e.target.value || undefined, page: 0 })}>
          <option value="">All severity</option>
          {['CRITICAL','HIGH','MEDIUM','LOW'].map((s) => <option key={s}>{s}</option>)}
        </select>
        <select onChange={(e) => setParams({ ...params, status: e.target.value || undefined, page: 0 })}>
          <option value="">All status</option>
          {['OPEN','IN_PROGRESS','PATCHED','ACCEPTED_RISK'].map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>
      {form && (
        <form className="card" onSubmit={save}>
          <h3>New vulnerability</h3>
          <div className="field"><label>Title</label><input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required /></div>
          <div className="field"><label>Description</label><textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
          <div className="field"><label>Asset</label>
            <select value={form.assetId} onChange={(e) => setForm({ ...form, assetId: e.target.value })} required>
              <option value="">Select asset</option>
              {(assets.data?.content || []).map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}
            </select>
          </div>
          <div className="row wrap">
            <select value={form.severity} onChange={(e) => setForm({ ...form, severity: e.target.value })}>
              {['CRITICAL','HIGH','MEDIUM','LOW'].map((s) => <option key={s}>{s}</option>)}
            </select>
            <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
              {['OPEN','IN_PROGRESS','PATCHED','ACCEPTED_RISK'].map((s) => <option key={s}>{s}</option>)}
            </select>
            <input placeholder="CVE (optional)" value={form.cveId} onChange={(e) => setForm({ ...form, cveId: e.target.value })} />
            <input type="date" value={form.discoveredDate} onChange={(e) => setForm({ ...form, discoveredDate: e.target.value })} />
          </div>
          <div className="row" style={{ marginTop: 12 }}>
            <button className="btn">Save</button>
            <button type="button" className="btn secondary" onClick={() => setForm(null)}>Cancel</button>
          </div>
        </form>
      )}
      <div className="card">
        {q.isLoading ? <SkeletonRows n={6} /> : (
          <table className="table">
            <thead><tr><th>Title</th><th>Asset</th><th>Severity</th><th>Status</th><th>CVE</th><th></th></tr></thead>
            <tbody>
              {(q.data?.content || []).map((v) => (
                <tr key={v.id} className="clickable" onClick={() => setDetail(v)}>
                  <td>{v.title}</td><td>{v.assetName}</td>
                  <td><SeverityBadge value={v.severity} /></td><td>{v.status}</td><td>{v.cveId || '—'}</td>
                  <td>
                    <WriterOnly>
                      {v.status !== 'PATCHED' && (
                        <button className="btn secondary" onClick={(e) => { e.stopPropagation(); patch.mutate(v.id) }}>Mark as Patched</button>
                      )}
                    </WriterOnly>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
      {detail && (
        <div className="card">
          <h3>{detail.title}</h3>
          <p>{detail.description}</p>
          <p className="muted">{detail.assetName} · discovered {detail.discoveredDate} · due {detail.remediationDeadline || 'n/a'}</p>
        </div>
      )}
    </>
  )
}

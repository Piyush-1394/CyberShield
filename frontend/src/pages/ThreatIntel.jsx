import { useState } from 'react'
import { useAssets, useThreats } from '../api/hooks'
import { ErrorState, PageHeader, SeverityBadge, SkeletonRows } from '../components/ui'
import { WriterOnly } from '../auth/ProtectedRoute'
import { api } from '../api/client'
import { useQueryClient } from '@tanstack/react-query'

const empty = {
  title: '', threatActor: '', threatType: 'Ransomware', severity: 'HIGH', source: 'CyberShield Intel Feed',
  publishedDate: new Date().toISOString().slice(0, 10), description: '', assetIds: [],
}

export default function ThreatIntel() {
  const [params, setParams] = useState({ page: 0, size: 20 })
  const q = useThreats(params)
  const assets = useAssets({ page: 0, size: 100, sort: 'name,asc' })
  const [detail, setDetail] = useState(null)
  const [form, setForm] = useState(null)
  const qc = useQueryClient()
  if (q.isError) return <ErrorState error={q.error} retry={q.refetch} />

  async function save(e) {
    e.preventDefault()
    await api.post('/api/threat-intel', { ...form, assetIds: form.assetIds.map(Number) })
    setForm(null)
    qc.invalidateQueries({ queryKey: ['threats'] })
    qc.invalidateQueries({ queryKey: ['risk-overview'] })
  }

  return (
    <>
      <PageHeader title="Threat Intelligence" subtitle="Actor activity mapped to your asset inventory."
        actions={<WriterOnly><button className="btn" onClick={() => setForm({ ...empty })}>Add threat</button></WriterOnly>} />
      <div className="filters">
        <select onChange={(e) => setParams({ ...params, severity: e.target.value || undefined, page: 0 })}>
          <option value="">All severity</option>
          {['CRITICAL','HIGH','MEDIUM','LOW'].map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>
      {form && (
        <form className="card" onSubmit={save}>
          <h3>New threat entry</h3>
          <div className="field"><label>Title</label><input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required /></div>
          <div className="field"><label>Actor</label><input value={form.threatActor} onChange={(e) => setForm({ ...form, threatActor: e.target.value })} /></div>
          <div className="field"><label>Type</label><input value={form.threatType} onChange={(e) => setForm({ ...form, threatType: e.target.value })} /></div>
          <div className="field"><label>Severity</label>
            <select value={form.severity} onChange={(e) => setForm({ ...form, severity: e.target.value })}>
              {['CRITICAL','HIGH','MEDIUM','LOW'].map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="field"><label>Related assets</label>
            <select multiple value={form.assetIds} onChange={(e) => setForm({ ...form, assetIds: [...e.target.selectedOptions].map((o) => o.value) })}>
              {(assets.data?.content || []).map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}
            </select>
          </div>
          <div className="field"><label>Description</label><textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
          <div className="row">
            <button className="btn">Save</button>
            <button type="button" className="btn secondary" onClick={() => setForm(null)}>Cancel</button>
          </div>
        </form>
      )}
      <div className="card">
        {q.isLoading ? <SkeletonRows /> : (q.data?.content || []).map((t) => (
          <article key={t.id} className="clickable" style={{ padding: '12px 0', borderBottom: '1px solid var(--line)', cursor: 'pointer' }} onClick={() => setDetail(t)}>
            <SeverityBadge value={t.severity} /> <strong>{t.title}</strong>
            <div className="muted">{t.threatActor} · {t.threatType} · {t.publishedDate} · {t.source}</div>
          </article>
        ))}
      </div>
      {detail && (
        <div className="card">
          <h3>{detail.title}</h3>
          <p>{detail.description}</p>
          <p className="muted">Related: {(detail.relatedAssets || []).map((a) => a.name).join(', ') || 'none'}</p>
        </div>
      )}
    </>
  )
}

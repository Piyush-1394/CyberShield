export function SeverityBadge({ value }) {
  const v = (value || '').toLowerCase()
  const cls = v.includes('crit') ? 'crit' : v.includes('high') ? 'high' : v.includes('low') ? 'low' : 'med'
  return <span className={`badge ${cls}`}>{value}</span>
}

export function SkeletonRows({ n = 4 }) {
  return (
    <div className="grid" style={{ gap: 8 }}>
      {Array.from({ length: n }).map((_, i) => <div key={i} className="skel" />)}
    </div>
  )
}

export function ErrorState({ error, retry }) {
  return (
    <div className="card">
      <p className="error">{error?.response?.data?.message || error?.message || 'Something went wrong'}</p>
      {retry && <button className="btn secondary" onClick={retry}>Retry</button>}
    </div>
  )
}

export function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="page-head">
      <div>
        <h2>{title}</h2>
        {subtitle && <p className="muted">{subtitle}</p>}
      </div>
      <div className="row wrap">{actions}</div>
    </div>
  )
}

export function Sparkline({ points = [] }) {
  if (!points.length) return <div className="muted">No trend data yet</div>
  const ys = points.map((p) => Number(p.score ?? p.y ?? 0))
  const min = Math.min(...ys)
  const max = Math.max(...ys)
  const span = Math.max(1, max - min)
  const d = ys.map((y, i) => {
    const x = (i / Math.max(1, ys.length - 1)) * 300
    const py = 100 - ((y - min) / span) * 90
    return `${i === 0 ? 'M' : 'L'}${x},${py}`
  }).join(' ')
  return (
    <svg className="spark" viewBox="0 0 300 110" preserveAspectRatio="none">
      <path d={d} fill="none" stroke="#3ee0c4" strokeWidth="3" />
    </svg>
  )
}

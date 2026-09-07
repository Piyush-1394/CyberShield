import { useAlerts, useMarkAllRead } from '../api/hooks'
import { ErrorState, PageHeader, SeverityBadge, SkeletonRows } from '../components/ui'

export default function Alerts() {
  const q = useAlerts({ page: 0, size: 50 })
  const mark = useMarkAllRead()
  if (q.isError) return <ErrorState error={q.error} retry={q.refetch} />
  return (
    <>
      <PageHeader title="Alerts" subtitle="Organization-wide risk notifications."
        actions={<button className="btn secondary" onClick={() => mark.mutate()}>Mark all as read</button>} />
      <div className="card">
        {q.isLoading ? <SkeletonRows /> : (q.data?.content || []).map((a) => (
          <div key={a.id} className="row space" style={{ padding: '10px 0', borderBottom: '1px solid var(--line)' }}>
            <div>
              <SeverityBadge value={a.severity} /> <strong>{a.title}</strong>
              <div className="muted">{a.message} · {a.assetName || 'unlinked'}</div>
            </div>
            <span className="muted">{a.read ? 'Read' : 'Unread'}</span>
          </div>
        ))}
      </div>
    </>
  )
}

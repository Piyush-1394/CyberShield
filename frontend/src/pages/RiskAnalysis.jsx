import { useState } from 'react'
import { useRiskDistribution, useRiskTrend } from '../api/hooks'
import { ErrorState, PageHeader, SkeletonRows, Sparkline } from '../components/ui'
import { inr } from '../api/client'

export default function RiskAnalysis() {
  const [days, setDays] = useState(30)
  const trend = useRiskTrend(days)
  const dist = useRiskDistribution()
  if (trend.isError) return <ErrorState error={trend.error} retry={trend.refetch} />
  return (
    <>
      <PageHeader title="Risk Analysis" subtitle="Category breakdown and historical residual risk."
        actions={
          <select value={days} onChange={(e) => setDays(Number(e.target.value))}>
            {[7, 14, 30, 90].map((d) => <option key={d} value={d}>{d} days</option>)}
          </select>
        } />
      <div className="grid cols-2">
        <div className="card">
          <h3>Trend</h3>
          {trend.isLoading ? <SkeletonRows /> : <Sparkline points={trend.data?.series || []} />}
          <table className="table">
            <thead><tr><th>Date</th><th>Score</th><th>Exposure</th></tr></thead>
            <tbody>
              {(trend.data?.series || []).slice(-10).map((p) => (
                <tr key={p.date}><td>{p.date}</td><td>{p.score}</td><td>{inr(p.financialExposure)}</td></tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="card">
          <h3>By category</h3>
          {dist.isLoading ? <SkeletonRows /> : (dist.data?.categories || []).map((c) => (
            <div key={c.name} className="row space" style={{ marginBottom: 10 }}>
              <span>{c.name}</span><strong>{Number(c.score).toFixed(1)}</strong>
            </div>
          ))}
        </div>
      </div>
    </>
  )
}

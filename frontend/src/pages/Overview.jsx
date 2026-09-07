import { useNavigate } from 'react-router-dom'
import { useAiSummary, useAlerts, useRecommendations, useRiskDistribution, useRiskOverview, useScenarios, useTopAssets } from '../api/hooks'
import { ErrorState, SeverityBadge, SkeletonRows, Sparkline } from '../components/ui'
import { inr } from '../api/client'
import { useState } from 'react'
import { api } from '../api/client'

export default function Overview() {
  const nav = useNavigate()
  const overview = useRiskOverview()
  const dist = useRiskDistribution()
  const top = useTopAssets()
  const rec = useRecommendations()
  const alerts = useAlerts({ page: 0, limit: 3 })
  const ai = useAiSummary()
  const scenarios = useScenarios()
  const [scenarioId, setScenarioId] = useState('')
  const [sim, setSim] = useState(null)

  if (overview.isError) return <ErrorState error={overview.error} retry={overview.refetch} />

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Overview</h2>
          <p className="muted">Live risk, investment, and billing posture for your organization.</p>
        </div>
      </div>
      <div className="grid kpis">
        <div className="card kpi">
          <div className="muted">Overall risk</div>
          <div className="value">{overview.data ? Number(overview.data.overallRiskScore).toFixed(1) : '—'}</div>
          <div className="muted">30d Δ {overview.data?.thirtyDayDelta ?? '—'}</div>
        </div>
        <div className="card kpi">
          <div className="muted">Financial exposure</div>
          <div className="value">{overview.data ? inr(overview.data.financialExposure) : '—'}</div>
        </div>
        <div className="card kpi">
          <div className="muted">Critical assets at risk</div>
          <div className="value">{overview.data?.criticalAssetsAtRisk ?? '—'}</div>
        </div>
        <div className="card kpi">
          <div className="muted">Top threat level</div>
          <div className="value">{overview.data?.topThreatLevel ?? '—'}</div>
          <button className="btn ghost" onClick={() => nav('/app/threat-intel')}>View Threats →</button>
        </div>
      </div>
      <div className="grid cols-2">
        <div className="card">
          <h3>Risk distribution</h3>
          {dist.isLoading ? <SkeletonRows /> : (
            <div className="row" style={{ gap: 24 }}>
              <div className="donut" />
              <div>
                {(dist.data?.categories || []).map((c) => (
                  <div key={c.name} className="row space" style={{ marginBottom: 8 }}>
                    <span>{c.name}</span><strong>{Number(c.score).toFixed(1)}</strong>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
        <div className="card">
          <h3>30-day trend</h3>
          {overview.isLoading ? <SkeletonRows /> : <Sparkline points={overview.data?.trend || []} />}
        </div>
      </div>
      <div className="grid cols-2">
        <div className="card">
          <div className="row space"><h3>Top risky assets</h3><button className="btn ghost" onClick={() => nav('/app/assets')}>View all assets →</button></div>
          {top.isLoading ? <SkeletonRows /> : (
            <table className="table">
              <thead><tr><th>Asset</th><th>Risk</th><th>Exposure</th></tr></thead>
              <tbody>
                {(top.data || []).map((a) => (
                  <tr key={a.id}><td>{a.name}</td><td><SeverityBadge value={a.criticality} /> {a.riskScore}</td><td>{inr(a.financialExposure)}</td></tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
        <div className="card">
          <h3>Investment recommendations</h3>
          {rec.isLoading ? <SkeletonRows /> : (rec.data?.actions || []).filter((a) => a.selected).slice(0, 5).map((a) => (
            <div key={a.id} className="row space" style={{ marginBottom: 8 }}>
              <span>{a.name}</span><span className="muted">{inr(a.cost)} · {a.riskReductionPercent}%</span>
            </div>
          ))}
          <button className="btn secondary" onClick={() => nav('/app/investment')}>Open optimizer</button>
        </div>
      </div>
      <div className="grid cols-2">
        <div className="card">
          <h3>What-if</h3>
          <div className="row wrap">
            <select value={scenarioId} onChange={(e) => setScenarioId(e.target.value)}>
              <option value="">Choose scenario</option>
              {(scenarios.data || []).map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
            <button className="btn secondary" disabled={!scenarioId} onClick={async () => {
              const { data } = await api.post('/api/whatif/simulate', { scenarioId: Number(scenarioId) })
              setSim(data)
            }}>Simulate</button>
            <button className="btn ghost" onClick={() => nav('/app/whatif')}>Run Custom Scenario →</button>
          </div>
          {sim && <p>Additional risk {inr(sim.additionalRisk)} ({sim.percentIncrease}%)</p>}
        </div>
        <div className="card">
          <div className="row space"><h3>Recent alerts</h3><button className="btn ghost" onClick={() => nav('/app/alerts')}>View all alerts →</button></div>
          {(alerts.data?.content || []).map((a) => (
            <p key={a.id}><SeverityBadge value={a.severity} /> {a.title}</p>
          ))}
        </div>
      </div>
      <div className="card">
        <h3>AI risk summary</h3>
        {ai.isLoading ? <SkeletonRows /> : <p>{ai.data?.summary}</p>}
      </div>
    </>
  )
}

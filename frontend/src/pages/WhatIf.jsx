import { useState } from 'react'
import { useRecommendations, useScenarios, useSimulate } from '../api/hooks'
import { ErrorState, PageHeader } from '../components/ui'
import { inr } from '../api/client'

export default function WhatIf() {
  const scenarios = useScenarios()
  const rec = useRecommendations()
  const sim = useSimulate()
  const [controlId, setControlId] = useState('')
  const [delayDays, setDelayDays] = useState(30)
  if (scenarios.isError) return <ErrorState error={scenarios.error} retry={scenarios.refetch} />

  return (
    <>
      <PageHeader title="What-if Analysis" subtitle="Delay a control and quantify extra residual exposure." />
      <div className="grid cols-2">
        <div className="card">
          <h3>Templates</h3>
          {(scenarios.data || []).map((s) => (
            <button key={s.id} className="btn secondary" style={{ margin: 4 }} onClick={() => { setControlId(s.controlId); setDelayDays(s.delayDays); sim.mutate({ scenarioId: s.id, controlId: s.controlId, delayDays: s.delayDays }) }}>
              {s.name}
            </button>
          ))}
          <form onSubmit={(e) => { e.preventDefault(); sim.mutate({ controlId: Number(controlId), delayDays: Number(delayDays) }) }}>
            <div className="field">
              <label>Control</label>
              <select value={controlId} onChange={(e) => setControlId(e.target.value)} required>
                <option value="">Select</option>
                {(rec.data?.actions || []).map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}
              </select>
            </div>
            <div className="field"><label>Delay days</label><input type="number" value={delayDays} onChange={(e) => setDelayDays(e.target.value)} /></div>
            <button className="btn">Run custom scenario</button>
          </form>
        </div>
        <div className="card">
          <h3>Result</h3>
          {sim.data ? (
            <ul>
              <li>Current exposure {inr(sim.data.currentExposure)}</li>
              <li>With control {inr(sim.data.withControlExposure)}</li>
              <li>Delayed {inr(sim.data.delayedExposure)}</li>
              <li>Additional risk {inr(sim.data.additionalRisk)} ({sim.data.percentIncrease}%)</li>
            </ul>
          ) : <p className="muted">Run a scenario to see computed exposure deltas.</p>}
        </div>
      </div>
    </>
  )
}

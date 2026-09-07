import { useApplyInvestment, useProjection, useRecommendations } from '../api/hooks'
import { ErrorState, PageHeader, SkeletonRows, Sparkline } from '../components/ui'
import { inr } from '../api/client'
import { WriterOnly } from '../auth/ProtectedRoute'
import { useState } from 'react'

export default function Investment() {
  const rec = useRecommendations()
  const proj = useProjection()
  const apply = useApplyInvestment()
  const [result, setResult] = useState(null)
  if (rec.isError) return <ErrorState error={rec.error} retry={rec.refetch} />
  const selected = (rec.data?.actions || []).filter((a) => a.selected).map((a) => a.id)

  return (
    <>
      <PageHeader title="Investment Optimizer" subtitle="Greedy value/cost selection within remaining budget."
        actions={
          <WriterOnly>
            <button className="btn" disabled={apply.isPending} onClick={() => apply.mutateAsync(selected).then(setResult)}>
              Apply Recommendations
            </button>
          </WriterOnly>
        } />
      <div className="grid cols-2">
        <div className="card">
          <p>Budget available {inr(rec.data?.budgetAvailable)} · expected reduction {rec.data?.expectedReductionPercent}%</p>
          {rec.isLoading ? <SkeletonRows /> : (
            <table className="table">
              <thead><tr><th>Action</th><th>Cost</th><th>Reduction</th><th>In plan</th></tr></thead>
              <tbody>
                {(rec.data?.actions || []).map((a) => (
                  <tr key={a.id}><td>{a.name}</td><td>{inr(a.cost)}</td><td>{a.riskReductionPercent}%</td><td>{a.selected ? 'Yes' : 'No'}</td></tr>
                ))}
              </tbody>
            </table>
          )}
          {result && <p>Applied. Remaining budget {inr(result.budgetRemaining)}. Overall risk now {result.overallRisk}.</p>}
        </div>
        <div className="card">
          <h3>Current vs projected</h3>
          {proj.isLoading ? <SkeletonRows /> : (
            <>
              <p>Current {proj.data?.currentRisk} → projected {proj.data?.projectedRisk}</p>
              <Sparkline points={(proj.data?.projectedCurve || []).map((p) => ({ score: p.score }))} />
            </>
          )}
        </div>
      </div>
    </>
  )
}

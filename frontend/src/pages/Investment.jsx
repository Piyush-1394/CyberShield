import { useApplyInvestment, useProjection, useRecommendations } from '../api/hooks'
import { ErrorState, PageHeader, SkeletonRows, Sparkline } from '../components/ui'
import { api, inr } from '../api/client'
import { WriterOnly } from '../auth/ProtectedRoute'
import { useAuth } from '../auth/AuthContext'
import { useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'

export default function Investment() {
  const [budgetInput, setBudgetInput] = useState('')
  const [activeBudget, setActiveBudget] = useState(null)
  const [saveStatus, setSaveStatus] = useState('')
  const { isAdmin } = useAuth()
  const qc = useQueryClient()

  const rec = useRecommendations(activeBudget)
  const proj = useProjection(activeBudget)
  const apply = useApplyInvestment()
  const [result, setResult] = useState(null)

  useEffect(() => {
    if (rec.data?.budgetAvailable && !budgetInput && activeBudget == null) {
      setBudgetInput(String(rec.data.budgetAvailable))
    }
  }, [rec.data])

  if (rec.isError) return <ErrorState error={rec.error} retry={rec.refetch} />
  const selected = (rec.data?.actions || []).filter((a) => a.selected).map((a) => a.id)

  const handleSimulate = (e) => {
    if (e) e.preventDefault()
    const val = Number(budgetInput)
    if (!isNaN(val) && val >= 0) {
      setActiveBudget(val)
    }
  }

  const handleSaveAsOrgBudget = async () => {
    try {
      const val = Number(budgetInput)
      await api.put('/api/settings/organization', { name: 'ABC University', budgetAvailable: val })
      qc.invalidateQueries({ queryKey: ['org'] })
      qc.invalidateQueries({ queryKey: ['invest-rec'] })
      setSaveStatus('✓ Saved as organization default budget!')
      setTimeout(() => setSaveStatus(''), 3000)
    } catch {
      setSaveStatus('Failed to save budget.')
    }
  }

  const setPreset = (amount) => {
    setBudgetInput(String(amount))
    setActiveBudget(amount)
  }

  return (
    <>
      <PageHeader title="Investment Optimizer" subtitle="0/1 Knapsack optimization algorithm maximizing risk reduction within your budget."
        actions={
          <WriterOnly>
            <button className="btn" disabled={apply.isPending || selected.length === 0} onClick={() => apply.mutateAsync(selected).then(setResult)}>
              Apply Recommendations ({selected.length})
            </button>
          </WriterOnly>
        } />

      <div className="card" style={{ marginBottom: 16 }}>
        <form onSubmit={handleSimulate} style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'center' }}>
          <div style={{ flex: '1 1 220px' }}>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, marginBottom: 4 }}>
              Optimization Budget (₹)
            </label>
            <input
              type="number"
              min="0"
              step="50000"
              placeholder="e.g. 2500000"
              value={budgetInput}
              onChange={(e) => setBudgetInput(e.target.value)}
              style={{ width: '100%', padding: '8px 12px', fontSize: 14 }}
            />
          </div>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 18, flexWrap: 'wrap' }}>
            <button type="submit" className="btn">Recalculate Plan</button>
            {isAdmin && (
              <button type="button" className="btn secondary" onClick={handleSaveAsOrgBudget}>
                Save as Default
              </button>
            )}
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
              <button type="button" className="tag" onClick={() => setPreset(500000)}>₹5L</button>
              <button type="button" className="tag" onClick={() => setPreset(1000000)}>₹10L</button>
              <button type="button" className="tag" onClick={() => setPreset(2500000)}>₹25L</button>
              <button type="button" className="tag" onClick={() => setPreset(5000000)}>₹50L</button>
              {activeBudget != null && (
                <button type="button" className="tag" style={{ background: '#374151' }} onClick={() => { setActiveBudget(null); setBudgetInput(''); }}>
                  Reset
                </button>
              )}
            </div>
          </div>
        </form>
        {saveStatus && <p style={{ color: '#10b981', fontSize: 13, marginTop: 8 }}>{saveStatus}</p>}
      </div>

      <div className="grid cols-2">
        <div className="card">
          <p>
            <strong>Budget evaluated:</strong> {inr(rec.data?.budgetAvailable)} · <strong>Remaining:</strong> {inr(rec.data?.remainingBudget)} · <strong>Expected reduction:</strong> {rec.data?.expectedReductionPercent}%
          </p>
          {rec.isLoading ? <SkeletonRows /> : (
            <table className="table">
              <thead><tr><th>Security Action</th><th>Cost</th><th>Risk Reduction</th><th>Selected by Knapsack</th></tr></thead>
              <tbody>
                {(rec.data?.actions || []).map((a) => (
                  <tr key={a.id} style={{ background: a.selected ? 'rgba(16, 185, 129, 0.08)' : 'transparent' }}>
                    <td><strong>{a.name}</strong></td>
                    <td>{inr(a.cost)}</td>
                    <td><span className="tag green">-{a.riskReductionPercent}%</span></td>
                    <td>
                      {a.selected ? (
                        <span style={{ color: '#10b981', fontWeight: 600 }}>✓ Included</span>
                      ) : (
                        <span className="muted">Exceeds Budget</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {result && <p style={{ color: '#10b981', marginTop: 12 }}>✓ Applied! Remaining budget {inr(result.budgetRemaining)}. Overall risk reduced to {result.overallRisk}.</p>}
        </div>
        <div className="card">
          <h3>Current vs Projected Risk Curve</h3>
          {proj.isLoading ? <SkeletonRows /> : (
            <>
              <p>Current Risk Score: <strong>{proj.data?.currentRisk}</strong> → Projected: <strong style={{ color: '#10b981' }}>{proj.data?.projectedRisk}</strong></p>
              <Sparkline points={(proj.data?.projectedCurve || []).map((p) => ({ score: p.score }))} />
            </>
          )}
        </div>
      </div>
    </>
  )
}

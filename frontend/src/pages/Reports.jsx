import { useReports } from '../api/hooks'
import { ErrorState, PageHeader, SkeletonRows } from '../components/ui'
import { api, downloadBlob } from '../api/client'
import { WriterOnly } from '../auth/ProtectedRoute'
import { useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'

export default function Reports() {
  const q = useReports()
  const qc = useQueryClient()
  const [type, setType] = useState('RISK_SUMMARY')
  const [format, setFormat] = useState('PDF')
  if (q.isError) return <ErrorState error={q.error} retry={q.refetch} />

  return (
    <>
      <PageHeader title="Reports" subtitle="Generate PDF or CSV exports from live org data."
        actions={
          <WriterOnly>
            <select value={type} onChange={(e) => setType(e.target.value)}>
              <option>RISK_SUMMARY</option>
              <option>ASSET_INVENTORY</option>
              <option>INVESTMENT_PLAN</option>
            </select>
            <select value={format} onChange={(e) => setFormat(e.target.value)}>
              <option>PDF</option>
              <option>CSV</option>
            </select>
            <button className="btn" onClick={async () => {
              await api.post('/api/reports/generate', { reportType: type, format })
              qc.invalidateQueries({ queryKey: ['reports'] })
            }}>Generate Report</button>
          </WriterOnly>
        } />
      <div className="card">
        {q.isLoading ? <SkeletonRows /> : (
          <table className="table">
            <thead><tr><th>Type</th><th>Format</th><th>Created</th><th></th></tr></thead>
            <tbody>
              {(q.data || []).map((r) => (
                <tr key={r.id}>
                  <td>{r.reportType}</td><td>{r.format}</td><td>{r.createdAt}</td>
                  <td><button className="btn ghost" onClick={async () => {
                    const res = await api.get(`/api/reports/${r.id}/download`, { responseType: 'blob' })
                    downloadBlob(res.data, `${r.reportType}.${r.format.toLowerCase()}`, r.format === 'CSV' ? 'text/csv' : 'application/pdf')
                  }}>Download</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}

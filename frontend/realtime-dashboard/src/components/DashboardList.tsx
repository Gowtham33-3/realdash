import { useEffect, useState } from "react"
import { getDashboards } from "../api/dashboardApi"
import type { Dashboard } from "../types/dashboard"
import { useNavigate } from "react-router-dom"

function DashboardList({ refreshKey }: { refreshKey: string }) {
  const [dashboards, setDashboards] = useState<Dashboard[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")
  const navigate = useNavigate()

  useEffect(() => {
    setLoading(true)
    getDashboards()
      .then(setDashboards)
      .catch(() => setError("Failed to load dashboards"))
      .finally(() => setLoading(false))
  }, [refreshKey])

  if (loading) return <div className="loading">Loading dashboards...</div>
  if (error) return <div className="error-banner">{error}</div>

  return (
    <div className="dashboard-list">
      {dashboards.length === 0 ? (
        <p className="empty-state">No dashboards yet. Create one above.</p>
      ) : (
        dashboards.map((d) => (
          <div
            key={d.id}
            className="dashboard-item"
            onClick={() => navigate(`/dashboard/${d.id}`)}
          >
            <span>{d.name}</span>
            <span className="arrow">→</span>
          </div>
        ))
      )}
    </div>
  )
}

export default DashboardList

import { useEffect, useState } from "react"
import { getDashboards } from "../api/dashboardApi"
import type { Dashboard } from "../types/dashboard"
import { useNavigate } from "react-router-dom"
import { getToken } from "../api/tokenStorage"

function DashboardList() {

  const [dashboards, setDashboards] = useState<Dashboard[]>([])
  const navigate = useNavigate()

  useEffect(() => {

    const token = getToken()

    if (!token) return


    const fetchDashboards = async () => {
      const data = await getDashboards()
      setDashboards(data)
    }

    fetchDashboards()

  }, [])

  return (
    <div>

      <h2>My Dashboards</h2>

      {dashboards.map((d) => (
        <div
          key={d.id}
          onClick={() => navigate(`/dashboard/${d.id}`)}
        >
          {d.name}
        </div>
      ))}

    </div>
  )
}

export default DashboardList
import DashboardList from "../components/DashboardList"
import CreateDashboard from "../components/CreateDashboard"
import { useState } from "react"

function DashboardPage() {
  const [refreshKey, setRefreshKey] = useState("0")

  return (
    <div className="dashboard-page">
      <h2>My Dashboards</h2>
      <CreateDashboard onCreated={() => setRefreshKey(Date.now().toString())} />
      <DashboardList refreshKey={refreshKey} />
    </div>
  )
}

export default DashboardPage

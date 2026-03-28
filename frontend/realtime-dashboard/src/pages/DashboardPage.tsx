import DashboardList from "../components/DashboardList"
import CreateDashboard from "../components/CreateDashboard"
import { useState } from "react"

function DashboardPage() {

  const [reload, setReload] = useState(false)

  const triggerReload = () => {
    setReload(!reload)
  }

  return (
    <div>

      <CreateDashboard onCreated={triggerReload} />

      <DashboardList key={reload.toString()} />

    </div>
  )
}

export default DashboardPage
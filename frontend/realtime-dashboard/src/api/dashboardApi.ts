import dashboardClient from "./dashboardClient"
import type { Dashboard } from "../types/dashboard"

export const getDashboards = async (): Promise<Dashboard[]> => {
  const res = await dashboardClient.get("/dashboards")
  return res.data
}

export const createDashboard = async (name: string): Promise<Dashboard> => {
  const res = await dashboardClient.post(`/dashboards?name=${encodeURIComponent(name)}`)
  return res.data
}
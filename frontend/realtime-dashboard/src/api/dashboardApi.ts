import dashboardClient from "./dashboardClient"
import type { Dashboard, Widget } from "../types/dashboard"

export const getDashboards = async (): Promise<Dashboard[]> => {
  const res = await dashboardClient.get("/dashboards")
  return res.data
}

export const createDashboard = async (name: string): Promise<Dashboard> => {
  const res = await dashboardClient.post(`/dashboards?name=${encodeURIComponent(name)}`)
  return res.data
}

export const addWidget = async (
  dashboardId: string,
  type: string,
  config: Record<string, unknown>
): Promise<Widget> => {
  const res = await dashboardClient.post(`/dashboards/${dashboardId}/widgets`, { type, config })
  return res.data
}

export const updateWidget = async (
  dashboardId: string,
  widgetId: string,
  config: Record<string, unknown>
): Promise<Widget> => {
  const res = await dashboardClient.put(`/dashboards/${dashboardId}/widgets/${widgetId}`, { config })
  return res.data
}

export const deleteWidget = async (dashboardId: string, widgetId: string): Promise<void> => {
  await dashboardClient.delete(`/dashboards/${dashboardId}/widgets/${widgetId}`)
}
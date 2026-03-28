export interface Widget {
  id: string
  type: string
  config: Record<string, unknown>
  dashboardId: string
}

export interface Dashboard {
  id: string
  name: string
  ownerId: string
  widgets: Widget[]
}

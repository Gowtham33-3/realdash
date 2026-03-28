export interface WidgetEvent {
  eventType: "WIDGET_ADDED" | "WIDGET_UPDATED" | "WIDGET_REMOVED"
  aggregateId: string   // dashboardId
  payload: {
    widgetId: string
    type?: string
    config?: Record<string, unknown>
  }
}

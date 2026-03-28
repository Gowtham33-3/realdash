export interface WidgetEvent {
  type: "WIDGET_CREATED" | "WIDGET_UPDATED" | "WIDGET_DELETED"
  widgetId: string
  data?: unknown
}
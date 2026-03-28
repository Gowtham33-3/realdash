import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { useDispatch, useSelector } from "react-redux"
import type { RootState } from "../store"
import {
  setActiveDashboard, setLoading, setError,
  widgetAdded, widgetUpdated, widgetRemoved,
} from "../store/dashboardSlice"
import { getDashboards, addWidget, deleteWidget } from "../api/dashboardApi"
import { connectDashboardSocket, disconnectDashboardSocket } from "../websocket/dashboardSocket"
import type { WidgetEvent } from "../types/socket"
import type { Widget } from "../types/dashboard"
import WidgetCard from "../components/WidgetCard"
import AddWidgetModal from "../components/AddWidgetModal"

function DashboardCanvasPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const { activeDashboard, loading, error } = useSelector((s: RootState) => s.dashboard)
  const [showAddModal, setShowAddModal] = useState(false)

  // load dashboard data
  useEffect(() => {
    if (!id) return
    dispatch(setLoading(true))
    getDashboards()
      .then((dashboards) => {
        const found = dashboards.find((d) => d.id === id)
        if (found) dispatch(setActiveDashboard(found))
        else dispatch(setError("Dashboard not found"))
      })
      .catch(() => dispatch(setError("Failed to load dashboard")))
      .finally(() => dispatch(setLoading(false)))
  }, [id, dispatch])

  // websocket for real-time updates
  useEffect(() => {
    if (!id) return
    connectDashboardSocket(id, (event: WidgetEvent) => {
      switch (event.eventType) {
        case "WIDGET_ADDED":
          if (event.payload.type && event.payload.config) {
            dispatch(widgetAdded({
              id: event.payload.widgetId,
              type: event.payload.type,
              config: event.payload.config,
              dashboardId: event.aggregateId,
            }))
          }
          break
        case "WIDGET_UPDATED":
          if (event.payload.config) {
            dispatch(widgetUpdated({
              id: event.payload.widgetId,
              type: "",
              config: event.payload.config,
              dashboardId: event.aggregateId,
            }))
          }
          break
        case "WIDGET_REMOVED":
          dispatch(widgetRemoved(event.payload.widgetId))
          break
      }
    })
    return () => disconnectDashboardSocket()
  }, [id, dispatch])

  const handleAddWidget = async (type: string, config: Record<string, unknown>) => {
    if (!id) return
    try {
      const widget = await addWidget(id, type, config)
      dispatch(widgetAdded(widget))
      setShowAddModal(false)
    } catch {
      dispatch(setError("Failed to add widget"))
    }
  }

  const handleDeleteWidget = async (widget: Widget) => {
    if (!id) return
    try {
      await deleteWidget(id, widget.id)
      dispatch(widgetRemoved(widget.id))
    } catch {
      dispatch(setError("Failed to delete widget"))
    }
  }

  if (loading) return <div className="loading">Loading dashboard...</div>
  if (error) return <div className="error">{error}</div>
  if (!activeDashboard) return null

  return (
    <div className="canvas-page">
      <div className="canvas-header">
        <button className="btn-back" onClick={() => navigate("/")}>← Back</button>
        <h2>{activeDashboard.name}</h2>
        <button className="btn-primary" onClick={() => setShowAddModal(true)}>+ Add Widget</button>
      </div>

      {activeDashboard.widgets.length === 0 ? (
        <div className="empty-state">
          <p>No widgets yet. Add one to get started.</p>
        </div>
      ) : (
        <div className="widget-grid">
          {activeDashboard.widgets.map((widget) => (
            <WidgetCard
              key={widget.id}
              widget={widget}
              onDelete={() => handleDeleteWidget(widget)}
            />
          ))}
        </div>
      )}

      {showAddModal && (
        <AddWidgetModal
          onAdd={handleAddWidget}
          onClose={() => setShowAddModal(false)}
        />
      )}
    </div>
  )
}

export default DashboardCanvasPage

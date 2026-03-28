import { useEffect } from "react"
import { useParams } from "react-router-dom"
import {
  connectDashboardSocket,
  disconnectDashboardSocket
} from "../websocket/dashboardSocket"
import type { WidgetEvent } from "../types/socket"

function DashboardCanvasPage() {

  const { id } = useParams()

  useEffect(() => {

    if (!id) return

    connectDashboardSocket(id, (data: WidgetEvent) => {

      switch (data.type) {

        case "WIDGET_CREATED":
          console.log("widget created", data)
          break

        case "WIDGET_UPDATED":
          console.log("widget updated", data)
          break

        case "WIDGET_DELETED":
          console.log("widget deleted", data)
          break

      }

    })

    return () => disconnectDashboardSocket()

  }, [id])

  return (
    <div>
      <h2>Dashboard {id}</h2>
    </div>
  )
}

export default DashboardCanvasPage
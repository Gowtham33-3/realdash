import { getToken } from "../api/tokenStorage"
import type { WidgetEvent } from "../types/socket"
import type { WidgetEvent } from "../types/socket"

let socket: WebSocket | null = null

export const connectDashboardSocket = (
  dashboardId: string,
  onMessage: (data: WidgetEvent) => void
) => {

  const token = getToken()

  const url = `ws://localhost:8083/ws/dashboard/${dashboardId}?token=${token}`

  socket = new WebSocket(url)

  socket.onopen = () => {
    console.log("WebSocket connected")
  }

  socket.onmessage = (event) => {

    const data: WidgetEvent = JSON.parse(event.data)

    onMessage(data)
  }

  socket.onclose = () => {
    console.log("WebSocket disconnected")
  }

  socket.onerror = (err) => {
    console.error("WebSocket error", err)
  }
}

export const disconnectDashboardSocket = () => {
  if (socket) {
    socket.close()
    socket = null
  }
}

export const sendDashboardEvent = (event: WidgetEvent) => {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(event))
  }
}
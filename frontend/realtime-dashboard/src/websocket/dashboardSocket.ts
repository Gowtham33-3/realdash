import { getToken, setToken, isTokenValid } from "../api/tokenStorage"
import { refreshToken } from "../api/authApi"
import type { WidgetEvent } from "../types/socket"

let socket: WebSocket | null = null

const getFreshToken = async (): Promise<string | null> => {
  const token = getToken()

  if (isTokenValid(token)) return token

  // token expired — try to refresh
  try {
    const data = await refreshToken()
    setToken(data.accessToken)
    return data.accessToken
  } catch {
    return null
  }
}

export const connectDashboardSocket = async (
  dashboardId: string,
  onMessage: (data: WidgetEvent) => void
) => {
  const token = await getFreshToken()

  if (!token) {
    console.warn("No valid token for WebSocket — redirecting to login")
    window.location.href = "/login"
    return
  }

  const url = `ws://localhost:8083/ws/dashboard/${dashboardId}?token=${token}`
  socket = new WebSocket(url)

  socket.onopen = () => console.log("WebSocket connected")

  socket.onmessage = (event) => {
    try {
      const data: WidgetEvent = JSON.parse(event.data)
      onMessage(data)
    } catch {
      console.warn("Failed to parse WebSocket message", event.data)
    }
  }

  socket.onclose = () => console.log("WebSocket disconnected")

  socket.onerror = (err) => console.error("WebSocket error", err)
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

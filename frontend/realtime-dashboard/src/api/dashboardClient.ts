import axios from "axios"
import { getToken, setToken, removeToken } from "./tokenStorage"
import { refreshToken } from "./authApi"

// extend axios types to allow our custom _retry flag
declare module "axios" {
  interface AxiosRequestConfig {
    _retry?: boolean
  }
}

const dashboardClient = axios.create({
  baseURL: "http://localhost:8082"
})

// attach bearer token on each request
dashboardClient.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// response interceptor with refresh and retry logic
let isRefreshing = false

interface QueueItem {
  resolve: (token: string) => void
  reject: (error: unknown) => void
}

let failedQueue: QueueItem[] = []

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else if (token) {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

dashboardClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    console.log("API Error:", error.response?.status, error.response?.data)

    if (
      (error.response?.status === 401 || error.response?.status === 403) &&
      !originalRequest._retry
    ) {
      if (isRefreshing) {
        // queue the request until refresh finished
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return dashboardClient(originalRequest)
          })
          .catch((err: unknown) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const data = await refreshToken()
        const newToken = data.accessToken
        setToken(newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        processQueue(null, newToken)
        return dashboardClient(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        removeToken()
        window.location.href = "/login"
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // if not handleable, or retry already attempted
    if (error.response?.status === 403) {
      window.location.href = "/login"
    }

    return Promise.reject(error)
  }
)

export default dashboardClient
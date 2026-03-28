import { Navigate } from "react-router-dom"
import { getToken, isTokenValid } from "../api/tokenStorage"
import type { ReactNode } from "react"

function ProtectedRoute({ children }: { children: ReactNode }) {

  const token = getToken()
  const isValid = isTokenValid(token)

  console.log("ProtectedRoute: token =", token, "isValid =", isValid)

  if (!isValid) {
    console.log("Redirecting to /login")
    return <Navigate to="/login" replace />
  }

  return children
}

export default ProtectedRoute
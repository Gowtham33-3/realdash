import { useState } from "react"
import { login } from "../api/authApi"
import { setToken } from "../api/tokenStorage"
import { useNavigate } from "react-router-dom"

function LoginPage() {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleLogin = async () => {
    if (!email.trim() || !password.trim()) {
      setError("Email and password are required")
      return
    }
    setError("")
    setLoading(true)
    try {
      const res = await login(email, password)
      setToken(res.accessToken)
      navigate("/")
    } catch {
      setError("Invalid credentials. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") handleLogin()
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Sign In</h2>

        {error && <div className="error-banner">{error}</div>}

        <input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          onKeyDown={handleKeyDown}
          autoComplete="email"
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={handleKeyDown}
          autoComplete="current-password"
        />

        <button className="btn-primary" onClick={handleLogin} disabled={loading}>
          {loading ? "Signing in..." : "Sign In"}
        </button>
      </div>
    </div>
  )
}

export default LoginPage

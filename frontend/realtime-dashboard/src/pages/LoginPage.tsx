import { useState } from "react"
import { login } from "../api/authApi"
import { setToken } from "../api/tokenStorage"
import { useNavigate } from "react-router-dom"

function LoginPage() {

  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const navigate = useNavigate()

  const handleLogin = async () => {

    const res = await login(email, password)
    console.log("result for token",res)
    setToken(res.accessToken)

    navigate("/")
  }

  return (
    <div>

      <h2>Login</h2>

      <input
        placeholder="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />

      <input
        type="password"
        placeholder="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />

      <button onClick={handleLogin}>
        Login
      </button>

    </div>
  )
}

export default LoginPage
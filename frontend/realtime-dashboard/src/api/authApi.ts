import authClient from "./authClient"

export const login = async (username: string, password: string) => {
  const res = await authClient.post("/auth/login", {
    username,
    password
  })

  return res.data
}

// use this to refresh the access token; refresh token is sent via cookie automatically
export const refreshToken = async () => {
  const res = await authClient.post("/auth/refresh")
  return res.data as { accessToken: string }
}
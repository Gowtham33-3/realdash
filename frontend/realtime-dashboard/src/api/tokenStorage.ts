export const setToken = (token: string) => {
  localStorage.setItem("token", token)
}

export const getToken = () => {
  return localStorage.getItem("token")
}

export const removeToken = () => {
  localStorage.removeItem("token")
}

export const isTokenValid = (token: string | null): boolean => {
  if (!token) return false

  try {
    // Decode JWT payload (second part)
    const payload = JSON.parse(atob(token.split('.')[1]))
    const currentTime = Date.now() / 1000

    // Check if token is expired
    return payload.exp > currentTime
  } catch {
    // If decoding fails, consider token invalid
    return false
  }
}
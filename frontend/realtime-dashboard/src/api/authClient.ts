import axios from "axios"

// include credentials so refresh token cookie is sent with requests
const authClient = axios.create({
  baseURL: "http://localhost:8081",
  withCredentials: true,
})

export default authClient
import { BrowserRouter, Routes, Route } from "react-router-dom"
import LoginPage from "./pages/LoginPage"
import DashboardPage from "./pages/DashboardPage"
import DashboardCanvasPage from "./pages/DashboardCanvasPage"
import ProtectedRoute from "./components/ProtectedRoute"

function App() {

  return (
    <BrowserRouter>

      <Routes>

        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard/:id"
          element={
            <ProtectedRoute>
              <DashboardCanvasPage />
            </ProtectedRoute>
          }
        />

      </Routes>

    </BrowserRouter>
  )
}

export default App
import { useState } from "react"
import { createDashboard } from "../api/dashboardApi"

function CreateDashboard({ onCreated }: { onCreated: () => void }) {
  const [name, setName] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  const handleCreate = async () => {
    if (!name.trim()) {
      setError("Name is required")
      return
    }
    setError("")
    setLoading(true)
    try {
      await createDashboard(name)
      setName("")
      onCreated()
    } catch {
      setError("Failed to create dashboard")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="create-dashboard">
      <input
        placeholder="Dashboard name"
        value={name}
        onChange={(e) => { setName(e.target.value); setError("") }}
        onKeyDown={(e) => e.key === "Enter" && handleCreate()}
      />
      <button className="btn-primary" onClick={handleCreate} disabled={loading}>
        {loading ? "Creating..." : "Create"}
      </button>
      {error && <span className="field-error">{error}</span>}
    </div>
  )
}

export default CreateDashboard

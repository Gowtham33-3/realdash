import { useState } from "react"
import { createDashboard } from "../api/dashboardApi"

function CreateDashboard({ onCreated }: { onCreated: () => void }) {

  const [name, setName] = useState("")

  const handleCreate = async () => {
    await createDashboard(name)
    setName("")
    onCreated()
  }

  return (
    <div>

      <input
        placeholder="Dashboard name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />

      <button onClick={handleCreate}>
        Create
      </button>

    </div>
  )
}

export default CreateDashboard
import { useState } from "react"

const WIDGET_TYPES = ["chart", "table", "metric", "text"]

interface Props {
  onAdd: (type: string, config: Record<string, unknown>) => void
  onClose: () => void
}

function AddWidgetModal({ onAdd, onClose }: Props) {
  const [type, setType] = useState(WIDGET_TYPES[0])
  const [title, setTitle] = useState("")
  const [error, setError] = useState("")

  const handleSubmit = () => {
    if (!title.trim()) {
      setError("Title is required")
      return
    }
    onAdd(type, { title })
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>Add Widget</h3>

        <label>Type</label>
        <select value={type} onChange={(e) => setType(e.target.value)}>
          {WIDGET_TYPES.map((t) => (
            <option key={t} value={t}>{t}</option>
          ))}
        </select>

        <label>Title</label>
        <input
          placeholder="Widget title"
          value={title}
          onChange={(e) => { setTitle(e.target.value); setError("") }}
        />
        {error && <span className="field-error">{error}</span>}

        <div className="modal-actions">
          <button className="btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn-primary" onClick={handleSubmit}>Add</button>
        </div>
      </div>
    </div>
  )
}

export default AddWidgetModal

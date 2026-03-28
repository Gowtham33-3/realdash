import type { Widget } from "../types/dashboard"

interface Props {
  widget: Widget
  onDelete: () => void
}

function WidgetCard({ widget, onDelete }: Props) {
  return (
    <div className="widget-card">
      <div className="widget-header">
        <span className="widget-type">{widget.type}</span>
        <button className="btn-delete" onClick={onDelete} title="Delete widget">✕</button>
      </div>
      <div className="widget-config">
        <pre>{JSON.stringify(widget.config, null, 2)}</pre>
      </div>
    </div>
  )
}

export default WidgetCard

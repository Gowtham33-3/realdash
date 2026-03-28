import { useEffect, useState } from "react"
import type { Widget } from "../types/dashboard"

interface Props {
  widget: Widget
  onDelete: () => void
}

function WidgetCard({ widget, onDelete }: Props) {
  const [liveValue, setLiveValue] = useState(0)

  // Simulate live data updates for metric widgets
  useEffect(() => {
    if (widget.type === "metric") {
      const baseValue = Math.floor(Math.random() * 1000) + 100
      setLiveValue(baseValue)
      
      const interval = setInterval(() => {
        setLiveValue(prev => prev + Math.floor(Math.random() * 20) - 10)
      }, 3000)
      
      return () => clearInterval(interval)
    }
  }, [widget.type])

  const renderContent = () => {
    const config = widget.config as Record<string, unknown>
    const title = (config.title as string) || "Untitled"

    switch (widget.type) {
      case "metric":
        return (
          <div className="metric-widget">
            <div className="metric-icon">📊</div>
            <div className="metric-value">{liveValue.toLocaleString()}</div>
            <div className="metric-label">{title}</div>
            <div className="metric-trend">
              <span className="trend-up">↑ {Math.floor(Math.random() * 15) + 5}%</span>
            </div>
          </div>
        )

      case "chart":
        return (
          <div className="chart-widget">
            <h4 className="widget-title">📈 {title}</h4>
            <div className="mock-chart">
              {[65, 80, 45, 90, 70, 85, 60].map((height, i) => (
                <div key={i} className="chart-bar">
                  <div className="bar-fill" style={{ height: `${height}%` }}></div>
                </div>
              ))}
            </div>
            <div className="chart-labels">
              <span>Mon</span>
              <span>Tue</span>
              <span>Wed</span>
              <span>Thu</span>
              <span>Fri</span>
              <span>Sat</span>
              <span>Sun</span>
            </div>
          </div>
        )

      case "table":
        return (
          <div className="table-widget">
            <h4 className="widget-title">📋 {title}</h4>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Item</th>
                  <th>Value</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Product A</td>
                  <td>$1,234</td>
                  <td><span className="status-badge success">Active</span></td>
                </tr>
                <tr>
                  <td>Product B</td>
                  <td>$2,456</td>
                  <td><span className="status-badge success">Active</span></td>
                </tr>
                <tr>
                  <td>Product C</td>
                  <td>$789</td>
                  <td><span className="status-badge warning">Pending</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        )

      case "text":
        return (
          <div className="text-widget">
            <h4 className="widget-title">📝 {title}</h4>
            <div className="text-content">
              <p>This is a text widget for displaying notes, alerts, or custom messages.</p>
              <p>Real-time updates will appear here when data changes.</p>
            </div>
          </div>
        )

      default:
        return (
          <div className="default-widget">
            <h4 className="widget-title">{title}</h4>
            <pre className="widget-config">{JSON.stringify(config, null, 2)}</pre>
          </div>
        )
    }
  }

  return (
    <div className={`widget-card widget-${widget.type}`}>
      <button className="btn-delete" onClick={onDelete} title="Delete widget">✕</button>
      {renderContent()}
    </div>
  )
}

export default WidgetCard

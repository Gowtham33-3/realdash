import { createSlice, type PayloadAction } from "@reduxjs/toolkit"
import type { Dashboard, Widget } from "../types/dashboard"

interface DashboardState {
  dashboards: Dashboard[]
  activeDashboard: Dashboard | null
  loading: boolean
  error: string | null
}

const initialState: DashboardState = {
  dashboards: [],
  activeDashboard: null,
  loading: false,
  error: null,
}

const dashboardSlice = createSlice({
  name: "dashboard",
  initialState,
  reducers: {
    setLoading(state, action: PayloadAction<boolean>) {
      state.loading = action.payload
    },
    setError(state, action: PayloadAction<string | null>) {
      state.error = action.payload
    },
    setDashboards(state, action: PayloadAction<Dashboard[]>) {
      state.dashboards = action.payload
    },
    setActiveDashboard(state, action: PayloadAction<Dashboard>) {
      state.activeDashboard = action.payload
    },
    widgetAdded(state, action: PayloadAction<Widget>) {
      if (state.activeDashboard) {
        const exists = state.activeDashboard.widgets.some(w => w.id === action.payload.id)
        if (!exists) state.activeDashboard.widgets.push(action.payload)
      }
    },
    widgetUpdated(state, action: PayloadAction<Widget>) {
      if (state.activeDashboard) {
        const idx = state.activeDashboard.widgets.findIndex(w => w.id === action.payload.id)
        if (idx !== -1) state.activeDashboard.widgets[idx] = action.payload
      }
    },
    widgetRemoved(state, action: PayloadAction<string>) {
      if (state.activeDashboard) {
        state.activeDashboard.widgets = state.activeDashboard.widgets.filter(
          w => w.id !== action.payload
        )
      }
    },
  },
})

export const {
  setLoading, setError, setDashboards, setActiveDashboard,
  widgetAdded, widgetUpdated, widgetRemoved,
} = dashboardSlice.actions

export default dashboardSlice.reducer

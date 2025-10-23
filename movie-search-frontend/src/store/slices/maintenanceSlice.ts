import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface MaintenanceState {
  isMaintenanceMode: boolean;
  lastUpdated: string | null;
}

const initialState: MaintenanceState = {
  isMaintenanceMode: false, // 默认是disable状态
  lastUpdated: null,
};

const maintenanceSlice = createSlice({
  name: 'maintenance',
  initialState,
  reducers: {
    enableMaintenanceMode: (state) => {
      state.isMaintenanceMode = true;
      state.lastUpdated = new Date().toISOString();
    },
    disableMaintenanceMode: (state) => {
      state.isMaintenanceMode = false;
      state.lastUpdated = new Date().toISOString();
    },
    setMaintenanceMode: (state, action: PayloadAction<boolean>) => {
      state.isMaintenanceMode = action.payload;
      state.lastUpdated = new Date().toISOString();
    },
  },
});

export const { enableMaintenanceMode, disableMaintenanceMode, setMaintenanceMode } = maintenanceSlice.actions;
export default maintenanceSlice.reducer;

import { createSlice, createAsyncThunk, type PayloadAction } from '@reduxjs/toolkit';
import type { FeatureFlag } from '../../types/movie';
import { apiService } from '../../services/api';

interface FeatureFlagState {
  flags: Record<string, FeatureFlag>;
  loading: boolean;
  error: string | null;
  lastUpdated: string | null;
}

const initialState: FeatureFlagState = {
  flags: {},
  loading: false,
  error: null,
  lastUpdated: null,
};

// Async thunks
export const fetchFeatureFlags = createAsyncThunk(
  'featureFlags/fetchFeatureFlags',
  async (_flagNames: string[], { rejectWithValue }) => {
    try {
      console.log('🔧 [FEATURE-FLAG] Fetching dark_mode feature flag...');
      const response = await apiService.getFeatureFlag('dark_mode');
      console.log('🔧 [FEATURE-FLAG] Received response:', response);
      return { flags: { dark_mode: { enabled: response.enabled } } };
    } catch (error) {
      console.error('🔧 [FEATURE-FLAG] Error fetching feature flags:', error);
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to fetch feature flags');
    }
  }
);

export const fetchFeatureFlag = createAsyncThunk(
  'featureFlags/fetchFeatureFlag',
  async (flagName: string, { rejectWithValue }) => {
    try {
      const response = await apiService.getFeatureFlag(flagName);
      return response;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to fetch feature flag');
    }
  }
);

const featureFlagSlice = createSlice({
  name: 'featureFlags',
  initialState,
  reducers: {
    updateFeatureFlag: (state, action: PayloadAction<{ name: string; enabled: boolean }>) => {
      state.flags[action.payload.name] = { name: action.payload.name, enabled: action.payload.enabled };
      state.lastUpdated = new Date().toISOString();
    },
    clearError: (state) => {
      state.error = null;
    },
    setFeatureFlags: (state, action: PayloadAction<Record<string, FeatureFlag>>) => {
      state.flags = action.payload;
      state.lastUpdated = new Date().toISOString();
    },
  },
  extraReducers: (builder) => {
    // Fetch feature flags batch
    builder
      .addCase(fetchFeatureFlags.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchFeatureFlags.fulfilled, (state, action) => {
        state.loading = false;
        console.log('🔧 [FEATURE-FLAG] Fulfilled with payload:', action.payload);
        if (action.payload.flags) {
          Object.entries(action.payload.flags).forEach(([name, flagInfo]) => {
            console.log(`🔧 [FEATURE-FLAG] Setting flag ${name} to ${flagInfo.enabled}`);
            state.flags[name] = { name: name, enabled: flagInfo.enabled };
          });
        }
        state.lastUpdated = new Date().toISOString();
        console.log('🔧 [FEATURE-FLAG] Final state:', state.flags);
      })
      .addCase(fetchFeatureFlags.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Fetch single feature flag
    builder
      .addCase(fetchFeatureFlag.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchFeatureFlag.fulfilled, (state, action) => {
        state.loading = false;
        state.flags[action.payload.name] = { name: action.payload.name, enabled: action.payload.enabled };
        state.lastUpdated = new Date().toISOString();
      })
      .addCase(fetchFeatureFlag.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { updateFeatureFlag, clearError, setFeatureFlags } = featureFlagSlice.actions;
export default featureFlagSlice.reducer;

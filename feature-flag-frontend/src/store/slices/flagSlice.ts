import { createSlice, createAsyncThunk, type PayloadAction } from '@reduxjs/toolkit';
import type { FeatureFlag, CreateFlagRequest, UpdateFlagRequest } from '../../types/flag';
import { apiService } from '../../services/api';

interface FlagState {
  flags: FeatureFlag[];
  currentFlag: FeatureFlag | null;
  loading: boolean;
  error: string | null;
  pagination: {
    page: number;
    size: number;
    total: number;
    totalPages: number;
  };
}

const initialState: FlagState = {
  flags: [],
  currentFlag: null,
  loading: false,
  error: null,
  pagination: {
    page: 0,
    size: 20,
    total: 0,
    totalPages: 0,
  },
};

// Async thunks
export const fetchFlags = createAsyncThunk(
  'flags/fetchFlags',
  async ({ page, size }: { page: number; size: number }, { rejectWithValue }) => {
    try {
      const response = await apiService.getFlags(page, size);
      return response;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to fetch flags');
    }
  }
);

export const fetchFlagByName = createAsyncThunk(
  'flags/fetchFlagByName',
  async (name: string, { rejectWithValue }) => {
    try {
      const response = await apiService.getFlagByName(name);
      return response;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to fetch flag');
    }
  }
);

export const createFlag = createAsyncThunk(
  'flags/createFlag',
  async (flagData: CreateFlagRequest, { rejectWithValue }) => {
    try {
      const response = await apiService.createFlag(flagData);
      return response;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to create flag');
    }
  }
);

export const updateFlag = createAsyncThunk(
  'flags/updateFlag',
  async ({ name, flagData }: { name: string; flagData: UpdateFlagRequest }, { rejectWithValue }) => {
    try {
      const response = await apiService.updateFlag(name, flagData);
      return response;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to update flag');
    }
  }
);

export const deleteFlag = createAsyncThunk(
  'flags/deleteFlag',
  async (name: string, { rejectWithValue }) => {
    try {
      await apiService.deleteFlag(name);
      return name;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to delete flag');
    }
  }
);

const flagSlice = createSlice({
  name: 'flags',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearCurrentFlag: (state) => {
      state.currentFlag = null;
    },
    setPage: (state, action: PayloadAction<number>) => {
      state.pagination.page = action.payload;
    },
    setPageSize: (state, action: PayloadAction<number>) => {
      state.pagination.size = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch flags
    builder
      .addCase(fetchFlags.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchFlags.fulfilled, (state, action) => {
        state.loading = false;
        state.flags = action.payload.flags;
        state.pagination = {
          page: action.payload.page,
          size: action.payload.size,
          total: action.payload.total,
          totalPages: action.payload.totalPages,
        };
      })
      .addCase(fetchFlags.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Fetch flag by name
    builder
      .addCase(fetchFlagByName.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchFlagByName.fulfilled, (state, action) => {
        state.loading = false;
        state.currentFlag = action.payload;
      })
      .addCase(fetchFlagByName.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Create flag
    builder
      .addCase(createFlag.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createFlag.fulfilled, (state, action) => {
        state.loading = false;
        state.flags.unshift(action.payload);
        state.pagination.total += 1;
      })
      .addCase(createFlag.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Update flag
    builder
      .addCase(updateFlag.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateFlag.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.flags.findIndex(flag => flag.name === action.payload.name);
        if (index !== -1) {
          state.flags[index] = action.payload;
        }
        if (state.currentFlag?.name === action.payload.name) {
          state.currentFlag = action.payload;
        }
      })
      .addCase(updateFlag.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Delete flag
    builder
      .addCase(deleteFlag.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteFlag.fulfilled, (state, action) => {
        state.loading = false;
        state.flags = state.flags.filter(flag => flag.name !== action.payload);
        state.pagination.total -= 1;
        if (state.currentFlag?.name === action.payload) {
          state.currentFlag = null;
        }
      })
      .addCase(deleteFlag.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { clearError, clearCurrentFlag, setPage, setPageSize } = flagSlice.actions;
export default flagSlice.reducer;

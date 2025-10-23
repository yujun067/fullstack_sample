import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { ApiError, ValidationError } from '../../types/movie';

export interface ErrorState {
  currentError: ApiError | ValidationError | null;
  errorHistory: (ApiError | ValidationError)[];
  isVisible: boolean;
}

const initialState: ErrorState = {
  currentError: null,
  errorHistory: [],
  isVisible: false,
};

const errorSlice = createSlice({
  name: 'error',
  initialState,
  reducers: {
    setError: (state, action: PayloadAction<ApiError | ValidationError>) => {
      state.currentError = action.payload;
      state.errorHistory.unshift(action.payload);
      state.isVisible = true;
      
      // Keep only last 10 errors in history
      if (state.errorHistory.length > 10) {
        state.errorHistory = state.errorHistory.slice(0, 10);
      }
    },
    clearError: (state) => {
      state.currentError = null;
      state.isVisible = false;
    },
    hideError: (state) => {
      state.isVisible = false;
    },
    clearErrorHistory: (state) => {
      state.errorHistory = [];
    },
  },
});

export const { setError, clearError, hideError, clearErrorHistory } = errorSlice.actions;
export default errorSlice.reducer;

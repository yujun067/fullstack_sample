// Centralized action exports for better maintainability
export {
  // Async thunks
  fetchFlags,
  fetchFlagByName,
  createFlag,
  updateFlag,
  deleteFlag,
  // Synchronous actions
  setPage,
  setPageSize,
  clearCurrentFlag,
} from './slices/flagSlice';

export {
  // Error actions
  setError,
  clearError,
  hideError,
  clearErrorHistory,
} from './slices/errorSlice';

// Re-export types for convenience
export type { RootState, AppDispatch } from './index';

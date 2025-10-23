// Re-export all actions from slices for convenience
export {
    clearError as clearMovieError,
    clearSearchResults,
    clearCurrentMovie,
    setSearchParams,
    addToSearchHistory,
    clearSearchHistory,
    searchMovies,
    getMovieDetails
} from './slices/movieSlice';

export { 
  clearError as clearFeatureFlagError,
  setFeatureFlags,
  updateFeatureFlag,
  fetchFeatureFlags
} from './slices/featureFlagSlice';

export {
    // Error actions
    setError,
    clearError,
    hideError,
    clearErrorHistory,
} from './slices/errorSlice';
  
// Re-export types for convenience
export type { RootState, AppDispatch } from './index';

import { configureStore } from '@reduxjs/toolkit';
import movieReducer from './slices/movieSlice';
import featureFlagReducer from './slices/featureFlagSlice';
import errorReducer from './slices/errorSlice';
import maintenanceReducer from './slices/maintenanceSlice';

export const store = configureStore({
  reducer: {
    movies: movieReducer,
    featureFlags: featureFlagReducer,
    error: errorReducer,
    maintenance: maintenanceReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

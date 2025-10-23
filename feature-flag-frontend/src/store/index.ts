import { configureStore } from '@reduxjs/toolkit';
import flagReducer from './slices/flagSlice';
import errorReducer from './slices/errorSlice';

export const store = configureStore({
  reducer: {
    flags: flagReducer,
    error: errorReducer,
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

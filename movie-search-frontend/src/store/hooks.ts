import { useDispatch, useSelector, type TypedUseSelectorHook } from 'react-redux';
import type { RootState, AppDispatch } from './index';

// Pre-typed hooks for better TypeScript support
export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

// Custom hooks for specific state slices
export const useMovies = () => {
  return useAppSelector((state) => state.movies);
};

export const useFeatureFlags = () => {
  return useAppSelector((state) => state.featureFlags);
};

export const useError = () => {
  return useAppSelector((state) => state.error);
};

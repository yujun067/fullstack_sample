import React, { useEffect, useState } from 'react';
import { useAppDispatch, useFeatureFlags } from '../store/hooks';
import { fetchFeatureFlags } from '../store/actions';
import { ThemeContext, type Theme, type ThemeContextType } from './ThemeContext';

interface ThemeProviderProps {
  children: React.ReactNode;
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
  const [theme, setTheme] = useState<Theme>('light');
  const dispatch = useAppDispatch();
  const { flags } = useFeatureFlags();

  // Check for dark mode feature flag
  const isDarkModeEnabled = flags.dark_mode?.enabled;

  useEffect(() => {
    // Fetch dark_mode feature flag on mount
    console.log('🎨 [THEME] Fetching feature flags on mount');
    dispatch(fetchFeatureFlags(['dark_mode']));
    
    // Simple polling - same as other APIs
    const interval = setInterval(() => {
      console.log('🎨 [THEME] Polling for feature flag updates');
      dispatch(fetchFeatureFlags(['dark_mode']));
    }, 30000); // 30 seconds
    
    return () => {
      console.log('🎨 [THEME] Cleaning up polling interval');
      clearInterval(interval);
    };
  }, [dispatch]);

  useEffect(() => {
    // Apply theme based on feature flag
    console.log('🎨 [THEME] Feature flag dark_mode enabled:', isDarkModeEnabled);
    
    if (isDarkModeEnabled) {
      console.log('🎨 [THEME] Setting theme to dark');
      setTheme('dark');
      document.documentElement.classList.add('dark');
    } else {
      console.log('🎨 [THEME] Setting theme to light');
      setTheme('light');
      document.documentElement.classList.remove('dark');
    }
  }, [isDarkModeEnabled]);

  const value: ThemeContextType = {
    theme,
    toggleTheme: () => {}, // No-op function since theme is controlled by feature flag
    isDark: theme === 'dark',
  };

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
};

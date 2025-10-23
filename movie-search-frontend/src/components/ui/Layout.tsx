import React from 'react';
import { ThemeToggle } from './index';

interface LayoutProps {
  children: React.ReactNode;
  maxWidth?: string;
  className?: string;
  showThemeToggle?: boolean;
}

const Layout: React.FC<LayoutProps> = ({ 
  children, 
  maxWidth = '7xl', 
  className = '',
  showThemeToggle = false // Default to false since theme is controlled by feature flag
}) => {
  const maxWidthClass = {
    '4xl': 'max-w-4xl',
    '5xl': 'max-w-5xl',
    '6xl': 'max-w-6xl',
    '7xl': 'max-w-7xl',
  }[maxWidth] || 'max-w-7xl';

  return (
    <div className={`min-h-screen bg-gray-50 dark:bg-gray-900 py-8 ${className}`}>
      <div className={`${maxWidthClass} mx-auto px-4 sm:px-6 lg:px-8`}>
        {/* Theme Toggle Header */}
        {showThemeToggle && (
          <div className="flex justify-end mb-6">
            <ThemeToggle />
          </div>
        )}
        {children}
      </div>
    </div>
  );
};

export default Layout;

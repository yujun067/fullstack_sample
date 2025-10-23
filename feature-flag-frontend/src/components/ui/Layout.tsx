import React from 'react';

interface LayoutProps {
  children: React.ReactNode;
  className?: string;
  maxWidth?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '4xl' | '7xl';
}

const Layout: React.FC<LayoutProps> = ({ 
  children, 
  className = "", 
  maxWidth = '4xl' 
}) => {
  const maxWidthClass = {
    'sm': 'max-w-sm',
    'md': 'max-w-md', 
    'lg': 'max-w-lg',
    'xl': 'max-w-xl',
    '2xl': 'max-w-2xl',
    '4xl': 'max-w-4xl',
    '7xl': 'max-w-7xl'
  }[maxWidth];

  return (
    <div className={`min-h-screen bg-gray-50 py-8 ${className}`}>
      <div className={`${maxWidthClass} mx-auto px-4 sm:px-6 lg:px-8`}>
        {children}
      </div>
    </div>
  );
};

export default Layout;

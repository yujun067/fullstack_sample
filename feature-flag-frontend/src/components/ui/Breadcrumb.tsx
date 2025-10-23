import React from 'react';
import { useNavigate } from 'react-router-dom';

interface BreadcrumbItem {
  label: string;
  href?: string;
  onClick?: () => void;
}

interface BreadcrumbProps {
  items: BreadcrumbItem[];
  className?: string;
}

const Breadcrumb: React.FC<BreadcrumbProps> = ({ items, className = "" }) => {
  const navigate = useNavigate();

  const handleItemClick = (item: BreadcrumbItem) => {
    if (item.onClick) {
      item.onClick();
    } else if (item.href) {
      navigate(item.href);
    }
  };

  return (
    <nav className={`flex ${className}`} aria-label="Breadcrumb">
      <ol className="flex items-center space-x-4">
        {items.map((item, index) => (
          <li key={index}>
            <div className="flex items-center">
              {index > 0 && (
                <svg 
                  className="flex-shrink-0 h-5 w-5 text-gray-300" 
                  fill="currentColor" 
                  viewBox="0 0 20 20"
                >
                  <path d="M5.555 17.776l8-16 .894.448-8 16-.894-.448z" />
                </svg>
              )}
              
              {index === 0 ? (
                // Home icon for first item
                <div>
                  <button
                    onClick={() => handleItemClick(item)}
                    className="text-gray-400 hover:text-gray-500"
                    disabled={!item.href && !item.onClick}
                  >
                    <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z" />
                    </svg>
                    <span className="sr-only">Home</span>
                  </button>
                </div>
              ) : (
                // Regular breadcrumb item
                <div className="flex items-center">
                  <button
                    onClick={() => handleItemClick(item)}
                    className={`ml-4 text-sm font-medium ${
                      item.href || item.onClick 
                        ? 'text-gray-500 hover:text-gray-700' 
                        : 'text-gray-500'
                    }`}
                    disabled={!item.href && !item.onClick}
                  >
                    {item.label}
                  </button>
                </div>
              )}
            </div>
          </li>
        ))}
      </ol>
    </nav>
  );
};

export default Breadcrumb;

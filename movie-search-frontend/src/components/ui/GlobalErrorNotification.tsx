import React, { useEffect, useCallback } from 'react';
import { useAppDispatch, useError } from '../../store/hooks';
import { hideError, clearError } from '../../store/actions';

const GlobalErrorNotification: React.FC = React.memo(() => {
  const dispatch = useAppDispatch();
  const { currentError, isVisible } = useError();

  const handleClose = useCallback(() => {
    dispatch(clearError());
  }, [dispatch]);

  const handleHide = useCallback(() => {
    dispatch(hideError());
  }, [dispatch]);

  useEffect(() => {
    if (currentError && isVisible) {
      // Auto-hide error after 8 seconds
      const timer = setTimeout(() => {
        dispatch(hideError());
      }, 8000);

      return () => clearTimeout(timer);
    }
  }, [currentError, isVisible, dispatch]);

  if (!currentError || !isVisible) {
    return null;
  }

  const isValidationError = 'fieldErrors' in currentError;

  return (
    <div className="fixed top-4 right-4 z-50 max-w-md">
      <div className="bg-red-50 border border-red-200 rounded-lg shadow-lg p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg
              className="h-5 w-5 text-red-400"
              viewBox="0 0 20 20"
              fill="currentColor"
              aria-hidden="true"
            >
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 7.22z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          <div className="ml-3 flex-1">
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-sm font-medium text-red-800">
                  Error {currentError.code}
                </h3>
                <div className="mt-1 text-sm text-red-700">
                  <p>{currentError.message}</p>
                  {isValidationError && currentError.fieldErrors && (
                    <div className="mt-2">
                      <p className="font-medium">Validation errors:</p>
                      <ul className="list-disc list-inside mt-1 space-y-1">
                        {Object.entries(currentError.fieldErrors).map(([field, error]) => (
                          <li key={field} className="text-xs">
                            <span className="font-medium">{field}:</span> {error}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              </div>
              <div className="ml-4 flex-shrink-0">
                <button
                  type="button"
                  onClick={handleClose}
                  className="inline-flex rounded-md bg-red-50 p-1.5 text-red-500 hover:bg-red-100 focus:outline-none focus:ring-2 focus:ring-red-600 focus:ring-offset-2 focus:ring-offset-red-50"
                >
                  <span className="sr-only">Dismiss</span>
                  <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                    <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
                  </svg>
                </button>
              </div>
            </div>
            <div className="mt-3 flex space-x-3">
              <button
                type="button"
                onClick={handleHide}
                className="text-sm font-medium text-red-800 hover:text-red-900"
              >
                Hide
              </button>
              <button
                type="button"
                onClick={handleClose}
                className="text-sm font-medium text-red-800 hover:text-red-900"
              >
                Dismiss
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
});

GlobalErrorNotification.displayName = 'GlobalErrorNotification';

export default GlobalErrorNotification;

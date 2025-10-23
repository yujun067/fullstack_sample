import React, { useState, useMemo, useCallback } from 'react';
import type { FeatureFlag } from '../../types/flag';
import { ToggleSwitch, ConfirmDialog } from '../ui';

interface FlagCardProps {
  flag: FeatureFlag;
  onToggle: (name: string, enabled: boolean) => void;
  onEdit: (name: string) => void;
  onDelete: (name: string) => void;
  isUpdating?: boolean;
}

const FlagCard: React.FC<FlagCardProps> = React.memo(({
  flag,
  onToggle,
  onEdit,
  onDelete,
  isUpdating = false,
}) => {
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const handleToggle = useCallback((enabled: boolean) => {
    onToggle(flag.name, enabled);
  }, [onToggle]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleEdit = useCallback(() => {
    onEdit(flag.name);
  }, [onEdit]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleDeleteClick = useCallback(() => {
    setShowDeleteDialog(true);
  }, []);

  const handleDeleteConfirm = useCallback(() => {
    onDelete(flag.name);
    setShowDeleteDialog(false);
  }, [onDelete]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleDeleteCancel = useCallback(() => {
    setShowDeleteDialog(false);
  }, []);

  // Memoize expensive date formatting
  const formattedDates = useMemo(() => ({
    createdAt: new Date(flag.createdAt).toLocaleDateString(),
    updatedAt: new Date(flag.updatedAt).toLocaleDateString(),
  }), [flag.createdAt, flag.updatedAt]);

  return (
    <div className="card hover:shadow-lg transition-shadow duration-200">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center gap-3 mb-2">
            <h3 className="text-lg font-semibold text-gray-900">{flag.name}</h3>
            <span
              className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                flag.enabled
                  ? 'bg-green-100 text-green-800'
                  : 'bg-gray-100 text-gray-800'
              }`}
            >
              {flag.enabled ? 'Enabled' : 'Disabled'}
            </span>
          </div>
          
          {flag.description && (
            <p className="text-gray-600 text-sm mb-3">{flag.description}</p>
          )}
          
          <div className="flex items-center gap-4 text-xs text-gray-500">
            <span>Created: {formattedDates.createdAt}</span>
            <span>Updated: {formattedDates.updatedAt}</span>
          </div>
        </div>
        
        <div className="flex items-center gap-2 ml-4">
          <ToggleSwitch
            enabled={flag.enabled}
            onChange={handleToggle}
            disabled={isUpdating}
            className="flex-shrink-0"
          />
        </div>
      </div>
      
      <div className="mt-4 pt-4 border-t border-gray-200 flex justify-end gap-2">
        <button
          onClick={handleEdit}
          className="btn-secondary text-sm px-3 py-1"
          disabled={isUpdating}
        >
          Edit
        </button>
        <button
          onClick={handleDeleteClick}
          className="btn-danger text-sm px-3 py-1"
          disabled={isUpdating}
        >
          Delete
        </button>
      </div>
      
      <ConfirmDialog
        isOpen={showDeleteDialog}
        title="Delete Feature Flag"
        message={`Are you sure you want to delete the flag "${flag.name}"? This action cannot be undone.`}
        confirmText="Delete"
        cancelText="Cancel"
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        isLoading={isUpdating}
      />
    </div>
  );
});

FlagCard.displayName = 'FlagCard';

export default FlagCard;

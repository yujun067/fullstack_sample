import React, { useCallback } from 'react';
import { useForm, Controller } from 'react-hook-form';
import type { CreateFlagRequest, UpdateFlagRequest, FeatureFlag } from '../../types/flag';

interface FlagFormProps {
  flag?: FeatureFlag;
  onSubmit: (data: CreateFlagRequest | UpdateFlagRequest) => void | Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
}

interface FormData {
  name: string;
  description: string;
  enabled: boolean;
}

const FlagForm: React.FC<FlagFormProps> = React.memo(({ flag, onSubmit, onCancel, isLoading = false }) => {
  const isEditing = Boolean(flag);
  
  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
    control,
  } = useForm<FormData>({
    defaultValues: {
      name: flag?.name || '',
      description: flag?.description || '',
      enabled: flag?.enabled ?? false,
    },
    mode: 'onChange', // Ensure form updates immediately
  });

  const watchedEnabled = watch('enabled');

  // Debug: log the current form state
  // console.log('Form state - isEditing:', isEditing, 'watchedEnabled:', watchedEnabled, 'flag?.enabled:', flag?.enabled);

  const handleFormSubmit = useCallback((data: FormData) => {
    if (isEditing) {
      // For editing, only send description and enabled
      const updateData: UpdateFlagRequest = {
        description: data.description,
        enabled: data.enabled,
      };
      onSubmit(updateData);
    } else {
      // For creating, send all fields
      const createData: CreateFlagRequest = {
        name: data.name,
        description: data.description,
        enabled: data.enabled,
      };
      onSubmit(createData);
    }
  }, [isEditing, onSubmit]);

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-6">
          {isEditing ? 'Edit Feature Flag' : 'Create New Feature Flag'}
        </h2>
      </div>

      {!isEditing && (
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
            Flag Name *
          </label>
          <input
            type="text"
            id="name"
            {...register('name', {
              required: 'Flag name is required',
              minLength: {
                value: 1,
                message: 'Flag name must be at least 1 character',
              },
              maxLength: {
                value: 100,
                message: 'Flag name must not exceed 100 characters',
              },
              pattern: {
                value: /^[a-zA-Z0-9_]+$/,
                message: 'Flag name can only contain letters, numbers, and underscores',
              },
            })}
            className="input-field"
            placeholder="e.g., dark_mode, new_feature"
            disabled={isLoading}
          />
          {errors.name && (
            <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
          )}
        </div>
      )}

      {isEditing && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Flag Name
          </label>
          <div className="px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900">
            {flag?.name}
          </div>
          <p className="mt-1 text-sm text-gray-500">
            Flag name cannot be changed after creation
          </p>
        </div>
      )}

      <div>
        <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
          Description
        </label>
        <textarea
          id="description"
          {...register('description', {
            maxLength: {
              value: 500,
              message: 'Description must not exceed 500 characters',
            },
          })}
          rows={3}
          className="input-field resize-none"
          placeholder="Describe what this feature flag controls..."
          disabled={isLoading}
        />
        {errors.description && (
          <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-3">
          Status
        </label>
        <Controller
          name="enabled"
          control={control}
          render={({ field }) => (
            <div className="flex items-center space-x-4">
              <label className="flex items-center">
                <input
                  type="radio"
                  name={field.name}
                  value="false"
                  checked={field.value === false}
                  onChange={() => field.onChange(false)}
                  className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                  disabled={isLoading}
                />
                <span className="ml-2 text-sm text-gray-700">Disabled</span>
              </label>
              <label className="flex items-center">
                <input
                  type="radio"
                  name={field.name}
                  value="true"
                  checked={field.value === true}
                  onChange={() => field.onChange(true)}
                  className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                  disabled={isLoading}
                />
                <span className="ml-2 text-sm text-gray-700">Enabled</span>
              </label>
            </div>
          )}
        />
        
        <div className="mt-2 p-3 bg-blue-50 border border-blue-200 rounded-lg">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-blue-700">
                {watchedEnabled
                  ? 'This flag is currently enabled and will be active.'
                  : 'This flag is currently disabled and will not be active.'}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
        <button
          type="button"
          onClick={onCancel}
          className="btn-secondary"
          disabled={isLoading}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="btn-primary"
          disabled={isLoading}
        >
          {isLoading ? (
            <div className="flex items-center">
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
              {isEditing ? 'Updating...' : 'Creating...'}
            </div>
          ) : (
            isEditing ? 'Update Flag' : 'Create Flag'
          )}
        </button>
      </div>
    </form>
  );
});

FlagForm.displayName = 'FlagForm';

export default FlagForm;

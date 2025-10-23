import React, { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useFlags } from '../store/hooks';
import { createFlag } from '../store/actions';
import { FlagForm } from '../components/feature';
import { Layout, Breadcrumb } from '../components/ui';
import type { CreateFlagRequest, UpdateFlagRequest } from '../types/flag';

const CreateFlagPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { loading } = useFlags();

  const handleSubmit = useCallback(async (data: CreateFlagRequest) => {
    await dispatch(createFlag(data)).unwrap();
    // Navigate back to list page on success
    navigate('/flags');
  }, [dispatch, navigate]);

  const handleCancel = useCallback(() => {
    navigate('/flags');
  }, [navigate]);


  const breadcrumbItems = [
    { label: 'Home', href: '/flags' },
    { label: 'Feature Flags', href: '/flags' },
    { label: 'Create New Flag' }
  ];

  return (
    <Layout>
      {/* Header */}
      <div className="mb-8">
        <Breadcrumb items={breadcrumbItems} />
      </div>


        {/* Form */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="px-6 py-8">
            <FlagForm
              onSubmit={handleSubmit as (data: CreateFlagRequest | UpdateFlagRequest) => Promise<void>}
              onCancel={handleCancel}
              isLoading={loading}
            />
          </div>
        </div>

      {/* Help Section */}
      <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-6">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <h3 className="text-sm font-medium text-blue-800">Creating Feature Flags</h3>
            <div className="mt-2 text-sm text-blue-700">
              <ul className="list-disc list-inside space-y-1">
                <li>Choose a descriptive name that clearly identifies the feature</li>
                <li>Use snake_case naming convention (e.g., dark_mode, new_search_ui)</li>
                <li>Provide a clear description of what the flag controls</li>
                <li>You can always change the description and status later</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default CreateFlagPage;

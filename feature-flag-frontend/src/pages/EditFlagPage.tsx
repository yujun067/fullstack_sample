import React, { useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useFlags } from '../store/hooks';
import { fetchFlagByName, updateFlag, clearCurrentFlag } from '../store/actions';
import { FlagForm } from '../components/feature';
import { LoadingSpinner, Layout, Breadcrumb } from '../components/ui';
import type { UpdateFlagRequest } from '../types/flag';

interface EditFlagPageProps {
  flagName: string;
}

const EditFlagPage: React.FC<EditFlagPageProps> = ({ flagName }) => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { currentFlag, loading } = useFlags();

  useEffect(() => {
    // Clear any previous flag data
    dispatch(clearCurrentFlag());
    
    // Fetch the flag data
    dispatch(fetchFlagByName(flagName));
    
    // Cleanup on unmount
    return () => {
      dispatch(clearCurrentFlag());
    };
  }, [dispatch, flagName]);

  const handleSubmit = useCallback(async (data: UpdateFlagRequest) => {
    await dispatch(updateFlag({ name: flagName, flagData: data })).unwrap();
    // Navigate back to list page on success
    navigate('/flags');
  }, [dispatch, flagName, navigate]);

  const handleCancel = useCallback(() => {
    navigate('/flags');
  }, [navigate]);


  if (loading && !currentFlag) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }


  if (!currentFlag) {
    return (
      <Layout>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900">Flag Not Found</h1>
          <p className="mt-2 text-gray-600">The requested feature flag could not be found.</p>
          <div className="mt-6">
            <button
              onClick={() => navigate('/flags')}
              className="btn-primary"
            >
              Back to Flags
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  const breadcrumbItems = [
    { label: 'Home', href: '/flags' },
    { label: 'Feature Flags', href: '/flags' },
    { label: 'Edit Flag' }
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
              flag={currentFlag}
              onSubmit={handleSubmit}
              onCancel={handleCancel}
              isLoading={loading}
            />
          </div>
        </div>

      {/* Flag Info */}
      <div className="mt-8 bg-gray-50 border border-gray-200 rounded-lg p-6">
        <h3 className="text-sm font-medium text-gray-900 mb-4">Flag Information</h3>
        <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-sm font-medium text-gray-500">Created</dt>
            <dd className="mt-1 text-sm text-gray-900">
              {new Date(currentFlag.createdAt).toLocaleString()}
            </dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Last Updated</dt>
            <dd className="mt-1 text-sm text-gray-900">
              {new Date(currentFlag.updatedAt).toLocaleString()}
            </dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Created By</dt>
            <dd className="mt-1 text-sm text-gray-900">{currentFlag.createdBy}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Updated By</dt>
            <dd className="mt-1 text-sm text-gray-900">{currentFlag.updatedBy}</dd>
          </div>
        </dl>
      </div>
    </Layout>
  );
};

export default EditFlagPage;

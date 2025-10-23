import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useFlags } from '../store/hooks';
import {
  fetchFlags,
  updateFlag,
  deleteFlag,
  setPage,
  setPageSize,
} from '../store/actions';
import { FlagCard } from '../components/feature';
import { LoadingSpinner, Layout } from '../components/ui';

const FlagListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { flags, loading, pagination } = useFlags();
  
  const [searchTerm, setSearchTerm] = useState('');
  const [filterEnabled, setFilterEnabled] = useState<'all' | 'enabled' | 'disabled'>('all');

  useEffect(() => {
    dispatch(fetchFlags({ page: pagination.page, size: pagination.size }));
  }, [dispatch, pagination.page, pagination.size]);

  const handleToggle = useCallback(async (name: string, enabled: boolean) => {
    await dispatch(updateFlag({ name, flagData: { enabled } })).unwrap();
  }, [dispatch]);

  const handleEdit = useCallback((name: string) => {
    // Navigate to edit page
    navigate(`/flags/${encodeURIComponent(name)}/edit`);
  }, [navigate]);

  const handleDelete = useCallback(async (name: string) => {
    await dispatch(deleteFlag(name)).unwrap();
  }, [dispatch]);

  const handlePageChange = useCallback((newPage: number) => {
    dispatch(setPage(newPage));
  }, [dispatch]);

  const handlePageSizeChange = useCallback((newSize: number) => {
    dispatch(setPageSize(newSize));
    dispatch(setPage(0)); // Reset to first page
  }, [dispatch]);

  const filteredFlags = useMemo(() => {
    return flags.filter(flag => {
      const matchesSearch = flag.name.toLowerCase().includes(searchTerm.toLowerCase());
      
      const matchesFilter = filterEnabled === 'all' ||
                           (filterEnabled === 'enabled' && flag.enabled) ||
                           (filterEnabled === 'disabled' && !flag.enabled);
      
      return matchesSearch && matchesFilter;
    });
  }, [flags, searchTerm, filterEnabled]);

  // Memoize statistics calculations
  const stats = useMemo(() => {
    const enabledCount = flags.filter(flag => flag.enabled).length;
    const disabledCount = flags.length - enabledCount;
    
    return {
      enabled: enabledCount,
      disabled: disabledCount,
      total: pagination.total
    };
  }, [flags, pagination.total]);

  if (loading && flags.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <Layout maxWidth="7xl">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Feature Flags</h1>
              <p className="mt-2 text-gray-600">
                Manage your application's feature flags
              </p>
            </div>
            <button
              onClick={() => navigate('/flags/create')}
              className="btn-primary"
            >
              Create New Flag
            </button>
          </div>
        </div>


        {/* Filters */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
                Search Flags
              </label>
              <input
                type="text"
                id="search"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input-field"
                placeholder="Search by name or description..."
              />
            </div>
            <div>
              <label htmlFor="filter" className="block text-sm font-medium text-gray-700 mb-2">
                Filter by Status
              </label>
              <select
                id="filter"
                value={filterEnabled}
                onChange={(e) => setFilterEnabled(e.target.value as 'all' | 'enabled' | 'disabled')}
                className="input-field"
              >
                <option value="all">All Flags</option>
                <option value="enabled">Enabled Only</option>
                <option value="disabled">Disabled Only</option>
              </select>
            </div>
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Total Flags</p>
                <p className="text-2xl font-semibold text-gray-900">{stats.total}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Enabled</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {stats.enabled}
                </p>
              </div>
            </div>
          </div>
          
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-gray-100 rounded-lg flex items-center justify-center">
                  <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </div>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">Disabled</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {stats.disabled}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Flags List */}
        {filteredFlags.length === 0 ? (
          <div className="text-center py-12">
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h3 className="mt-2 text-sm font-medium text-gray-900">No flags found</h3>
            <p className="mt-1 text-sm text-gray-500">
              {searchTerm || filterEnabled !== 'all'
                ? 'Try adjusting your search or filter criteria.'
                : 'Get started by creating a new feature flag.'}
            </p>
            {!searchTerm && filterEnabled === 'all' && (
              <div className="mt-6">
                <button
                  onClick={() => navigate('/flags/create')}
                  className="btn-primary"
                >
                  Create New Flag
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="space-y-6">
            {filteredFlags.map((flag) => (
              <FlagCard
                key={flag.name}
                flag={flag}
                onToggle={handleToggle}
                onEdit={handleEdit}
                onDelete={handleDelete}
                isUpdating={loading}
              />
            ))}
          </div>
        )}

        {/* Pagination */}
        {pagination.totalPages > 1 && (
          <div className="mt-8 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <span className="text-sm text-gray-700">Show</span>
              <select
                value={pagination.size}
                onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                className="input-field w-20"
              >
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
                <option value={100}>100</option>
              </select>
              <span className="text-sm text-gray-700">per page</span>
            </div>
            
            <div className="flex items-center space-x-2">
              <button
                onClick={() => handlePageChange(pagination.page - 1)}
                disabled={pagination.page === 0}
                className="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              
              <span className="text-sm text-gray-700">
                Page {pagination.page + 1} of {pagination.totalPages}
              </span>
              
              <button
                onClick={() => handlePageChange(pagination.page + 1)}
                disabled={pagination.page >= pagination.totalPages - 1}
                className="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          </div>
        )}
    </Layout>
  );
};

export default FlagListPage;

import React, { useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMovies } from '../store/hooks';
import { useAppDispatch } from '../store/hooks';
import { searchMovies, setSearchParams } from '../store/actions';
import { SearchForm, MovieList } from '../components/feature';
import { Layout, LoadingSpinner } from '../components/ui';
import { withMaintenanceCheck } from '../utils/withMaintenanceCheck';
import type { MovieSearchResponse } from '../types/movie';

const SearchPageComponent: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { searchResults, loading, searchParams } = useMovies();

  const handleViewDetails = useCallback((imdbId: string) => {
    navigate(`/movie/${encodeURIComponent(imdbId)}`);
  }, [navigate]);

  const handleSearch = useCallback((results: MovieSearchResponse) => {
    // Search results are automatically handled by Redux
    console.log('Search completed:', results);
  }, []);

  const handlePageChange = useCallback(async (newPage: number) => {
    if (!searchResults?.searchTerm) return;
    
    const request = {
      search: searchResults.searchTerm,
      page: newPage,
      year: searchParams.year,
      type: searchParams.type,
    };

    // Update search params in store
    dispatch(setSearchParams({
      search: request.search,
      page: request.page,
      year: request.year,
      type: request.type,
    }));

    // Perform search with new page
    try {
      await dispatch(searchMovies(request)).unwrap();
    } catch (error) {
      console.error('Page change failed:', error);
    }
  }, [dispatch, searchResults?.searchTerm, searchParams.year, searchParams.type]);

  // Memoize search statistics
  const searchStats = useMemo(() => {
    if (!searchResults) return null;
    
    return {
      totalResults: searchResults.totalResults,
      currentPage: searchResults.currentPage,
      totalPages: searchResults.totalPages,
      responseTime: searchResults.responseTimeMs,
    };
  }, [searchResults?.totalResults, searchResults?.currentPage, searchResults?.totalPages, searchResults?.responseTimeMs]);


  return (
    <Layout>
      {/* Header */}
      <div className="mb-8">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100 mb-4">
            Movie Search
          </h1>
          <p className="text-lg text-gray-600 dark:text-gray-400">
            Discover your next favorite movie
          </p>
        </div>
      </div>

      {/* Search Form */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6 mb-8">
        <SearchForm onSearch={handleSearch} />
      </div>

      {/* Search Results */}
      {searchResults && (
        <div className="mb-8">
          {/* Search Stats */}
          <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4 mb-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-blue-900 dark:text-blue-100">
                  Search Results
                </h3>
                <p className="text-blue-700 dark:text-blue-300">
                  Found {searchStats?.totalResults} movies
                  {searchStats?.responseTime && (
                    <span className="ml-2 text-sm">
                      (in {searchStats.responseTime}ms)
                    </span>
                  )}
                </p>
              </div>
              <div className="text-sm text-blue-600 dark:text-blue-400">
                Page {searchStats?.currentPage} of {searchStats?.totalPages}
              </div>
            </div>
          </div>

          {/* Movie List */}
          <MovieList
            movies={searchResults.movies}
            onViewDetails={handleViewDetails}
            loading={loading}
          />

          {/* Pagination */}
          {searchStats && searchStats.totalPages > 1 && (
            <div className="mt-8 flex justify-center">
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => handlePageChange(searchStats.currentPage - 1)}
                  disabled={searchStats.currentPage <= 1}
                  className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300"
                >
                  Previous
                </button>
                
                <span className="px-4 py-2 text-sm text-gray-700 dark:text-gray-300">
                  Page {searchStats.currentPage} of {searchStats.totalPages}
                </span>
                
                <button
                  onClick={() => handlePageChange(searchStats.currentPage + 1)}
                  disabled={searchStats.currentPage >= searchStats.totalPages}
                  className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Loading State */}
      {loading && !searchResults && (
        <div className="text-center py-12">
          <LoadingSpinner size="lg" />
          <p className="mt-4 text-gray-600 dark:text-gray-400">Searching movies...</p>
        </div>
      )}

      {/* Empty State */}
      {!loading && !searchResults && (
        <div className="text-center py-12">
          <svg className="mx-auto h-12 w-12 text-gray-400 dark:text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <h3 className="mt-2 text-sm font-medium text-gray-900 dark:text-gray-100">No search performed</h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Use the search form above to find movies.
          </p>
        </div>
      )}
    </Layout>
  );
};

const SearchPage = withMaintenanceCheck(SearchPageComponent);
export default SearchPage;

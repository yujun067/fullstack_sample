import React, { useState, useCallback } from 'react';
import { useAppDispatch, useMovies } from '../../store/hooks';
import { searchMovies, setSearchParams } from '../../store/actions';
import type { MovieSearchRequest, MovieSearchResponse } from '../../types/movie';

interface SearchFormProps {
  onSearch?: (results: MovieSearchResponse) => void;
  className?: string;
}

const SearchForm: React.FC<SearchFormProps> = ({ onSearch, className = '' }) => {
  const dispatch = useAppDispatch();
  const { searchParams, loading } = useMovies();
  
  const [formData, setFormData] = useState({
    search: searchParams.search || '',
    year: searchParams.year || '',
    type: searchParams.type || '',
  });

  const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  }, []);

  const handleYearInput = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseInt(e.target.value, 10);
    if (value < 1900 || value > 2030) {
      e.target.setCustomValidity('Please enter a year between 1900 and 2030');
    } else {
      e.target.setCustomValidity('');
    }
    handleInputChange(e);
  }, [handleInputChange]);

  const handleSearchInput = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    
    // Check for minimum length
    if (value.length > 0 && value.length < 2) {
      e.target.setCustomValidity('Please enter at least 2 characters');
    }
    // Check for maximum length
    else if (value.length > 100) {
      e.target.setCustomValidity('Movie title must be less than 100 characters');
    }
    else {
      e.target.setCustomValidity('');
    }
    
    handleInputChange(e);
  }, [handleInputChange]);

  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.search.trim()) {
      return;
    }

    const request: MovieSearchRequest = {
      search: formData.search.trim(),
      page: 1,
      year: formData.year ? parseInt(formData.year.toString(), 10) : undefined,
      type: formData.type || undefined,
    };

    // Update search params in store
    dispatch(setSearchParams({
      search: request.search,
      page: request.page,
      year: request.year,
      type: request.type,
    }));

    // Perform search
    try {
      const result = await dispatch(searchMovies(request)).unwrap();
      onSearch?.(result);
    } catch (error) {
      console.error('Search failed:', error);
    }
  }, [formData, dispatch, onSearch]);

  return (
    <form onSubmit={handleSubmit} className={`space-y-4 ${className}`}>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Search Term */}
        <div className="lg:col-span-2">
          <label htmlFor="search" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Search Movies
          </label>
          <input
            type="text"
            id="search"
            name="search"
            value={formData.search}
            onChange={handleSearchInput}
            placeholder="Enter movie title..."
            minLength={2}
            maxLength={100}
            title="Please enter a movie title (2-100 characters, letters, numbers, and common punctuation only)"
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
            required
            disabled={loading}
          />
        </div>

        {/* Year */}
        <div>
          <label htmlFor="year" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Year
          </label>
          <input
            type="number"
            id="year"
            name="year"
            value={formData.year}
            onChange={handleYearInput}
            placeholder="e.g., 2023"
            min="1900"
            max="2030"
            title="Please enter a year between 1900 and 2030"
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
            disabled={loading}
          />
        </div>

        {/* Type */}
        <div>
          <label htmlFor="type" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Type
          </label>
          <select
            id="type"
            name="type"
            value={formData.type}
            onChange={handleInputChange}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
            disabled={loading}
          >
            <option value="">All Types</option>
            <option value="movie">Movie</option>
            <option value="series">Series</option>
            <option value="episode">Episode</option>
          </select>
        </div>
      </div>

      {/* Search Button */}
      <div className="flex justify-center">
        <button
          type="submit"
          disabled={loading || !formData.search.trim()}
          className="bg-primary-600 text-white px-8 py-3 rounded-lg hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
        >
          {loading ? (
            <div className="flex items-center">
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
              Searching...
            </div>
          ) : (
            'Search Movies'
          )}
        </button>
      </div>
    </form>
  );
};

export default SearchForm;

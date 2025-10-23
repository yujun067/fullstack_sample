import React, { useMemo } from 'react';
import { Movie } from '../../types/movie';
import MovieCard from './MovieCard';
import { deduplicateMovies } from '../../utils/deduplication';

interface MovieListProps {
  movies: Movie[];
  onViewDetails: (imdbId: string) => void;
  loading?: boolean;
  className?: string;
}

const MovieList: React.FC<MovieListProps> = ({ movies, onViewDetails, loading = false, className = '' }) => {
  // Deduplicate movies for display
  const uniqueMovies = useMemo(() => deduplicateMovies(movies), [movies]);
  
  // Memoize skeleton items to prevent recreation on each render
  const skeletonItems = useMemo(() => 
    Array.from({ length: 8 }).map((_, index) => (
      <div key={`skeleton-${index}`} className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden animate-pulse">
        <div className="w-full h-64 bg-gray-200" />
        <div className="p-4">
          <div className="h-4 bg-gray-200 rounded mb-2" />
          <div className="h-3 bg-gray-200 rounded mb-2" />
          <div className="h-3 bg-gray-200 rounded mb-4" />
          <div className="h-8 bg-gray-200 rounded" />
        </div>
      </div>
    ))
  , []);

  if (loading) {
    return (
      <div className={`grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 ${className}`}>
        {skeletonItems}
      </div>
    );
  }

  if (movies.length === 0) {
    return (
      <div className={`text-center py-12 ${className}`}>
        <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.34 0-4.29-1.009-5.824-2.709M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
        <h3 className="mt-2 text-sm font-medium text-gray-900">No movies found</h3>
        <p className="mt-1 text-sm text-gray-500">
          Try adjusting your search criteria or search for a different movie.
        </p>
      </div>
    );
  }

  return (
    <div className={className}>
      
      {/* Movie list */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        {uniqueMovies.map((movie, index) => (
          <MovieCard
            key={`${movie.imdbId}-${index}`}
            movie={movie}
            onViewDetails={onViewDetails}
          />
        ))}
      </div>
    </div>
  );
};

export default React.memo(MovieList);

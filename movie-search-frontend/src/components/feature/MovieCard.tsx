import React, { useCallback } from 'react';
import { Movie } from '../../types/movie';

interface MovieCardProps {
  movie: Movie;
  onViewDetails: (imdbId: string) => void;
  className?: string;
}

const MovieCard: React.FC<MovieCardProps> = ({ movie, onViewDetails, className = '' }) => {
  const [imageError, setImageError] = React.useState(false);
  
  const handleViewDetails = useCallback(() => {
    onViewDetails(movie.imdbId);
  }, [onViewDetails, movie.imdbId]);

  const handleImageError = useCallback(() => {
    setImageError(true);
  }, []);

  return (
    <div className={`bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden hover:shadow-lg transition-shadow duration-200 flex flex-col ${className}`}>
      {/* Movie Poster */}
      <div className="aspect-w-2 aspect-h-3 bg-gray-200 dark:bg-gray-700">
        {movie.poster && movie.poster !== 'N/A' && !imageError ? (
          <img
            src={movie.poster}
            alt={movie.title}
            className="w-full h-64 object-cover"
            onError={handleImageError}
          />
        ) : (
          <div className="w-full h-64 bg-gray-200 dark:bg-gray-700 flex items-center justify-center">
            <svg className="w-16 h-16 text-gray-400 dark:text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}
      </div>

      {/* Movie Info */}
      <div className="p-4 flex flex-col flex-1">
        <div className="flex-1">
          <div className="flex items-start justify-between mb-2">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 line-clamp-2">
              {movie.title}
            </h3>
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 ml-2 flex-shrink-0">
              {movie.year}
            </span>
          </div>

          {movie.type && (
            <div className="mb-2">
              <span className={`inline-flex items-center px-2 py-1 rounded-md text-xs font-medium capitalize ${
                movie.type.toLowerCase() === 'series' 
                  ? 'bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200' 
                  : movie.type.toLowerCase() === 'movie'
                  ? 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200'
                  : movie.type.toLowerCase() === 'episode'
                  ? 'bg-orange-100 dark:bg-orange-900 text-orange-800 dark:text-orange-200'
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200'
              }`}>
                {movie.type.toLowerCase() === 'series' && (
                  <svg className="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M2 6a2 2 0 012-2h6a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V6zM14.553 7.106A1 1 0 0014 8v4a1 1 0 00.553.894l2 1A1 1 0 0018 13V7a1 1 0 00-1.447-.894l-2 1z" />
                  </svg>
                )}
                {movie.type.toLowerCase() === 'movie' && (
                  <svg className="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
                  </svg>
                )}
                {movie.type.toLowerCase() === 'episode' && (
                  <svg className="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" clipRule="evenodd" />
                  </svg>
                )}
                {movie.type}
              </span>
            </div>
          )}

          {movie.imdbRating && movie.imdbRating !== 'N/A' && (
            <div className="flex items-center mb-2">
              <svg className="w-4 h-4 text-yellow-400 mr-1" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                {movie.imdbRating}
              </span>
              <span className="text-sm text-gray-500 dark:text-gray-400 ml-1">
                ({movie.imdbVotes} votes)
              </span>
            </div>
          )}

          {movie.genre && (
            <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
              {movie.genre}
            </p>
          )}

          {movie.plot && movie.plot !== 'N/A' && (
            <p className="text-sm text-gray-700 dark:text-gray-300 line-clamp-3 mb-4">
              {movie.plot}
            </p>
          )}
        </div>

        {/* Action Button - Always at bottom */}
        <button
          onClick={handleViewDetails}
          className="w-full bg-primary-600 text-white py-2 px-4 rounded-lg hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 transition-colors duration-200 mt-auto"
        >
          View Details
        </button>
      </div>
    </div>
  );
};

export default React.memo(MovieCard);

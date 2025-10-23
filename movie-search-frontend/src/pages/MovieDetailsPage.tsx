import React, { useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAppDispatch, useMovies } from '../store/hooks';
import { getMovieDetails, clearCurrentMovie } from '../store/actions';
import { Layout, LoadingSpinner } from '../components/ui';
import { withMaintenanceCheck } from '../utils/withMaintenanceCheck';

const MovieDetailsPageComponent: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { imdbId } = useParams<{ imdbId: string }>();
  const { currentMovie, loading } = useMovies();

  useEffect(() => {
    if (imdbId) {
      dispatch(getMovieDetails(imdbId));
    }

    // Cleanup on unmount
    return () => {
      dispatch(clearCurrentMovie());
    };
  }, [dispatch, imdbId]);

  const handleBackToSearch = useCallback(() => {
    navigate('/');
  }, [navigate]);


  if (loading) {
    return (
      <Layout>
        <div className="text-center py-12">
          <LoadingSpinner size="lg" />
          <p className="mt-4 text-gray-600">Loading movie details...</p>
        </div>
      </Layout>
    );
  }

  if (!currentMovie) {
    return (
      <Layout>
        <div className="text-center py-12">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            Movie Not Found
          </h1>
          <p className="text-gray-600 mb-6">
            The requested movie could not be found.
          </p>
          <button
            onClick={handleBackToSearch}
            className="bg-primary-600 text-white px-6 py-3 rounded-lg hover:bg-primary-700"
          >
            Back to Search
          </button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout maxWidth="6xl">
      {/* Back Button */}
      <div className="mb-6">
        <button
          onClick={handleBackToSearch}
          className="flex items-center text-primary-600 hover:text-primary-700"
        >
          <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to Search
        </button>
      </div>

      {/* Movie Details */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div className="md:flex">
          {/* Movie Poster */}
          <div className="md:w-1/3">
            {currentMovie.poster && currentMovie.poster !== 'N/A' ? (
              <img
                src={currentMovie.poster}
                alt={currentMovie.title}
                className="w-full h-96 md:h-full object-cover"
                onError={(e) => {
                  const target = e.target as HTMLImageElement;
                  target.src = 'https://via.placeholder.com/400x600?text=No+Image';
                }}
              />
            ) : (
              <div className="w-full h-96 md:h-full bg-gray-200 flex items-center justify-center">
                <svg className="w-24 h-24 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
            )}
          </div>

          {/* Movie Info */}
          <div className="md:w-2/3 p-6">
            <div className="mb-6">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">
                {currentMovie.title}
              </h1>
              <div className="flex items-center space-x-4 text-sm text-gray-600 mb-4">
                <span>{currentMovie.year}</span>
                <span>•</span>
                <span className="capitalize">{currentMovie.type}</span>
                <span>•</span>
                <span>{currentMovie.runtime}</span>
              </div>

              {/* Rating */}
              {currentMovie.imdbRating && currentMovie.imdbRating !== 'N/A' && (
                <div className="flex items-center mb-4">
                  <svg className="w-6 h-6 text-yellow-400 mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                  <span className="text-2xl font-bold text-gray-900">
                    {currentMovie.imdbRating}
                  </span>
                  <span className="text-gray-600 ml-2">
                    /10 ({currentMovie.imdbVotes} votes)
                  </span>
                </div>
              )}

              {/* Plot */}
              {currentMovie.plot && currentMovie.plot !== 'N/A' && (
                <div className="mb-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">Plot</h3>
                  <p className="text-gray-700 leading-relaxed">{currentMovie.plot}</p>
                </div>
              )}
            </div>

            {/* Movie Details Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Left Column */}
              <div className="space-y-4">
                {currentMovie.genre && currentMovie.genre !== 'N/A' && (
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Genre</h4>
                    <p className="text-gray-700">{currentMovie.genre}</p>
                  </div>
                )}

                {currentMovie.director && currentMovie.director !== 'N/A' && (
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Director</h4>
                    <p className="text-gray-700">{currentMovie.director}</p>
                  </div>
                )}

                {currentMovie.writer && currentMovie.writer !== 'N/A' && (
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Writer</h4>
                    <p className="text-gray-700">{currentMovie.writer}</p>
                  </div>
                )}

                {currentMovie.actors && currentMovie.actors !== 'N/A' && (
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Cast</h4>
                    <p className="text-gray-700">{currentMovie.actors}</p>
                  </div>
                )}
              </div>

              {/* Right Column */}
              <div className="space-y-4">
                {currentMovie.released && currentMovie.released !== 'N/A' && (
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Released</h4>
                    <p className="text-gray-700">{currentMovie.released}</p>
                  </div>
                )}

                {currentMovie.language && currentMovie.language !== 'N/A' && (
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Language</h4>
                    <p className="text-gray-700">{currentMovie.language}</p>
                  </div>
                )}

                {currentMovie.country && currentMovie.country !== 'N/A' && (
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Country</h4>
                    <p className="text-gray-700">{currentMovie.country}</p>
                  </div>
                )}

                {currentMovie.awards && currentMovie.awards !== 'N/A' && (
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Awards</h4>
                    <p className="text-gray-700">{currentMovie.awards}</p>
                  </div>
                )}
              </div>
            </div>

            {/* Additional Info */}
            {(currentMovie.boxOffice && currentMovie.boxOffice !== 'N/A') ||
             (currentMovie.production && currentMovie.production !== 'N/A') ||
             (currentMovie.website && currentMovie.website !== 'N/A') ? (
              <div className="mt-8 pt-6 border-t border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Additional Information</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {currentMovie.boxOffice && currentMovie.boxOffice !== 'N/A' && (
                    <div>
                      <h4 className="font-semibold text-gray-900 mb-1">Box Office</h4>
                      <p className="text-gray-700">{currentMovie.boxOffice}</p>
                    </div>
                  )}
                  {currentMovie.production && currentMovie.production !== 'N/A' && (
                    <div>
                      <h4 className="font-semibold text-gray-900 mb-1">Production</h4>
                      <p className="text-gray-700">{currentMovie.production}</p>
                    </div>
                  )}
                  {currentMovie.website && currentMovie.website !== 'N/A' && (
                    <div>
                      <h4 className="font-semibold text-gray-900 mb-1">Website</h4>
                      <a
                        href={currentMovie.website}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-primary-600 hover:text-primary-700"
                      >
                        Visit Website
                      </a>
                    </div>
                  )}
                </div>
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </Layout>
  );
};

const MovieDetailsPage = withMaintenanceCheck(MovieDetailsPageComponent);
export default MovieDetailsPage;

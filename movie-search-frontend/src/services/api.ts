import axios, { type AxiosInstance, type AxiosResponse } from 'axios';
import type { MovieSearchRequest, MovieSearchResponse, Movie, ApiError, ValidationError } from '../types/movie';
import { store } from '../store';
import { setError } from '../store/slices/errorSlice';
import { enableMaintenanceMode } from '../store/slices/maintenanceSlice';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/movie',
      timeout: 15000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor
    this.api.interceptors.request.use(
      (config) => {
        console.log(`Making ${config.method?.toUpperCase()} request to ${config.url}`);
        return config;
      },
      (error) => {
        console.error('Request error:', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.api.interceptors.response.use(
      (response: AxiosResponse) => {
        console.log(`Response received: ${response.status} ${response.statusText}`);
        return response;
      },
      (error) => {
        console.error('Response error:', error);
        
        // Handle different error types
        if (error.response) {
          // Server responded with error status
          const errorData = error.response.data;
          
          // Check for maintenance mode (503 status with MAINTENANCE_MODE error)
          if (error.response.status === 503 && errorData?.error === 'MAINTENANCE_MODE') {
            console.log('Maintenance mode detected, enabling maintenance mode');
            store.dispatch(enableMaintenanceMode());
            // Don't dispatch error for maintenance mode, just enable it
            throw error;
          }
          
          // Check if it's a validation error (has fieldErrors)
          if (errorData.fieldErrors) {
            const validationError: ValidationError = errorData;
            store.dispatch(setError(validationError));
          } else {
            const apiError: ApiError = errorData;
            store.dispatch(setError(apiError));
          }
          
          // Re-throw the error to be handled by the calling code
          throw error;
        } else if (error.request) {
          // Request was made but no response received
          const networkError: ApiError = {
            code: 0,
            error: 'NETWORK_ERROR',
            message: 'Network error: Unable to connect to server',
            timestamp: new Date().toISOString(),
          };
          store.dispatch(setError(networkError));
          throw error;
        } else {
          // Something else happened
          const unexpectedError: ApiError = {
            code: -1,
            error: 'UNEXPECTED_ERROR',
            message: 'An unexpected error occurred',
            timestamp: new Date().toISOString(),
          };
          store.dispatch(setError(unexpectedError));
          throw error;
        }
      }
    );
  }

  // Movie Search API methods
  async searchMovies(request: MovieSearchRequest): Promise<MovieSearchResponse> {
    const response = await this.api.get<MovieSearchResponse>('/movies/search', {
      params: {
        search: request.search,
        page: request.page,
        year: request.year,
        type: request.type,
      }
    });
    return response.data;
  }

  async getMovieDetails(imdbId: string): Promise<Movie> {
    const response = await this.api.get<Movie>(`/movies/details/${encodeURIComponent(imdbId)}`);
    return response.data;
  }

  // Feature Flag API methods
  async getFeatureFlag(flagName: string): Promise<{ name: string; enabled: boolean }> {
    const response = await this.api.get<{ name: string; enabled: boolean }>(`/movies/flags/${encodeURIComponent(flagName)}`);
    return response.data;
  }

}

export const apiService = new ApiService();
export default apiService;

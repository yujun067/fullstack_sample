import axios, { type AxiosInstance, type AxiosResponse } from 'axios';
import type { FeatureFlag, CreateFlagRequest, UpdateFlagRequest, FlagListResponse, ApiError, ValidationError } from '../types/flag';
import { store } from '../store';
import { setError } from '../store/actions';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/feature',
      timeout: 10000,
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

  // Feature Flag API methods
  async getFlags(page: number = 0, size: number = 20): Promise<FlagListResponse> {
    const response = await this.api.get<FlagListResponse>('/flags', {
      params: { page, size }
    });
    return response.data;
  }

  async getFlagByName(name: string): Promise<FeatureFlag> {
    const response = await this.api.get<FeatureFlag>(`/flags/${encodeURIComponent(name)}`);
    return response.data;
  }

  async createFlag(flag: CreateFlagRequest): Promise<FeatureFlag> {
    const response = await this.api.post<FeatureFlag>('/flags', flag);
    return response.data;
  }

  async updateFlag(name: string, flag: UpdateFlagRequest): Promise<FeatureFlag> {
    const response = await this.api.put<FeatureFlag>(`/flags/${encodeURIComponent(name)}`, flag);
    return response.data;
  }

  async deleteFlag(name: string): Promise<void> {
    await this.api.delete(`/flags/${encodeURIComponent(name)}`);
  }
}

export const apiService = new ApiService();
export default apiService;

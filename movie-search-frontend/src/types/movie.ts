export interface Movie {
  imdbId: string;
  title: string;
  year: string;
  rated: string;
  released: string;
  runtime: string;
  genre: string;
  director: string;
  writer: string;
  actors: string;
  plot: string;
  language: string;
  country: string;
  awards: string;
  poster: string;
  imdbRating: string;
  imdbVotes: string;
  type: string;
  dvd: string;
  boxOffice: string;
  production: string;
  website: string;
  cachedAt: string;
}

export interface MovieSearchRequest {
  search: string;
  page: number;
  year?: number;
  type?: string;
}

export interface MovieSearchResponse {
  movies: Movie[];
  totalResults: number;
  currentPage: number;
  totalPages: number;
  hasNextPage: boolean;
  hasPreviousPage: boolean;
  searchTerm: string;
  responseTimeMs: number;
}

export interface FeatureFlag {
  name: string;
  enabled: boolean;
}

export interface FeatureFlagBatchResponse {
  flags: Record<string, FeatureFlag>;
}

export interface ApiError {
  code: number;
  error: string;
  message: string;
  timestamp: string;
}

export interface ValidationError {
  code: number;
  error: string;
  message: string;
  fieldErrors: Record<string, string>;
  timestamp: string;
}

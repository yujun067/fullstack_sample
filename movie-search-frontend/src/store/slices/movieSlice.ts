import { createSlice, createAsyncThunk, type PayloadAction } from '@reduxjs/toolkit';
import type { Movie, MovieSearchRequest, MovieSearchResponse } from '../../types/movie';
import { apiService } from '../../services/api';

interface MovieState {
  searchResults: MovieSearchResponse | null;
  currentMovie: Movie | null;
  searchHistory: string[];
  loading: boolean;
  error: string | null;
  searchParams: {
    search: string;
    page: number;
    year?: number;
    type?: string;
  };
}

const initialState: MovieState = {
  searchResults: null,
  currentMovie: null,
  searchHistory: [],
  loading: false,
  error: null,
  searchParams: {
    search: '',
    page: 1,
  },
};

// Async thunks
export const searchMovies = createAsyncThunk(
  'movies/searchMovies',
  async (request: MovieSearchRequest, { rejectWithValue }) => {
    try {
      const response = await apiService.searchMovies(request);
      return response;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to search movies');
    }
  }
);

export const getMovieDetails = createAsyncThunk(
  'movies/getMovieDetails',
  async (imdbId: string, { rejectWithValue }) => {
    try {
      const response = await apiService.getMovieDetails(imdbId);
      return response;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to get movie details');
    }
  }
);

const movieSlice = createSlice({
  name: 'movies',
  initialState,
  reducers: {
    clearSearchResults: (state) => {
      state.searchResults = null;
    },
    clearCurrentMovie: (state) => {
      state.currentMovie = null;
    },
    clearError: (state) => {
      state.error = null;
    },
    setSearchParams: (state, action: PayloadAction<Partial<MovieState['searchParams']>>) => {
      state.searchParams = { ...state.searchParams, ...action.payload };
    },
    addToSearchHistory: (state, action: PayloadAction<string>) => {
      const searchTerm = action.payload.trim();
      if (searchTerm && !state.searchHistory.includes(searchTerm)) {
        state.searchHistory.unshift(searchTerm);
        // Keep only last 10 searches
        if (state.searchHistory.length > 10) {
          state.searchHistory = state.searchHistory.slice(0, 10);
        }
      }
    },
    clearSearchHistory: (state) => {
      state.searchHistory = [];
    },
  },
  extraReducers: (builder) => {
    // Search movies
    builder
      .addCase(searchMovies.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchMovies.fulfilled, (state, action) => {
        state.loading = false;
        state.searchResults = action.payload;
        // Add to search history
        if (action.payload.searchTerm) {
          const searchTerm = action.payload.searchTerm.trim();
          if (searchTerm && !state.searchHistory.includes(searchTerm)) {
            state.searchHistory.unshift(searchTerm);
            if (state.searchHistory.length > 10) {
              state.searchHistory = state.searchHistory.slice(0, 10);
            }
          }
        }
      })
      .addCase(searchMovies.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Get movie details
    builder
      .addCase(getMovieDetails.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getMovieDetails.fulfilled, (state, action) => {
        state.loading = false;
        state.currentMovie = action.payload;
      })
      .addCase(getMovieDetails.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const {
  clearSearchResults,
  clearCurrentMovie,
  clearError,
  setSearchParams,
  addToSearchHistory,
  clearSearchHistory,
} = movieSlice.actions;
export default movieSlice.reducer;

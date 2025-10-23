import { Movie } from '../types/movie';

/**
 * Deduplicate movies based on IMDb ID while preserving original order
 * @param movies Array of movies to deduplicate
 * @returns Array of unique movies
 */
export const deduplicateMovies = (movies: Movie[]): Movie[] => {
  const seen = new Set<string>();
  const uniqueMovies: Movie[] = [];
  
  for (const movie of movies) {
    if (!seen.has(movie.imdbId)) {
      seen.add(movie.imdbId);
      uniqueMovies.push(movie);
    }
  }
  
  return uniqueMovies;
};

/**
 * Get duplicate movie information and statistics
 * @param movies Array of movies to analyze
 * @returns Duplicate statistics and details
 */
export const getDuplicateInfo = (movies: Movie[]): {
  totalCount: number;
  uniqueCount: number;
  duplicateCount: number;
  duplicates: Array<{ imdbId: string; title: string; count: number }>;
} => {
  const movieCounts = new Map<string, { title: string; count: number }>();
  
  // Count occurrences of each IMDb ID
  for (const movie of movies) {
    const existing = movieCounts.get(movie.imdbId);
    if (existing) {
      existing.count++;
    } else {
      movieCounts.set(movie.imdbId, { title: movie.title, count: 1 });
    }
  }
  
  // Find duplicate entries
  const duplicates = Array.from(movieCounts.entries())
    .filter(([, info]) => info.count > 1)
    .map(([imdbId, info]) => ({ imdbId, title: info.title, count: info.count }));
  
  return {
    totalCount: movies.length,
    uniqueCount: movieCounts.size,
    duplicateCount: movies.length - movieCounts.size,
    duplicates
  };
};

/**
 * Check if the movie list contains duplicates
 * @param movies Array of movies to check
 * @returns True if duplicates exist, false otherwise
 */
export const hasDuplicates = (movies: Movie[]): boolean => {
  const seen = new Set<string>();
  for (const movie of movies) {
    if (seen.has(movie.imdbId)) {
      return true;
    }
    seen.add(movie.imdbId);
  }
  return false;
};

export interface FeatureFlag {
  id: number;
  name: string;
  description: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export interface CreateFlagRequest {
  name: string;
  description: string;
  enabled: boolean;
}

export interface UpdateFlagRequest {
  description?: string;
  enabled?: boolean;
}

export interface FlagListResponse {
  flags: FeatureFlag[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
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

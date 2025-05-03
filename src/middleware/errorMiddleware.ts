import { AxiosError } from 'axios';
import { AppError, errors } from '../utils/errorHandling';

export const handleApiError = (error: unknown): AppError => {
  if (error instanceof AxiosError) {
    if (!error.response) {
      return errors.network();
    }

    const { status, data } = error.response;
    
    switch (status) {
      case 401:
        return errors.auth();
      default:
        return errors.api(status, 'An unexpected error occurred', data);
    }
  }

  return errors.api(500, 'Unknown error occurred');
}; 
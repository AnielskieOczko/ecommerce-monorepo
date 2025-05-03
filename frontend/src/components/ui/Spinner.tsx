import React from 'react';
import { cn } from '../../utils/styles';

export interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  color?: 'primary' | 'white' | 'error';
  className?: string;
}

const Spinner: React.FC<SpinnerProps> = ({ size = 'md', color = 'primary', className }) => {
  const sizes = {
    sm: 'w-4 h-4 border-2',
    md: 'w-6 h-6 border-2',
    lg: 'w-8 h-8 border-3',
  };

  const colors = {
    primary: 'border-black border-t-transparent',
    white: 'border-white border-t-transparent',
    error: 'border-red-600 border-t-transparent',
  };

  return (
    <div
      className={cn('animate-spin rounded-full', sizes[size], colors[color], className)}
      role="status"
      aria-label="Loading"
    />
  );
};

export default Spinner;

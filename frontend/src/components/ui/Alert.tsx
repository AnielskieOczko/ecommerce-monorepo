import React from 'react';
import { cn } from '../../utils/styles';
import { XMarkIcon } from '../icons';

export interface AlertProps {
  type?: 'success' | 'error' | 'warning' | 'info';
  message: string;
  onClose?: () => void;
  className?: string;
}

const Alert: React.FC<AlertProps> = ({ type = 'info', message, onClose, className }) => {
  const variants = {
    success: 'bg-green-500 text-white',
    error: 'bg-red-500 text-white',
    warning: 'bg-yellow-500 text-white',
    info: 'bg-blue-500 text-white',
  };

  return (
    <div
      className={cn(
        'rounded-lg p-4 shadow-lg flex items-center justify-between animate-fade-in',
        variants[type],
        className
      )}
      role="alert"
    >
      <span>{message}</span>
      {onClose && (
        <button
          onClick={onClose}
          className="ml-4 text-white hover:text-gray-200 transition-colors"
          aria-label="Close alert"
        >
          <XMarkIcon className="w-5 h-5" />
        </button>
      )}
    </div>
  );
};

export default Alert;

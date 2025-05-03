import React from 'react';
import { cn } from '../../../utils/styles';
import { XMarkIcon } from '../../icons';

/**
 * Alert component for displaying status messages, notifications, or feedback.
 * Supports different types and can be dismissible.
 *
 * @example
 * ```tsx
 * <Alert
 *   type="success"
 *   message="Changes saved successfully!"
 *   onClose={() => setShowAlert(false)}
 * />
 * ```
 */
export interface AlertProps {
  /** Visual style variant of the alert */
  type?: 'success' | 'error' | 'warning' | 'info';
  /** The message to display */
  message: string;
  /** Optional handler for closing the alert */
  onClose?: () => void;
  /** Additional CSS classes */
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

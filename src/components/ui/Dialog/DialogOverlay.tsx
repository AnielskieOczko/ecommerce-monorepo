import React from 'react';
import { cn } from '../../../utils/styles';

export interface DialogOverlayProps {
  children: React.ReactNode;
  className?: string;
}

/**
 * Background overlay for the Dialog component.
 * Provides a semi-transparent backdrop and centers the dialog content.
 */
const DialogOverlay: React.FC<DialogOverlayProps> = ({ children, className }) => {
  return (
    <div
      className={cn(
        'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50',
        className
      )}
    >
      {children}
    </div>
  );
};

export default DialogOverlay;

import React from 'react';
import { cn } from '../../../utils/styles';

export interface DialogContentProps {
  children: React.ReactNode;
  className?: string;
}

/**
 * Content container for the Dialog component.
 * Provides styling and animation for the dialog box.
 */
const DialogContent: React.FC<DialogContentProps> = ({ children, className }) => {
  return (
    <div className={cn('bg-white rounded-lg p-6 max-w-md w-full mx-4 animate-fade-in', className)}>
      {children}
    </div>
  );
};

export default DialogContent;

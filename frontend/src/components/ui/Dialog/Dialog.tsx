import React from 'react';
import { cn } from '../../../utils/styles';
import Button from '../Button';
import DialogOverlay from './DialogOverlay';
import DialogContent from './DialogContent';

/**
 * Dialog component for displaying modal dialogs with confirm/cancel actions.
 * Includes backdrop overlay and focus management.
 *
 * @example
 * ```tsx
 * <Dialog
 *   isOpen={isOpen}
 *   title="Delete Item"
 *   message="Are you sure you want to delete this item?"
 *   onConfirm={handleDelete}
 *   onCancel={() => setIsOpen(false)}
 *   variant="danger"
 * />
 * ```
 */
export interface DialogProps {
  /** Whether the dialog is visible */
  isOpen: boolean;
  /** Dialog title */
  title: string;
  /** Main message or content */
  message: string;
  /** Text for the confirm button */
  confirmLabel?: string;
  /** Text for the cancel button */
  cancelLabel?: string;
  /** Handler for confirm action */
  onConfirm: () => void;
  /** Handler for cancel action */
  onCancel: () => void;
  /** Visual style variant affecting the confirm button */
  variant?: 'danger' | 'primary';
  /** Additional CSS classes */
  className?: string;
}

const Dialog: React.FC<DialogProps> = ({
  isOpen,
  title,
  message,
  confirmLabel = 'Potwierdź',
  cancelLabel = 'Anuluj',
  onConfirm,
  onCancel,
  variant = 'primary',
  className,
}) => {
  if (!isOpen) return null;

  return (
    <DialogOverlay>
      <DialogContent className={className}>
        <h2 className="text-xl font-semibold mb-4">{title}</h2>
        <p className="text-gray-600 mb-6">{message}</p>
        <div className="flex justify-end gap-4">
          <Button variant="outline" onClick={onCancel}>
            {cancelLabel}
          </Button>
          <Button variant={variant === 'danger' ? 'danger' : 'primary'} onClick={onConfirm}>
            {confirmLabel}
          </Button>
        </div>
      </DialogContent>
    </DialogOverlay>
  );
};

export default Dialog;

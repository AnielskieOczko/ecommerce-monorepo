/**
 * Core UI Components
 *
 * This module exports all the shared UI components used throughout the application.
 * Each component is designed to be reusable and follows our design system guidelines.
 *
 * @module UI
 */

export { default as Button } from './Button';
export { default as Input } from './Input';
export { default as Card } from './Card';
export { default as Spinner } from './Spinner';
export { default as Alert } from './Alert';
export { default as Dialog } from './Dialog';
export { DialogOverlay, DialogContent } from './Dialog/index';

// Re-export component types for better DX
export type { ButtonProps } from './Button';
export type { InputProps } from './Input';
export type { CardProps } from './Card';
export type { SpinnerProps } from './Spinner';
export type { AlertProps } from './Alert';
export type { DialogProps } from './Dialog';
export type { DialogOverlayProps, DialogContentProps } from './Dialog/index';

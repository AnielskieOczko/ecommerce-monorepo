import React from 'react';
import { cn } from '../../utils/styles';

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'hover' | 'interactive';
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

const Card: React.FC<CardProps> = ({
  children,
  variant = 'default',
  padding = 'md',
  className,
  ...props
}) => {
  const baseStyles = 'bg-white rounded-lg shadow-md overflow-hidden';

  const variants = {
    default: '',
    hover: 'transition-shadow hover:shadow-lg',
    interactive: 'transition-all hover:shadow-lg hover:-translate-y-1',
  };

  const paddings = {
    none: '',
    sm: 'p-3',
    md: 'p-4',
    lg: 'p-6',
  };

  return (
    <div className={cn(baseStyles, variants[variant], paddings[padding], className)} {...props}>
      {children}
    </div>
  );
};

export default Card;

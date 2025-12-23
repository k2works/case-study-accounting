import React from 'react';
import './Button.css';

type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'text';
type ButtonSize = 'small' | 'medium' | 'large';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
  fullWidth?: boolean;
  children: React.ReactNode;
}

interface ButtonClassOptions {
  variant: ButtonVariant;
  size: ButtonSize;
  isLoading: boolean;
  fullWidth: boolean;
  className: string;
}

const buildButtonClassName = (options: ButtonClassOptions): string => {
  const classes = ['button', `button--${options.variant}`, `button--${options.size}`];

  if (options.fullWidth) classes.push('button--full-width');
  if (options.isLoading) classes.push('button--loading');
  if (options.className) classes.push(options.className);

  return classes.join(' ');
};

const ButtonContent: React.FC<{ isLoading: boolean; children: React.ReactNode }> = ({
  isLoading,
  children,
}) => {
  if (isLoading) {
    return <span className="button__spinner" />;
  }
  return <>{children}</>;
};

/**
 * ボタンコンポーネント
 *
 * 統一されたスタイルのボタンを提供する。
 */
export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'medium',
  isLoading = false,
  fullWidth = false,
  children,
  className = '',
  disabled,
  ...props
}) => {
  const buttonClassName = buildButtonClassName({ variant, size, isLoading, fullWidth, className });

  return (
    <button className={buttonClassName} disabled={disabled || isLoading} {...props}>
      <ButtonContent isLoading={isLoading}>{children}</ButtonContent>
    </button>
  );
};

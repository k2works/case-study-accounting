import React from 'react';
import './ErrorMessage.css';

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
  onDismiss?: () => void;
}

/**
 * エラーメッセージコンポーネント
 *
 * エラー状態を視覚的に表示する。
 */
export const ErrorMessage: React.FC<ErrorMessageProps> = ({ message, onRetry, onDismiss }) => {
  return (
    <div className="error-message" role="alert">
      <span className="error-message__icon">⚠</span>
      <span className="error-message__text">{message}</span>
      <div className="error-message__actions">
        {onRetry && (
          <button className="error-message__btn error-message__btn--retry" onClick={onRetry}>
            再試行
          </button>
        )}
        {onDismiss && (
          <button className="error-message__btn error-message__btn--dismiss" onClick={onDismiss}>
            閉じる
          </button>
        )}
      </div>
    </div>
  );
};

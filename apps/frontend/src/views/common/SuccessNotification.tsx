import React, { useEffect } from 'react';
import './SuccessNotification.css';

interface SuccessNotificationProps {
  message: string;
  onDismiss?: () => void;
  autoHideDuration?: number;
}

/**
 * 成功通知コンポーネント
 *
 * 処理成功時のメッセージを表示する。
 */
export const SuccessNotification: React.FC<SuccessNotificationProps> = ({
  message,
  onDismiss,
  autoHideDuration = 3000,
}) => {
  useEffect(() => {
    if (onDismiss && autoHideDuration > 0) {
      const timer = setTimeout(onDismiss, autoHideDuration);
      return () => clearTimeout(timer);
    }
  }, [onDismiss, autoHideDuration]);

  return (
    <div className="success-notification" role="status">
      <span className="success-notification__icon">✓</span>
      <span className="success-notification__text">{message}</span>
      {onDismiss && (
        <button className="success-notification__dismiss" onClick={onDismiss} aria-label="閉じる">
          ×
        </button>
      )}
    </div>
  );
};

import React from 'react';
import './Loading.css';

interface LoadingProps {
  message?: string;
  size?: 'small' | 'medium' | 'large';
  fullScreen?: boolean;
}

/**
 * ローディングコンポーネント
 *
 * データ取得中などの待機状態を表示する。
 */
export const Loading: React.FC<LoadingProps> = ({
  message = '読み込み中...',
  size = 'medium',
  fullScreen = false,
}) => {
  const containerClass = fullScreen ? 'loading loading--fullscreen' : 'loading';

  return (
    <div className={containerClass}>
      <div className={`loading__spinner loading__spinner--${size}`} />
      {message && <p className="loading__message">{message}</p>}
    </div>
  );
};

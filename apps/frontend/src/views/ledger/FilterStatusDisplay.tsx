import React from 'react';
import { ErrorMessage, Loading } from '../common';

interface FilterStatusDisplayProps {
  isLoading: boolean;
  hasData: boolean;
  errorMessage: string | null;
  onRetry: () => void;
}

export const FilterStatusDisplay: React.FC<FilterStatusDisplayProps> = ({
  isLoading,
  hasData,
  errorMessage,
  onRetry,
}) => {
  return (
    <>
      {isLoading && !hasData && (
        <div style={{ marginTop: '12px' }}>
          <Loading message="勘定科目を読み込み中..." size="small" />
        </div>
      )}
      {errorMessage && (
        <div style={{ marginTop: '12px' }}>
          <ErrorMessage message={errorMessage} onRetry={onRetry} />
        </div>
      )}
    </>
  );
};

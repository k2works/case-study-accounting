import React from 'react';
import { LedgerFilterBase } from './LedgerFilterBase';
import type { LedgerFilterValues } from './LedgerFilterBase';

// 既存のインターフェース名を維持（後方互換性）
export type DailyBalanceFilterValues = LedgerFilterValues;

interface DailyBalanceFilterProps {
  values: DailyBalanceFilterValues;
  onChange: (values: DailyBalanceFilterValues) => void;
  onSearch: () => void;
}

export const DailyBalanceFilter: React.FC<DailyBalanceFilterProps> = ({
  values,
  onChange,
  onSearch,
}) => {
  return (
    <LedgerFilterBase
      testIdPrefix="daily-balance"
      values={values}
      onChange={onChange}
      onSearch={onSearch}
    />
  );
};

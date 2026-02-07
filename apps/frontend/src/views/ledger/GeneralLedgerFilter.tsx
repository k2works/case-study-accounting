import React from 'react';
import { LedgerFilterBase } from './LedgerFilterBase';
import type { LedgerFilterValues } from './LedgerFilterBase';

// 既存のインターフェース名を維持（後方互換性）
export type GeneralLedgerFilterValues = LedgerFilterValues;

interface GeneralLedgerFilterProps {
  values: GeneralLedgerFilterValues;
  onChange: (values: GeneralLedgerFilterValues) => void;
  onSearch: () => void;
}

export const GeneralLedgerFilter: React.FC<GeneralLedgerFilterProps> = ({
  values,
  onChange,
  onSearch,
}) => {
  return (
    <LedgerFilterBase
      testIdPrefix="general-ledger"
      values={values}
      onChange={onChange}
      onSearch={onSearch}
    />
  );
};

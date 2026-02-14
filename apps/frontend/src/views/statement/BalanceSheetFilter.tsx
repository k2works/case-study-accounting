import React from 'react';
import { Button } from '../common';

export interface BalanceSheetFilterValues {
  date: string;
  comparativeDate: string;
}

interface BalanceSheetFilterProps {
  values: BalanceSheetFilterValues;
  onChange: (values: BalanceSheetFilterValues) => void;
  onSearch: () => void;
}

export const BalanceSheetFilter: React.FC<BalanceSheetFilterProps> = ({
  values,
  onChange,
  onSearch,
}) => {
  const handleDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...values, date: event.target.value });
  };

  const handleComparativeDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...values, comparativeDate: event.target.value });
  };

  return (
    <div data-testid="balance-sheet-filter">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <div>
          <label htmlFor="balance-sheet-filter-date">基準日</label>
          <input
            id="balance-sheet-filter-date"
            type="date"
            value={values.date}
            onChange={handleDateChange}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <div>
          <label htmlFor="balance-sheet-filter-comparative-date">前期比較日</label>
          <input
            id="balance-sheet-filter-comparative-date"
            type="date"
            value={values.comparativeDate}
            onChange={handleComparativeDateChange}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <Button variant="primary" onClick={onSearch}>
          表示
        </Button>
      </div>
    </div>
  );
};

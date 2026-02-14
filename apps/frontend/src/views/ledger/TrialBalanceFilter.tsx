import React from 'react';
import { Button } from '../common';

export interface TrialBalanceFilterValues {
  date: string;
}

interface TrialBalanceFilterProps {
  values: TrialBalanceFilterValues;
  onChange: (values: TrialBalanceFilterValues) => void;
  onSearch: () => void;
}

export const TrialBalanceFilter: React.FC<TrialBalanceFilterProps> = ({
  values,
  onChange,
  onSearch,
}) => {
  const handleDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...values, date: event.target.value });
  };

  return (
    <div data-testid="trial-balance-filter">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <div>
          <label htmlFor="trial-balance-filter-date">基準日</label>
          <input
            id="trial-balance-filter-date"
            type="date"
            value={values.date}
            onChange={handleDateChange}
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

import React from 'react';
import { Button } from '../common';
import { useAccountOptions } from './useAccountOptions';
import { AccountSelect } from './AccountSelect';
import { FilterStatusDisplay } from './FilterStatusDisplay';

export interface LedgerFilterValues {
  accountId: string;
  dateFrom: string;
  dateTo: string;
}

interface LedgerFilterBaseProps {
  testIdPrefix: string;
  values: LedgerFilterValues;
  onChange: (values: LedgerFilterValues) => void;
  onSearch: () => void;
}

export const LedgerFilterBase: React.FC<LedgerFilterBaseProps> = ({
  testIdPrefix,
  values,
  onChange,
  onSearch,
}) => {
  const { accounts, isLoading, errorMessage, fetchAccounts } = useAccountOptions();

  const handleChange =
    (field: keyof LedgerFilterValues) =>
    (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      onChange({ ...values, [field]: event.target.value });
    };

  return (
    <div data-testid={`${testIdPrefix}-filter`}>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <AccountSelect
          id={`${testIdPrefix}-filter-account`}
          value={values.accountId}
          onChange={handleChange('accountId')}
          accounts={accounts}
          isLoading={isLoading}
        />
        <div>
          <label htmlFor={`${testIdPrefix}-filter-date-from`}>期間（開始）</label>
          <input
            id={`${testIdPrefix}-filter-date-from`}
            type="date"
            value={values.dateFrom}
            onChange={handleChange('dateFrom')}
            style={{ display: 'block', marginTop: '4px' }}
          />
        </div>
        <div>
          <label htmlFor={`${testIdPrefix}-filter-date-to`}>期間（終了）</label>
          <input
            id={`${testIdPrefix}-filter-date-to`}
            type="date"
            value={values.dateTo}
            onChange={handleChange('dateTo')}
            style={{ display: 'block', marginTop: '4px' }}
          />
        </div>
        <Button variant="primary" onClick={onSearch}>
          照会
        </Button>
      </div>
      <FilterStatusDisplay
        isLoading={isLoading}
        hasData={accounts.length > 0}
        errorMessage={errorMessage}
        onRetry={fetchAccounts}
      />
    </div>
  );
};

import React from 'react';
import { Button } from '../common';
import { useAccountOptions } from './useAccountOptions';
import { AccountSelect } from './AccountSelect';
import { FilterStatusDisplay } from './FilterStatusDisplay';

export interface MonthlyBalanceFilterValues {
  accountCode: string;
  fiscalPeriod: string;
}

interface MonthlyBalanceFilterProps {
  values: MonthlyBalanceFilterValues;
  onChange: (values: MonthlyBalanceFilterValues) => void;
  onSearch: () => void;
}

export const MonthlyBalanceFilter: React.FC<MonthlyBalanceFilterProps> = ({
  values,
  onChange,
  onSearch,
}) => {
  const { accounts, isLoading, errorMessage, fetchAccounts } = useAccountOptions();

  return (
    <div data-testid="monthly-balance-filter">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <AccountSelect
          id="monthly-balance-filter-account"
          value={values.accountCode}
          onChange={(e) => onChange({ ...values, accountCode: e.target.value })}
          accounts={accounts}
          isLoading={isLoading}
          valueField="accountCode"
        />
        <div>
          <label htmlFor="monthly-balance-filter-fiscal-period">年度</label>
          <input
            id="monthly-balance-filter-fiscal-period"
            type="number"
            value={values.fiscalPeriod}
            onChange={(e) => onChange({ ...values, fiscalPeriod: e.target.value })}
            placeholder="例: 2026"
            style={{ display: 'block', marginTop: '4px', width: '120px' }}
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

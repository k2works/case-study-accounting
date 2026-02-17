import React from 'react';
import { Button } from '../common';
import { useAccountOptions } from './useAccountOptions';
import { AccountSelect } from './AccountSelect';
import { FilterStatusDisplay } from './FilterStatusDisplay';

export interface SubsidiaryLedgerFilterValues {
  accountCode: string;
  subAccountCode: string;
  dateFrom: string;
  dateTo: string;
}

interface SubsidiaryLedgerFilterProps {
  values: SubsidiaryLedgerFilterValues;
  onChange: (values: SubsidiaryLedgerFilterValues) => void;
  onSearch: () => void;
}

export const SubsidiaryLedgerFilter: React.FC<SubsidiaryLedgerFilterProps> = ({
  values,
  onChange,
  onSearch,
}) => {
  const { accounts, isLoading, errorMessage, fetchAccounts } = useAccountOptions();

  const handleChange =
    (field: keyof SubsidiaryLedgerFilterValues) =>
    (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      onChange({ ...values, [field]: event.target.value });
    };

  return (
    <div data-testid="subsidiary-ledger-filter">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <AccountSelect
          id="subsidiary-ledger-filter-account"
          value={values.accountCode}
          onChange={handleChange('accountCode')}
          accounts={accounts}
          isLoading={isLoading}
          valueField="accountCode"
        />
        <div>
          <label htmlFor="subsidiary-ledger-filter-sub-account">補助科目</label>
          <input
            id="subsidiary-ledger-filter-sub-account"
            type="text"
            value={values.subAccountCode}
            onChange={handleChange('subAccountCode')}
            style={{ display: 'block', marginTop: '4px' }}
          />
        </div>
        <div>
          <label htmlFor="subsidiary-ledger-filter-date-from">期間（開始）</label>
          <input
            id="subsidiary-ledger-filter-date-from"
            type="date"
            value={values.dateFrom}
            onChange={handleChange('dateFrom')}
            style={{ display: 'block', marginTop: '4px' }}
          />
        </div>
        <div>
          <label htmlFor="subsidiary-ledger-filter-date-to">期間（終了）</label>
          <input
            id="subsidiary-ledger-filter-date-to"
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

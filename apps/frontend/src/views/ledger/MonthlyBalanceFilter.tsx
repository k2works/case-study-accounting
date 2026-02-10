import React, { useCallback, useEffect, useState } from 'react';
import { getAccounts, getAccountsErrorMessage } from '../../api/getAccounts';
import type { Account } from '../../api/getAccounts';
import { Button, ErrorMessage, Loading } from '../common';

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
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchAccounts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await getAccounts();
      setAccounts(data);
    } catch (error) {
      setErrorMessage(getAccountsErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchAccounts();
  }, [fetchAccounts]);

  const handleAccountChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    onChange({ ...values, accountCode: event.target.value });
  };

  const handleFiscalPeriodChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...values, fiscalPeriod: event.target.value });
  };

  return (
    <div data-testid="monthly-balance-filter">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <div style={{ minWidth: '220px' }}>
          <label htmlFor="monthly-balance-filter-account">勘定科目</label>
          <select
            id="monthly-balance-filter-account"
            value={values.accountCode}
            onChange={handleAccountChange}
            disabled={isLoading}
            style={{ display: 'block', width: '100%', marginTop: '4px' }}
          >
            <option value="">勘定科目を選択</option>
            {accounts.map((account) => (
              <option key={account.accountId} value={account.accountCode}>
                {account.accountCode} {account.accountName}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="monthly-balance-filter-fiscal-period">年度</label>
          <input
            id="monthly-balance-filter-fiscal-period"
            type="number"
            value={values.fiscalPeriod}
            onChange={handleFiscalPeriodChange}
            placeholder="例: 2026"
            style={{ display: 'block', marginTop: '4px', width: '120px' }}
          />
        </div>
        <Button variant="primary" onClick={onSearch}>
          照会
        </Button>
      </div>
      {isLoading && accounts.length === 0 && (
        <div style={{ marginTop: '12px' }}>
          <Loading message="勘定科目を読み込み中..." size="small" />
        </div>
      )}
      {errorMessage && (
        <div style={{ marginTop: '12px' }}>
          <ErrorMessage message={errorMessage} onRetry={fetchAccounts} />
        </div>
      )}
    </div>
  );
};

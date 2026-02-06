import React, { useCallback, useEffect, useState } from 'react';
import { getAccounts, getAccountsErrorMessage } from '../../api/getAccounts';
import type { Account } from '../../api/getAccounts';
import { Button, ErrorMessage, Loading } from '../common';

export interface DailyBalanceFilterValues {
  accountId: string;
  dateFrom: string;
  dateTo: string;
}

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

  const handleChange =
    (field: keyof DailyBalanceFilterValues) =>
    (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      onChange({ ...values, [field]: event.target.value });
    };

  return (
    <div data-testid="daily-balance-filter">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <div style={{ minWidth: '220px' }}>
          <label htmlFor="daily-balance-filter-account">勘定科目</label>
          <select
            id="daily-balance-filter-account"
            value={values.accountId}
            onChange={handleChange('accountId')}
            disabled={isLoading}
            style={{ display: 'block', width: '100%', marginTop: '4px' }}
          >
            <option value="">勘定科目を選択</option>
            {accounts.map((account) => (
              <option key={account.accountId} value={String(account.accountId)}>
                {account.accountCode} {account.accountName}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="daily-balance-filter-date-from">期間（開始）</label>
          <input
            id="daily-balance-filter-date-from"
            type="date"
            value={values.dateFrom}
            onChange={handleChange('dateFrom')}
            style={{ display: 'block', marginTop: '4px' }}
          />
        </div>
        <div>
          <label htmlFor="daily-balance-filter-date-to">期間（終了）</label>
          <input
            id="daily-balance-filter-date-to"
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

import React from 'react';
import type { Account } from '../../api/getAccounts';

interface AccountSelectProps {
  id: string;
  value: string;
  onChange: (event: React.ChangeEvent<HTMLSelectElement>) => void;
  accounts: Account[];
  isLoading: boolean;
  valueField?: 'accountId' | 'accountCode';
}

export const AccountSelect: React.FC<AccountSelectProps> = ({
  id,
  value,
  onChange,
  accounts,
  isLoading,
  valueField = 'accountId',
}) => {
  return (
    <div style={{ minWidth: '220px' }}>
      <label htmlFor={id}>勘定科目</label>
      <select
        id={id}
        value={value}
        onChange={onChange}
        disabled={isLoading}
        style={{ display: 'block', width: '100%', marginTop: '4px' }}
      >
        <option value="">勘定科目を選択</option>
        {accounts.map((account) => (
          <option
            key={account.accountId}
            value={valueField === 'accountCode' ? account.accountCode : String(account.accountId)}
          >
            {account.accountCode} {account.accountName}
          </option>
        ))}
      </select>
    </div>
  );
};

import React from 'react';
import { Button } from '../common';
import './AccountFilter.css';

export interface AccountFilterValues {
  type: string;
  keyword: string;
}

interface AccountFilterProps {
  values: AccountFilterValues;
  onChange: (values: AccountFilterValues) => void;
  onSearch: () => void;
  onReset: () => void;
}

const accountTypeOptions = [
  { value: '', label: 'すべて' },
  { value: 'ASSET', label: '資産' },
  { value: 'LIABILITY', label: '負債' },
  { value: 'EQUITY', label: '純資産' },
  { value: 'REVENUE', label: '収益' },
  { value: 'EXPENSE', label: '費用' },
];

/**
 * 勘定科目一覧用の検索・フィルタコンポーネント
 */
export const AccountFilter: React.FC<AccountFilterProps> = ({
  values,
  onChange,
  onSearch,
  onReset,
}) => {
  const handleTypeChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    onChange({ ...values, type: event.target.value });
  };

  const handleKeywordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...values, keyword: event.target.value });
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      onSearch();
    }
  };

  return (
    <div className="account-filter" data-testid="account-filter">
      <div className="account-filter__field">
        <label htmlFor="account-filter-type" className="account-filter__label">
          勘定科目種別
        </label>
        <select
          id="account-filter-type"
          className="account-filter__select"
          value={values.type}
          onChange={handleTypeChange}
        >
          {accountTypeOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      <div className="account-filter__field">
        <label htmlFor="account-filter-keyword" className="account-filter__label">
          検索キーワード
        </label>
        <input
          id="account-filter-keyword"
          type="text"
          className="account-filter__input"
          value={values.keyword}
          onChange={handleKeywordChange}
          onKeyDown={handleKeyDown}
          placeholder="科目コードまたは科目名"
        />
      </div>
      <div className="account-filter__actions">
        <Button variant="primary" onClick={onSearch}>
          検索
        </Button>
        <Button variant="secondary" onClick={onReset}>
          リセット
        </Button>
      </div>
    </div>
  );
};

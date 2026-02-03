import React from 'react';
import { Button } from '../common';
import './JournalEntryFilter.css';

export interface JournalEntryFilterValues {
  status: string;
  dateFrom: string;
  dateTo: string;
  accountId: string;
  amountFrom: string;
  amountTo: string;
  description: string;
}

interface JournalEntryFilterProps {
  values: JournalEntryFilterValues;
  onChange: (values: JournalEntryFilterValues) => void;
  onSearch: () => void;
  onReset: () => void;
}

const statusOptions = [
  { value: '', label: 'すべて' },
  { value: 'DRAFT', label: '下書き' },
  { value: 'PENDING', label: '承認待ち' },
  { value: 'APPROVED', label: '承認済み' },
  { value: 'CONFIRMED', label: '確定' },
];

/**
 * 仕訳一覧用の検索・フィルタコンポーネント
 */
export const JournalEntryFilter: React.FC<JournalEntryFilterProps> = ({
  values,
  onChange,
  onSearch,
  onReset,
}) => {
  const handleChange =
    (field: keyof JournalEntryFilterValues) =>
    (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      onChange({ ...values, [field]: event.target.value });
    };

  return (
    <div className="journal-entry-filter" data-testid="journal-entry-filter">
      <div className="journal-entry-filter__field">
        <label htmlFor="journal-entry-filter-status" className="journal-entry-filter__label">
          ステータス
        </label>
        <select
          id="journal-entry-filter-status"
          className="journal-entry-filter__select"
          value={values.status}
          onChange={handleChange('status')}
        >
          {statusOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      <div className="journal-entry-filter__field">
        <label htmlFor="journal-entry-filter-date-from" className="journal-entry-filter__label">
          日付（開始）
        </label>
        <input
          id="journal-entry-filter-date-from"
          type="date"
          className="journal-entry-filter__input"
          value={values.dateFrom}
          onChange={handleChange('dateFrom')}
        />
      </div>
      <div className="journal-entry-filter__field">
        <label htmlFor="journal-entry-filter-date-to" className="journal-entry-filter__label">
          日付（終了）
        </label>
        <input
          id="journal-entry-filter-date-to"
          type="date"
          className="journal-entry-filter__input"
          value={values.dateTo}
          onChange={handleChange('dateTo')}
        />
      </div>
      <div className="journal-entry-filter__field">
        <label htmlFor="journal-entry-filter-description" className="journal-entry-filter__label">
          摘要
        </label>
        <input
          id="journal-entry-filter-description"
          type="text"
          className="journal-entry-filter__input"
          placeholder="摘要で検索"
          value={values.description}
          onChange={handleChange('description')}
        />
      </div>
      <div className="journal-entry-filter__field">
        <label htmlFor="journal-entry-filter-account-id" className="journal-entry-filter__label">
          勘定科目ID
        </label>
        <input
          id="journal-entry-filter-account-id"
          type="number"
          className="journal-entry-filter__input"
          placeholder="勘定科目ID"
          value={values.accountId}
          onChange={handleChange('accountId')}
        />
      </div>
      <div className="journal-entry-filter__field">
        <label htmlFor="journal-entry-filter-amount-from" className="journal-entry-filter__label">
          金額（以上）
        </label>
        <input
          id="journal-entry-filter-amount-from"
          type="number"
          className="journal-entry-filter__input"
          placeholder="最小金額"
          value={values.amountFrom}
          onChange={handleChange('amountFrom')}
        />
      </div>
      <div className="journal-entry-filter__field">
        <label htmlFor="journal-entry-filter-amount-to" className="journal-entry-filter__label">
          金額（以下）
        </label>
        <input
          id="journal-entry-filter-amount-to"
          type="number"
          className="journal-entry-filter__input"
          placeholder="最大金額"
          value={values.amountTo}
          onChange={handleChange('amountTo')}
        />
      </div>
      <div className="journal-entry-filter__actions">
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

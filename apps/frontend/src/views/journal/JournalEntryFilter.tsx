import React from 'react';
import { Button } from '../common';
import './JournalEntryFilter.css';

export interface JournalEntryFilterValues {
  status: string;
  dateFrom: string;
  dateTo: string;
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
  const handleStatusChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    onChange({ ...values, status: event.target.value });
  };

  const handleDateFromChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...values, dateFrom: event.target.value });
  };

  const handleDateToChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...values, dateTo: event.target.value });
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
          onChange={handleStatusChange}
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
          onChange={handleDateFromChange}
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
          onChange={handleDateToChange}
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

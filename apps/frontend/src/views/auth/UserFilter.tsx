import React from 'react';
import { Button } from '../common';
import './UserFilter.css';

export interface UserFilterValues {
  role: string;
  keyword: string;
}

interface UserFilterProps {
  values: UserFilterValues;
  onChange: (values: UserFilterValues) => void;
  onSearch: () => void;
  onReset: () => void;
}

const roleOptions = [
  { value: '', label: 'すべてのロール' },
  { value: 'ADMIN', label: '管理者' },
  { value: 'MANAGER', label: 'マネージャー' },
  { value: 'USER', label: '一般ユーザー' },
  { value: 'VIEWER', label: '閲覧者' },
];

/**
 * ユーザー一覧用の検索・フィルタコンポーネント
 */
export const UserFilter: React.FC<UserFilterProps> = ({ values, onChange, onSearch, onReset }) => {
  const handleRoleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    onChange({ ...values, role: event.target.value });
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
    <div className="user-filter" data-testid="user-filter">
      <div className="user-filter__field">
        <label htmlFor="user-filter-role" className="user-filter__label">
          ロール
        </label>
        <select
          id="user-filter-role"
          className="user-filter__select"
          value={values.role}
          onChange={handleRoleChange}
          data-testid="user-filter-role"
        >
          {roleOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      <div className="user-filter__field">
        <label htmlFor="user-filter-keyword" className="user-filter__label">
          検索キーワード
        </label>
        <input
          id="user-filter-keyword"
          type="text"
          className="user-filter__input"
          value={values.keyword}
          onChange={handleKeywordChange}
          onKeyDown={handleKeyDown}
          placeholder="ユーザーIDまたは氏名"
          data-testid="user-filter-keyword"
        />
      </div>
      <div className="user-filter__actions">
        <Button variant="primary" onClick={onSearch} data-testid="user-filter-search">
          検索
        </Button>
        <Button variant="secondary" onClick={onReset} data-testid="user-filter-reset">
          リセット
        </Button>
      </div>
    </div>
  );
};

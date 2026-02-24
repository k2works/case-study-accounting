import React from 'react';
import { Button } from '../common';

export interface AuditLogFilterValues {
  userId: string;
  actionType: string;
  dateFrom: string;
  dateTo: string;
}

interface AuditLogFilterProps {
  values: AuditLogFilterValues;
  onChange: (values: AuditLogFilterValues) => void;
  onSearch: () => void;
}

const ACTION_TYPE_OPTIONS = [
  { value: '', label: 'すべて' },
  { value: 'LOGIN', label: 'ログイン' },
  { value: 'LOGOUT', label: 'ログアウト' },
  { value: 'CREATE', label: '作成' },
  { value: 'UPDATE', label: '更新' },
  { value: 'DELETE', label: '削除' },
  { value: 'APPROVE', label: '承認' },
  { value: 'REJECT', label: '差し戻し' },
  { value: 'CONFIRM', label: '確定' },
];

export const AuditLogFilter: React.FC<AuditLogFilterProps> = ({ values, onChange, onSearch }) => {
  const handleInputChange =
    (field: keyof AuditLogFilterValues) => (event: React.ChangeEvent<HTMLInputElement>) => {
      onChange({ ...values, [field]: event.target.value });
    };

  const handleSelectChange =
    (field: keyof AuditLogFilterValues) => (event: React.ChangeEvent<HTMLSelectElement>) => {
      onChange({ ...values, [field]: event.target.value });
    };

  return (
    <div data-testid="audit-log-filter">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <div>
          <label htmlFor="audit-filter-user-id">ユーザーID</label>
          <input
            id="audit-filter-user-id"
            type="text"
            value={values.userId}
            onChange={handleInputChange('userId')}
            placeholder="ユーザーIDで検索"
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <div>
          <label htmlFor="audit-filter-action-type">アクション</label>
          <select
            id="audit-filter-action-type"
            value={values.actionType}
            onChange={handleSelectChange('actionType')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          >
            {ACTION_TYPE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="audit-filter-date-from">開始日</label>
          <input
            id="audit-filter-date-from"
            type="date"
            value={values.dateFrom}
            onChange={handleInputChange('dateFrom')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <div>
          <label htmlFor="audit-filter-date-to">終了日</label>
          <input
            id="audit-filter-date-to"
            type="date"
            value={values.dateTo}
            onChange={handleInputChange('dateTo')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <Button variant="primary" onClick={onSearch}>
          検索
        </Button>
      </div>
    </div>
  );
};

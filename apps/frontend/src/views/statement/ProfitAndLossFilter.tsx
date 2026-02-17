import React from 'react';
import { Button } from '../common';

export interface ProfitAndLossFilterValues {
  dateFrom: string;
  dateTo: string;
  comparativeDateFrom: string;
  comparativeDateTo: string;
}

interface ProfitAndLossFilterProps {
  values: ProfitAndLossFilterValues;
  onChange: (values: ProfitAndLossFilterValues) => void;
  onSearch: () => void;
}

export const ProfitAndLossFilter: React.FC<ProfitAndLossFilterProps> = ({
  values,
  onChange,
  onSearch,
}) => {
  const handleChange = (field: keyof ProfitAndLossFilterValues) => {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      onChange({ ...values, [field]: event.target.value });
    };
  };

  return (
    <div data-testid="profit-and-loss-filter">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <div>
          <label htmlFor="pl-filter-date-from">期間開始日</label>
          <input
            id="pl-filter-date-from"
            type="date"
            value={values.dateFrom}
            onChange={handleChange('dateFrom')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <div>
          <label htmlFor="pl-filter-date-to">期間終了日</label>
          <input
            id="pl-filter-date-to"
            type="date"
            value={values.dateTo}
            onChange={handleChange('dateTo')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <div>
          <label htmlFor="pl-filter-comparative-from">前期開始日</label>
          <input
            id="pl-filter-comparative-from"
            type="date"
            value={values.comparativeDateFrom}
            onChange={handleChange('comparativeDateFrom')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <div>
          <label htmlFor="pl-filter-comparative-to">前期終了日</label>
          <input
            id="pl-filter-comparative-to"
            type="date"
            value={values.comparativeDateTo}
            onChange={handleChange('comparativeDateTo')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <Button variant="primary" onClick={onSearch}>
          表示
        </Button>
      </div>
    </div>
  );
};

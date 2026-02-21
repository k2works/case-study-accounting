import React from 'react';
import { Button } from '../common';

export interface FinancialAnalysisFilterValues {
  dateFrom: string;
  dateTo: string;
  comparativeDateFrom: string;
  comparativeDateTo: string;
}

interface FinancialAnalysisFilterProps {
  values: FinancialAnalysisFilterValues;
  onChange: (values: FinancialAnalysisFilterValues) => void;
  onSearch: () => void;
}

export const FinancialAnalysisFilter: React.FC<FinancialAnalysisFilterProps> = ({
  values,
  onChange,
  onSearch,
}) => {
  const handleChange =
    (field: keyof FinancialAnalysisFilterValues) =>
    (event: React.ChangeEvent<HTMLInputElement>) => {
      onChange({ ...values, [field]: event.target.value });
    };

  return (
    <div data-testid="financial-analysis-filter">
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'flex-end' }}>
        <div>
          <label htmlFor="fa-filter-date-from">開始日</label>
          <input
            id="fa-filter-date-from"
            type="date"
            value={values.dateFrom}
            onChange={handleChange('dateFrom')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <div>
          <label htmlFor="fa-filter-date-to">終了日</label>
          <input
            id="fa-filter-date-to"
            type="date"
            value={values.dateTo}
            onChange={handleChange('dateTo')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <div>
          <label htmlFor="fa-filter-comp-from">前期開始日</label>
          <input
            id="fa-filter-comp-from"
            type="date"
            value={values.comparativeDateFrom}
            onChange={handleChange('comparativeDateFrom')}
            style={{ display: 'block', marginTop: '4px', width: '180px' }}
          />
        </div>
        <div>
          <label htmlFor="fa-filter-comp-to">前期終了日</label>
          <input
            id="fa-filter-comp-to"
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

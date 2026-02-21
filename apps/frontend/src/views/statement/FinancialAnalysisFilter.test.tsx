import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { FinancialAnalysisFilter } from './FinancialAnalysisFilter';
import type { FinancialAnalysisFilterValues } from './FinancialAnalysisFilter';

const defaultValues: FinancialAnalysisFilterValues = {
  dateFrom: '',
  dateTo: '',
  comparativeDateFrom: '',
  comparativeDateTo: '',
};

const filledValues: FinancialAnalysisFilterValues = {
  dateFrom: '2024-01-01',
  dateTo: '2024-12-31',
  comparativeDateFrom: '2023-01-01',
  comparativeDateTo: '2023-12-31',
};

describe('FinancialAnalysisFilter', () => {
  const onChange = vi.fn();
  const onSearch = vi.fn();

  const renderFilter = (values: FinancialAnalysisFilterValues = defaultValues) =>
    render(<FinancialAnalysisFilter values={values} onChange={onChange} onSearch={onSearch} />);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders all date inputs and search button', () => {
    renderFilter();
    expect(screen.getByLabelText('開始日')).toBeInTheDocument();
    expect(screen.getByLabelText('終了日')).toBeInTheDocument();
    expect(screen.getByLabelText('前期開始日')).toBeInTheDocument();
    expect(screen.getByLabelText('前期終了日')).toBeInTheDocument();
    expect(screen.getByText('表示')).toBeInTheDocument();
  });

  it('calls onChange when dateFrom is changed', async () => {
    renderFilter();
    await userEvent.setup().type(screen.getByLabelText('開始日'), '2024-01-01');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onChange when dateTo is changed', async () => {
    renderFilter();
    await userEvent.setup().type(screen.getByLabelText('終了日'), '2024-12-31');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onChange when comparativeDateFrom is changed', async () => {
    renderFilter();
    await userEvent.setup().type(screen.getByLabelText('前期開始日'), '2023-01-01');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onChange when comparativeDateTo is changed', async () => {
    renderFilter(filledValues);
    const user = userEvent.setup();
    const input = screen.getByLabelText('前期終了日');
    await user.clear(input);
    await user.type(input, '2022-12-31');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onSearch when search button is clicked', async () => {
    renderFilter(filledValues);
    await userEvent.setup().click(screen.getByText('表示'));
    expect(onSearch).toHaveBeenCalled();
  });
});

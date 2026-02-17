import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ProfitAndLossFilter } from './ProfitAndLossFilter';
import type { ProfitAndLossFilterValues } from './ProfitAndLossFilter';

const defaultValues: ProfitAndLossFilterValues = {
  dateFrom: '',
  dateTo: '',
  comparativeDateFrom: '',
  comparativeDateTo: '',
};

const filledValues: ProfitAndLossFilterValues = {
  dateFrom: '2024-01-01',
  dateTo: '2024-12-31',
  comparativeDateFrom: '',
  comparativeDateTo: '',
};

describe('ProfitAndLossFilter', () => {
  const onChange = vi.fn();
  const onSearch = vi.fn();

  const renderFilter = (values: ProfitAndLossFilterValues = defaultValues) =>
    render(<ProfitAndLossFilter values={values} onChange={onChange} onSearch={onSearch} />);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders all date inputs and search button', () => {
    renderFilter();
    expect(screen.getByLabelText('期間開始日')).toBeInTheDocument();
    expect(screen.getByLabelText('期間終了日')).toBeInTheDocument();
    expect(screen.getByLabelText('前期開始日')).toBeInTheDocument();
    expect(screen.getByLabelText('前期終了日')).toBeInTheDocument();
    expect(screen.getByText('表示')).toBeInTheDocument();
  });

  it('calls onChange when dateFrom is changed', async () => {
    renderFilter();
    await userEvent.setup().type(screen.getByLabelText('期間開始日'), '2024-01-01');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onChange when dateTo is changed', async () => {
    renderFilter();
    await userEvent.setup().type(screen.getByLabelText('期間終了日'), '2024-12-31');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onSearch when search button is clicked', async () => {
    renderFilter(filledValues);
    await userEvent.setup().click(screen.getByText('表示'));
    expect(onSearch).toHaveBeenCalled();
  });
});

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BalanceSheetFilter } from './BalanceSheetFilter';
import type { BalanceSheetFilterValues } from './BalanceSheetFilter';

const defaultValues: BalanceSheetFilterValues = { date: '', comparativeDate: '' };
const filledValues: BalanceSheetFilterValues = { date: '2024-12-31', comparativeDate: '' };

describe('BalanceSheetFilter', () => {
  const onChange = vi.fn();
  const onSearch = vi.fn();

  const renderFilter = (values: BalanceSheetFilterValues = defaultValues) =>
    render(<BalanceSheetFilter values={values} onChange={onChange} onSearch={onSearch} />);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders date input, comparative date input, and search button', () => {
    renderFilter();
    expect(screen.getByLabelText('基準日')).toBeInTheDocument();
    expect(screen.getByLabelText('前期比較日')).toBeInTheDocument();
    expect(screen.getByText('表示')).toBeInTheDocument();
  });

  it('calls onChange when date is changed', async () => {
    renderFilter();
    await userEvent.setup().type(screen.getByLabelText('基準日'), '2024-12-31');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onChange when comparative date is changed', async () => {
    renderFilter(filledValues);
    await userEvent.setup().type(screen.getByLabelText('前期比較日'), '2023-12-31');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onSearch when search button is clicked', async () => {
    renderFilter(filledValues);
    await userEvent.setup().click(screen.getByText('表示'));
    expect(onSearch).toHaveBeenCalled();
  });
});

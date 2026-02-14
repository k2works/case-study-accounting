import { beforeEach, describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MonthlyBalanceChart } from './MonthlyBalanceChart';
import type { MonthlyBalanceEntry } from '../../api/getMonthlyBalance';
import { setTooltipPayload, setTooltipLabel, resetTooltipState } from '../../test/rechartsMock';

vi.mock('recharts', async () => await import('../../test/rechartsMock'));

const createEntry = (overrides: Partial<MonthlyBalanceEntry> = {}): MonthlyBalanceEntry => ({
  month: 1,
  openingBalance: 10000,
  debitAmount: 5000,
  creditAmount: 3000,
  closingBalance: 12000,
  ...overrides,
});

describe('MonthlyBalanceChart', () => {
  beforeEach(() => {
    resetTooltipState('1');
  });

  it('returns null when entries are empty', () => {
    const { queryByTestId } = render(<MonthlyBalanceChart entries={[]} />);
    expect(queryByTestId('monthly-balance-chart')).toBeNull();
  });

  it('renders chart components when entries exist', () => {
    const entry = createEntry();
    setTooltipPayload([{ payload: entry }]);

    render(<MonthlyBalanceChart entries={[entry]} />);

    expect(screen.getByTestId('monthly-balance-chart')).toBeInTheDocument();
    expect(screen.getByTestId('line')).toHaveAttribute('data-key', 'closingBalance');
    expect(screen.getByTestId('y-axis')).toHaveAttribute('data-value', '1,000');
    expect(screen.getByTestId('x-axis')).toHaveAttribute('data-tick', '1月');
  });

  it('renders tooltip content with entry details', () => {
    const entry = createEntry({ month: 3 });
    setTooltipPayload([{ payload: entry }]);
    setTooltipLabel('3');

    render(<MonthlyBalanceChart entries={[entry]} />);

    expect(screen.getByText('3月')).toBeInTheDocument();
    expect(screen.getByText('借方合計: 5,000')).toBeInTheDocument();
    expect(screen.getByText('貸方合計: 3,000')).toBeInTheDocument();
    expect(screen.getByText('期末残高: 12,000')).toBeInTheDocument();
  });

  it('tooltip renders nothing when payload is empty', () => {
    const entry = createEntry();
    setTooltipPayload([]);

    render(<MonthlyBalanceChart entries={[entry]} />);

    const tooltip = screen.getByTestId('tooltip');
    expect(tooltip).toBeInTheDocument();
    expect(tooltip.querySelector('div > div')).toBeNull();
  });
});

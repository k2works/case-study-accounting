import { beforeEach, describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { DailyBalanceChart } from './DailyBalanceChart';
import type { DailyBalanceEntry } from '../../api/getDailyBalance';
import { setTooltipPayload, setTooltipLabel, resetTooltipState } from '../../test/rechartsMock';

vi.mock('recharts', async () => await import('../../test/rechartsMock'));

describe('DailyBalanceChart', () => {
  beforeEach(() => {
    resetTooltipState('2024-01-01');
  });

  it('returns null when entries are empty', () => {
    const { queryByTestId } = render(<DailyBalanceChart entries={[]} />);
    expect(queryByTestId('daily-balance-chart')).toBeNull();
  });

  it('renders chart and tooltip content when entries exist', () => {
    const entry: DailyBalanceEntry = {
      date: '2024-01-01',
      debitTotal: 1000,
      creditTotal: 500,
      balance: 1500,
      transactionCount: 1,
    };
    setTooltipPayload([{ payload: entry }]);
    setTooltipLabel('2024-01-01');

    render(<DailyBalanceChart entries={[entry]} />);

    expect(screen.getByTestId('daily-balance-chart')).toBeInTheDocument();
    expect(screen.getByTestId('y-axis')).toHaveAttribute('data-value', '1,000');
    expect(screen.getByText('2024-01-01')).toBeInTheDocument();
    expect(screen.getByText('借方合計: 1,000')).toBeInTheDocument();
    expect(screen.getByText('貸方合計: 500')).toBeInTheDocument();
    expect(screen.getByText('残高: 1,500')).toBeInTheDocument();
  });
});

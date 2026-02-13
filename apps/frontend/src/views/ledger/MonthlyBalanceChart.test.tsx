import { beforeEach, describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MonthlyBalanceChart } from './MonthlyBalanceChart';
import type { MonthlyBalanceEntry } from '../../api/getMonthlyBalance';

let tooltipPayload: Array<{ payload: MonthlyBalanceEntry }> = [];
let tooltipLabel = '1';

vi.mock('recharts', async () => {
  const React = await import('react');
  return {
    ResponsiveContainer: ({ children }: { children: React.ReactNode }) => (
      <div data-testid="responsive-container">{children}</div>
    ),
    LineChart: ({ children }: { children: React.ReactNode }) => (
      <div data-testid="line-chart">{children}</div>
    ),
    CartesianGrid: () => <div data-testid="cartesian-grid" />,
    XAxis: ({
      dataKey,
      tickFormatter,
    }: {
      dataKey: string;
      tickFormatter?: (value: number) => string;
    }) => (
      <div
        data-testid="x-axis"
        data-key={dataKey}
        data-tick={tickFormatter ? tickFormatter(1) : ''}
      />
    ),
    YAxis: ({ tickFormatter }: { tickFormatter?: (value: number) => string }) => (
      <div data-testid="y-axis" data-value={tickFormatter ? tickFormatter(1000) : ''} />
    ),
    Tooltip: ({ content }: { content?: React.ReactElement }) => (
      <div data-testid="tooltip">
        {content
          ? React.cloneElement(content, {
              active: true,
              payload: tooltipPayload,
              label: tooltipLabel,
            })
          : null}
      </div>
    ),
    Line: ({ dataKey }: { dataKey: string }) => <div data-testid="line" data-key={dataKey} />,
  };
});

describe('MonthlyBalanceChart', () => {
  beforeEach(() => {
    tooltipPayload = [];
    tooltipLabel = '1';
  });

  it('returns null when entries are empty', () => {
    const { queryByTestId } = render(<MonthlyBalanceChart entries={[]} />);
    expect(queryByTestId('monthly-balance-chart')).toBeNull();
  });

  it('renders chart components when entries exist', () => {
    const entry: MonthlyBalanceEntry = {
      month: 1,
      openingBalance: 10000,
      debitAmount: 5000,
      creditAmount: 3000,
      closingBalance: 12000,
    };
    tooltipPayload = [{ payload: entry }];

    render(<MonthlyBalanceChart entries={[entry]} />);

    expect(screen.getByTestId('monthly-balance-chart')).toBeInTheDocument();
    expect(screen.getByTestId('line')).toHaveAttribute('data-key', 'closingBalance');
    expect(screen.getByTestId('y-axis')).toHaveAttribute('data-value', '1,000');
    expect(screen.getByTestId('x-axis')).toHaveAttribute('data-tick', '1月');
  });

  it('renders tooltip content with entry details', () => {
    const entry: MonthlyBalanceEntry = {
      month: 3,
      openingBalance: 10000,
      debitAmount: 5000,
      creditAmount: 3000,
      closingBalance: 12000,
    };
    tooltipPayload = [{ payload: entry }];
    tooltipLabel = '3';

    render(<MonthlyBalanceChart entries={[entry]} />);

    expect(screen.getByText('3月')).toBeInTheDocument();
    expect(screen.getByText('借方合計: 5,000')).toBeInTheDocument();
    expect(screen.getByText('貸方合計: 3,000')).toBeInTheDocument();
    expect(screen.getByText('期末残高: 12,000')).toBeInTheDocument();
  });
});

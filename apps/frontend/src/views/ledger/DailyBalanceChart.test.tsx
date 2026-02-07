import { beforeEach, describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { DailyBalanceChart } from './DailyBalanceChart';
import type { DailyBalanceEntry } from '../../api/getDailyBalance';

let tooltipPayload: Array<{ payload: DailyBalanceEntry }> = [];
let tooltipLabel = '2024-01-01';

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
    XAxis: ({ dataKey }: { dataKey: string }) => <div data-testid="x-axis" data-key={dataKey} />,
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

describe('DailyBalanceChart', () => {
  beforeEach(() => {
    tooltipPayload = [];
    tooltipLabel = '2024-01-01';
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
    tooltipPayload = [{ payload: entry }];

    render(<DailyBalanceChart entries={[entry]} />);

    expect(screen.getByTestId('daily-balance-chart')).toBeInTheDocument();
    expect(screen.getByTestId('y-axis')).toHaveAttribute('data-value', '1,000');
    expect(screen.getByText('2024-01-01')).toBeInTheDocument();
    expect(screen.getByText('借方合計: 1,000')).toBeInTheDocument();
    expect(screen.getByText('貸方合計: 500')).toBeInTheDocument();
    expect(screen.getByText('残高: 1,500')).toBeInTheDocument();
  });
});

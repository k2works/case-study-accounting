import React from 'react';
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { MonthlyBalanceEntry } from '../../api/getMonthlyBalance';
import { formatCurrency } from '../../utils/formatCurrency';

interface MonthlyBalanceChartProps {
  entries: MonthlyBalanceEntry[];
}

interface MonthlyBalanceTooltipProps {
  active?: boolean;
  payload?: Array<{ payload: MonthlyBalanceEntry }>;
  label?: string;
}

const MonthlyBalanceTooltip: React.FC<MonthlyBalanceTooltipProps> = ({
  active,
  payload,
  label,
}) => {
  if (!active || !payload?.length) return null;
  const entry = payload[0]?.payload as MonthlyBalanceEntry | undefined;
  if (!entry) return null;

  return (
    <div
      style={{
        background: '#ffffff',
        border: '1px solid #e0e0e0',
        padding: '8px 12px',
        fontSize: '12px',
      }}
    >
      <div style={{ fontWeight: 600, marginBottom: '4px' }}>{label}月</div>
      <div>借方合計: {formatCurrency(entry.debitAmount)}</div>
      <div>貸方合計: {formatCurrency(entry.creditAmount)}</div>
      <div>期末残高: {formatCurrency(entry.closingBalance)}</div>
    </div>
  );
};

export const MonthlyBalanceChart: React.FC<MonthlyBalanceChartProps> = ({ entries }) => {
  if (entries.length === 0) return null;

  return (
    <div data-testid="monthly-balance-chart" style={{ marginTop: '16px', height: '320px' }}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={entries} margin={{ top: 10, right: 20, bottom: 0, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" tickFormatter={(v) => `${v}月`} />
          <YAxis tickFormatter={formatCurrency} />
          <Tooltip content={<MonthlyBalanceTooltip />} />
          <Line type="monotone" dataKey="closingBalance" stroke="#2563eb" strokeWidth={2} dot />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

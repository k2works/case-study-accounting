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
import type { DailyBalanceEntry } from '../../api/getDailyBalance';
import { formatCurrency } from '../../utils/formatCurrency';

interface DailyBalanceChartProps {
  entries: DailyBalanceEntry[];
}

interface DailyBalanceTooltipProps {
  active?: boolean;
  payload?: Array<{ payload: DailyBalanceEntry }>;
  label?: string;
}

const DailyBalanceTooltip: React.FC<DailyBalanceTooltipProps> = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  const entry = payload[0]?.payload as DailyBalanceEntry | undefined;
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
      <div style={{ fontWeight: 600, marginBottom: '4px' }}>{label}</div>
      <div>借方合計: {formatCurrency(entry.debitTotal)}</div>
      <div>貸方合計: {formatCurrency(entry.creditTotal)}</div>
      <div>残高: {formatCurrency(entry.balance)}</div>
    </div>
  );
};

export const DailyBalanceChart: React.FC<DailyBalanceChartProps> = ({ entries }) => {
  if (entries.length === 0) return null;

  return (
    <div data-testid="daily-balance-chart" style={{ marginTop: '16px', height: '320px' }}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={entries} margin={{ top: 10, right: 20, bottom: 0, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis tickFormatter={formatCurrency} />
          <Tooltip content={<DailyBalanceTooltip />} />
          <Line type="monotone" dataKey="balance" stroke="#2563eb" strokeWidth={2} dot />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

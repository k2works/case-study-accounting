import React from 'react';
import { formatCurrency } from '../../utils/formatCurrency';

interface ProfitAndLossSummaryProps {
  dateFrom: string | null;
  dateTo: string | null;
  comparativeDateFrom: string | null;
  comparativeDateTo: string | null;
  totalRevenue: number;
  totalExpense: number;
  netIncome: number;
}

const safeCurrency = (value: number | undefined | null): string => formatCurrency(value ?? 0);

interface SummaryItemProps {
  label: string;
  value: React.ReactNode;
}

const SummaryItem: React.FC<SummaryItemProps> = ({ label, value }) => (
  <div>
    <div style={{ fontSize: '12px', color: '#666' }}>{label}</div>
    <div style={{ fontSize: '18px', fontWeight: 600 }}>{value}</div>
  </div>
);

const formatPeriod = (from: string | null, to: string | null): string => {
  if (from && to) return `${from} ~ ${to}`;
  if (from) return `${from} ~`;
  if (to) return `~ ${to}`;
  return '全期間';
};

export const ProfitAndLossSummary: React.FC<ProfitAndLossSummaryProps> = ({
  dateFrom,
  dateTo,
  comparativeDateFrom,
  comparativeDateTo,
  totalRevenue,
  totalExpense,
  netIncome,
}) => {
  const period = formatPeriod(dateFrom, dateTo);
  const hasComparative = !!(comparativeDateFrom || comparativeDateTo);
  const comparativePeriod = hasComparative
    ? formatPeriod(comparativeDateFrom, comparativeDateTo)
    : null;

  return (
    <div data-testid="profit-and-loss-summary" style={{ marginTop: '16px' }}>
      <div style={{ marginBottom: '12px', fontWeight: 600 }}>
        損益計算書（{period}）
        {comparativePeriod && (
          <span style={{ fontSize: '14px', color: '#666', marginLeft: '12px' }}>
            前期比較: {comparativePeriod}
          </span>
        )}
      </div>

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '24px', marginBottom: '12px' }}>
        <SummaryItem label="収益合計" value={safeCurrency(totalRevenue)} />
        <SummaryItem label="費用合計" value={safeCurrency(totalExpense)} />
        <SummaryItem
          label="当期純利益"
          value={
            <span style={{ color: netIncome >= 0 ? '#16a34a' : '#dc2626' }}>
              {safeCurrency(netIncome)}
            </span>
          }
        />
      </div>
    </div>
  );
};

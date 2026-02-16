import React from 'react';
import { formatCurrency } from '../../utils/formatCurrency';

interface SummaryItemProps {
  label: string;
  value: number;
}

const SummaryItem: React.FC<SummaryItemProps> = ({ label, value }) => (
  <div>
    <div>{label}</div>
    <div>{formatCurrency(value)}</div>
  </div>
);

interface BalanceSummaryBaseProps {
  testId: string;
  title: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  openingBalanceLabel?: string;
}

export const BalanceSummaryBase: React.FC<BalanceSummaryBaseProps> = ({
  testId,
  title,
  openingBalance,
  debitTotal,
  creditTotal,
  closingBalance,
  openingBalanceLabel = '期首残高',
}) => {
  return (
    <div data-testid={testId} style={{ marginTop: '16px' }}>
      <div style={{ marginBottom: '8px', fontWeight: 600 }}>{title}</div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px' }}>
        <SummaryItem label={openingBalanceLabel} value={openingBalance} />
        <SummaryItem label="借方合計" value={debitTotal} />
        <SummaryItem label="貸方合計" value={creditTotal} />
        <SummaryItem label="期末残高" value={closingBalance} />
      </div>
    </div>
  );
};

import React from 'react';
import { formatCurrency } from '../../utils/formatCurrency';

interface DailyBalanceSummaryProps {
  accountCode: string;
  accountName: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
}

export const DailyBalanceSummary: React.FC<DailyBalanceSummaryProps> = ({
  accountCode,
  accountName,
  openingBalance,
  debitTotal,
  creditTotal,
  closingBalance,
}) => {
  return (
    <div data-testid="daily-balance-summary" style={{ marginTop: '16px' }}>
      <div style={{ marginBottom: '8px', fontWeight: 600 }}>
        {accountCode} {accountName}
      </div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <div>期首残高</div>
          <div>{formatCurrency(openingBalance)}</div>
        </div>
        <div>
          <div>借方合計</div>
          <div>{formatCurrency(debitTotal)}</div>
        </div>
        <div>
          <div>貸方合計</div>
          <div>{formatCurrency(creditTotal)}</div>
        </div>
        <div>
          <div>期末残高</div>
          <div>{formatCurrency(closingBalance)}</div>
        </div>
      </div>
    </div>
  );
};

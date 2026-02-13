import React from 'react';
import { BalanceSummaryBase } from './BalanceSummaryBase';

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
  ...rest
}) => {
  return (
    <BalanceSummaryBase
      testId="daily-balance-summary"
      title={`${accountCode} ${accountName}`}
      {...rest}
    />
  );
};

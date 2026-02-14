import React from 'react';
import { BalanceSummaryBase } from './BalanceSummaryBase';

interface MonthlyBalanceSummaryProps {
  accountCode: string;
  accountName: string;
  fiscalPeriod: number | null;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
}

export const MonthlyBalanceSummary: React.FC<MonthlyBalanceSummaryProps> = ({
  accountCode,
  accountName,
  fiscalPeriod,
  ...rest
}) => {
  const title = `${accountCode} ${accountName} ${fiscalPeriod ? `(${fiscalPeriod}年度)` : ''}`;
  return <BalanceSummaryBase testId="monthly-balance-summary" title={title} {...rest} />;
};

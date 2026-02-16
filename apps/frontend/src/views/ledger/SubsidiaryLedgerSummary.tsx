import React from 'react';
import { BalanceSummaryBase } from './BalanceSummaryBase';

interface SubsidiaryLedgerSummaryProps {
  accountCode: string;
  accountName: string;
  subAccountCode: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
}

export const SubsidiaryLedgerSummary: React.FC<SubsidiaryLedgerSummaryProps> = ({
  accountCode,
  accountName,
  subAccountCode,
  ...rest
}) => {
  return (
    <BalanceSummaryBase
      testId="subsidiary-ledger-summary"
      title={`科目: ${accountCode} ${accountName} - ${subAccountCode}`}
      openingBalanceLabel="前期繰越"
      {...rest}
    />
  );
};

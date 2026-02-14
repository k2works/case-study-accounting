import React, { useMemo } from 'react';
import type { TrialBalanceEntry } from '../../api/getTrialBalance';
import { Table, TableColumn } from '../common';
import { formatCurrency } from '../../utils/formatCurrency';

interface TrialBalanceTableProps {
  entries: TrialBalanceEntry[];
}

export const TrialBalanceTable: React.FC<TrialBalanceTableProps> = ({ entries }) => {
  const columns = useMemo<TableColumn<TrialBalanceEntry>[]>(
    () => [
      {
        key: 'accountCode',
        header: '勘定科目コード',
        width: '120px',
      },
      {
        key: 'accountName',
        header: '勘定科目名',
        width: '200px',
      },
      {
        key: 'debitBalance',
        header: '借方残高',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatCurrency((value as number) ?? 0)}</>,
      },
      {
        key: 'creditBalance',
        header: '貸方残高',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatCurrency((value as number) ?? 0)}</>,
      },
    ],
    []
  );

  return (
    <div data-testid="trial-balance-table" style={{ marginTop: '16px' }}>
      <Table
        columns={columns}
        data={entries}
        keyField="accountCode"
        emptyMessage="試算表データがありません"
      />
    </div>
  );
};

import React, { useMemo } from 'react';
import type { DailyBalanceEntry } from '../../api/getDailyBalance';
import { Table, TableColumn } from '../common';
import { formatCurrency } from '../../utils/formatCurrency';

interface DailyBalanceTableProps {
  entries: DailyBalanceEntry[];
}

export const DailyBalanceTable: React.FC<DailyBalanceTableProps> = ({ entries }) => {
  const columns = useMemo<TableColumn<DailyBalanceEntry>[]>(
    () => [
      { key: 'date', header: '日付', width: '120px' },
      {
        key: 'debitTotal',
        header: '借方合計',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatCurrency(value as number)}</>,
      },
      {
        key: 'creditTotal',
        header: '貸方合計',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatCurrency(value as number)}</>,
      },
      {
        key: 'balance',
        header: '残高',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatCurrency(value as number)}</>,
      },
      {
        key: 'transactionCount',
        header: '取引件数',
        width: '120px',
        align: 'right',
      },
    ],
    []
  );

  return (
    <div data-testid="daily-balance-table" style={{ marginTop: '16px' }}>
      <Table columns={columns} data={entries} keyField="date" emptyMessage="日次残高がありません" />
    </div>
  );
};

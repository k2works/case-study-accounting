import React, { useMemo } from 'react';
import type { MonthlyBalanceEntry } from '../../api/getMonthlyBalance';
import { Table, TableColumn } from '../common';
import { formatCurrency } from '../../utils/formatCurrency';

interface MonthlyBalanceTableProps {
  entries: MonthlyBalanceEntry[];
}

export const MonthlyBalanceTable: React.FC<MonthlyBalanceTableProps> = ({ entries }) => {
  const columns = useMemo<TableColumn<MonthlyBalanceEntry>[]>(
    () => [
      {
        key: 'month',
        header: '月',
        width: '80px',
        render: (value) => <>{value}月</>,
      },
      {
        key: 'openingBalance',
        header: '期首残高',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatCurrency(value as number)}</>,
      },
      {
        key: 'debitAmount',
        header: '借方合計',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatCurrency(value as number)}</>,
      },
      {
        key: 'creditAmount',
        header: '貸方合計',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatCurrency(value as number)}</>,
      },
      {
        key: 'closingBalance',
        header: '期末残高',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatCurrency(value as number)}</>,
      },
    ],
    []
  );

  return (
    <div data-testid="monthly-balance-table" style={{ marginTop: '16px' }}>
      <Table
        columns={columns}
        data={entries}
        keyField="month"
        emptyMessage="月次残高がありません"
      />
    </div>
  );
};

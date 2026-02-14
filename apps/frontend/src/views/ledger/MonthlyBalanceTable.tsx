import React, { useMemo } from 'react';
import type { MonthlyBalanceEntry } from '../../api/getMonthlyBalance';
import { Table, TableColumn } from '../common';
import { currencyColumn } from './currencyColumn';

interface MonthlyBalanceTableProps {
  entries: MonthlyBalanceEntry[];
}

export const MonthlyBalanceTable: React.FC<MonthlyBalanceTableProps> = ({ entries }) => {
  const columns = useMemo<TableColumn<MonthlyBalanceEntry>[]>(
    () => [
      { key: 'month', header: '月', width: '80px', render: (value) => <>{value}月</> },
      currencyColumn('openingBalance', '期首残高'),
      currencyColumn('debitAmount', '借方合計'),
      currencyColumn('creditAmount', '貸方合計'),
      currencyColumn('closingBalance', '期末残高'),
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

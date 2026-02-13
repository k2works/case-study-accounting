import React, { useMemo } from 'react';
import type { DailyBalanceEntry } from '../../api/getDailyBalance';
import { Table, TableColumn } from '../common';
import { currencyColumn } from './currencyColumn';

interface DailyBalanceTableProps {
  entries: DailyBalanceEntry[];
}

export const DailyBalanceTable: React.FC<DailyBalanceTableProps> = ({ entries }) => {
  const columns = useMemo<TableColumn<DailyBalanceEntry>[]>(
    () => [
      { key: 'date', header: '日付', width: '120px' },
      currencyColumn('debitTotal', '借方合計'),
      currencyColumn('creditTotal', '貸方合計'),
      currencyColumn('balance', '残高'),
      { key: 'transactionCount', header: '取引件数', width: '120px', align: 'right' },
    ],
    []
  );

  return (
    <div data-testid="daily-balance-table" style={{ marginTop: '16px' }}>
      <Table columns={columns} data={entries} keyField="date" emptyMessage="日次残高がありません" />
    </div>
  );
};

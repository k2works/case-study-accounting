import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import type { GeneralLedgerEntry } from '../../api/getGeneralLedger';
import { Table, TableColumn } from '../common';

interface GeneralLedgerTableProps {
  entries: GeneralLedgerEntry[];
}

const formatAmount = (amount: number): string => {
  return amount.toLocaleString('ja-JP', { maximumFractionDigits: 0 });
};

export const GeneralLedgerTable: React.FC<GeneralLedgerTableProps> = ({ entries }) => {
  const columns = useMemo<TableColumn<GeneralLedgerEntry>[]>(
    () => [
      { key: 'journalDate', header: '日付', width: '120px' },
      {
        key: 'journalEntryId',
        header: '仕訳番号',
        width: '120px',
        render: (value) => (
          <Link to={`/journal/entries/${value as number}/edit`}>{value as number}</Link>
        ),
      },
      { key: 'description', header: '摘要' },
      {
        key: 'debitAmount',
        header: '借方',
        width: '130px',
        align: 'right',
        render: (value) => <>{formatAmount(value as number)}</>,
      },
      {
        key: 'creditAmount',
        header: '貸方',
        width: '130px',
        align: 'right',
        render: (value) => <>{formatAmount(value as number)}</>,
      },
      {
        key: 'runningBalance',
        header: '残高',
        width: '140px',
        align: 'right',
        render: (value) => <>{formatAmount(value as number)}</>,
      },
    ],
    []
  );

  return (
    <div data-testid="general-ledger-table" style={{ marginTop: '16px' }}>
      <Table
        columns={columns}
        data={entries}
        keyField="journalEntryId"
        emptyMessage="元帳データがありません"
      />
    </div>
  );
};

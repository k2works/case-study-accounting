import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import type { SubsidiaryLedgerEntry } from '../../api/getSubsidiaryLedger';
import { Table, TableColumn } from '../common';

interface SubsidiaryLedgerTableProps {
  entries: SubsidiaryLedgerEntry[];
}

const formatAmount = (amount: number): string => {
  return amount.toLocaleString('ja-JP', { maximumFractionDigits: 0 });
};

export const SubsidiaryLedgerTable: React.FC<SubsidiaryLedgerTableProps> = ({ entries }) => {
  const columns = useMemo<TableColumn<SubsidiaryLedgerEntry>[]>(
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
    <div data-testid="subsidiary-ledger-table" style={{ marginTop: '16px' }}>
      <Table
        columns={columns}
        data={entries}
        keyField="journalEntryId"
        emptyMessage="元帳データがありません"
      />
    </div>
  );
};

import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import type { SubsidiaryLedgerEntry } from '../../api/getSubsidiaryLedger';
import { Table, TableColumn } from '../common';
import { currencyColumn } from './currencyColumn';

interface SubsidiaryLedgerTableProps {
  entries: SubsidiaryLedgerEntry[];
}

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
      currencyColumn('debitAmount', '借方'),
      currencyColumn('creditAmount', '貸方'),
      currencyColumn('runningBalance', '残高'),
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

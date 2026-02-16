import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SubsidiaryLedgerTable } from './SubsidiaryLedgerTable';
import type { SubsidiaryLedgerEntry } from '../../api/getSubsidiaryLedger';

vi.mock('react-router-dom', () => ({
  Link: ({ to, children }: { to: string; children: React.ReactNode }) => (
    <a href={to}>{children}</a>
  ),
}));

describe('SubsidiaryLedgerTable', () => {
  it('renders table headers and formatted values', () => {
    const entries: SubsidiaryLedgerEntry[] = [
      {
        journalEntryId: 1,
        journalDate: '2024-01-01',
        description: 'テスト仕訳',
        debitAmount: 1000,
        creditAmount: 2000,
        runningBalance: 3000,
      },
    ];

    render(<SubsidiaryLedgerTable entries={entries} />);

    expect(screen.getByText('日付')).toBeInTheDocument();
    expect(screen.getByText('仕訳番号')).toBeInTheDocument();
    expect(screen.getByText('摘要')).toBeInTheDocument();
    expect(screen.getByText('借方')).toBeInTheDocument();
    expect(screen.getByText('貸方')).toBeInTheDocument();
    expect(screen.getByText('残高')).toBeInTheDocument();

    expect(screen.getByText('2024-01-01')).toBeInTheDocument();
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('テスト仕訳')).toBeInTheDocument();
    expect(screen.getByText('1,000')).toBeInTheDocument();
    expect(screen.getByText('2,000')).toBeInTheDocument();
    expect(screen.getByText('3,000')).toBeInTheDocument();
  });

  it('renders empty message when no entries', () => {
    render(<SubsidiaryLedgerTable entries={[]} />);
    expect(screen.getByText('元帳データがありません')).toBeInTheDocument();
  });
});

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TrialBalanceTable } from './TrialBalanceTable';
import type { TrialBalanceEntry } from '../../api/getTrialBalance';

describe('TrialBalanceTable', () => {
  it('renders table headers and formatted values', () => {
    const entries: TrialBalanceEntry[] = [
      {
        accountCode: '1000',
        accountName: '現金',
        bsplCategory: 'BS',
        accountType: 'ASSET',
        debitBalance: 50000,
        creditBalance: 0,
      },
    ];

    render(<TrialBalanceTable entries={entries} />);

    expect(screen.getByText('勘定科目コード')).toBeInTheDocument();
    expect(screen.getByText('勘定科目名')).toBeInTheDocument();
    expect(screen.getByText('借方残高')).toBeInTheDocument();
    expect(screen.getByText('貸方残高')).toBeInTheDocument();

    expect(screen.getByText('1000')).toBeInTheDocument();
    expect(screen.getByText('現金')).toBeInTheDocument();
    expect(screen.getByText('50,000')).toBeInTheDocument();
    expect(screen.getByText('0')).toBeInTheDocument();
  });

  it('renders empty message when no entries', () => {
    render(<TrialBalanceTable entries={[]} />);
    expect(screen.getByText('試算表データがありません')).toBeInTheDocument();
  });
});

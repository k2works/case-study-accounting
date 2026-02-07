import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { DailyBalanceTable } from './DailyBalanceTable';
import type { DailyBalanceEntry } from '../../api/getDailyBalance';

describe('DailyBalanceTable', () => {
  it('renders table headers and formatted values', () => {
    const entries: DailyBalanceEntry[] = [
      {
        date: '2024-01-01',
        debitTotal: 1000,
        creditTotal: 2000,
        balance: 3000,
        transactionCount: 2,
      },
    ];

    render(<DailyBalanceTable entries={entries} />);

    expect(screen.getByText('日付')).toBeInTheDocument();
    expect(screen.getByText('借方合計')).toBeInTheDocument();
    expect(screen.getByText('貸方合計')).toBeInTheDocument();
    expect(screen.getByText('残高')).toBeInTheDocument();
    expect(screen.getByText('取引件数')).toBeInTheDocument();

    expect(screen.getByText('2024-01-01')).toBeInTheDocument();
    expect(screen.getByText('1,000')).toBeInTheDocument();
    expect(screen.getByText('2,000')).toBeInTheDocument();
    expect(screen.getByText('3,000')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('renders empty message when no entries', () => {
    render(<DailyBalanceTable entries={[]} />);
    expect(screen.getByText('日次残高がありません')).toBeInTheDocument();
  });
});

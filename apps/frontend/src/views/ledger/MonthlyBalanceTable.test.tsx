import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MonthlyBalanceTable } from './MonthlyBalanceTable';
import type { MonthlyBalanceEntry } from '../../api/getMonthlyBalance';

describe('MonthlyBalanceTable', () => {
  it('renders table headers and formatted values', () => {
    const entries: MonthlyBalanceEntry[] = [
      {
        month: 1,
        openingBalance: 10000,
        debitAmount: 5000,
        creditAmount: 3000,
        closingBalance: 12000,
      },
    ];

    render(<MonthlyBalanceTable entries={entries} />);

    expect(screen.getByText('月')).toBeInTheDocument();
    expect(screen.getByText('期首残高')).toBeInTheDocument();
    expect(screen.getByText('借方合計')).toBeInTheDocument();
    expect(screen.getByText('貸方合計')).toBeInTheDocument();
    expect(screen.getByText('期末残高')).toBeInTheDocument();

    expect(screen.getByText('1月')).toBeInTheDocument();
    expect(screen.getByText('10,000')).toBeInTheDocument();
    expect(screen.getByText('5,000')).toBeInTheDocument();
    expect(screen.getByText('3,000')).toBeInTheDocument();
    expect(screen.getByText('12,000')).toBeInTheDocument();
  });

  it('renders empty message when no entries', () => {
    render(<MonthlyBalanceTable entries={[]} />);
    expect(screen.getByText('月次残高がありません')).toBeInTheDocument();
  });
});

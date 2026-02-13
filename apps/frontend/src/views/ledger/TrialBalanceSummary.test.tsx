import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TrialBalanceSummary } from './TrialBalanceSummary';
import type { CategorySubtotal } from '../../api/getTrialBalance';

describe('TrialBalanceSummary', () => {
  it('renders totals and balanced status with date', () => {
    render(
      <TrialBalanceSummary
        date="2024-06-30"
        totalDebit={100000}
        totalCredit={100000}
        balanced={true}
        difference={0}
        categorySubtotals={[]}
      />
    );

    expect(screen.getByText(/基準日: 2024-06-30/)).toBeInTheDocument();
    expect(screen.getByText('借方合計')).toBeInTheDocument();
    expect(screen.getByText('貸方合計')).toBeInTheDocument();
    expect(screen.getAllByText('100,000')).toHaveLength(2);
    expect(screen.getByText('一致')).toBeInTheDocument();
  });

  it('renders "全期間" when date is null', () => {
    render(
      <TrialBalanceSummary
        date={null}
        totalDebit={50000}
        totalCredit={50000}
        balanced={true}
        difference={0}
        categorySubtotals={[]}
      />
    );

    expect(screen.getByText(/全期間/)).toBeInTheDocument();
  });

  it('renders unbalanced status with difference', () => {
    render(
      <TrialBalanceSummary
        date={null}
        totalDebit={100000}
        totalCredit={90000}
        balanced={false}
        difference={10000}
        categorySubtotals={[]}
      />
    );

    expect(screen.getByText(/不一致/)).toBeInTheDocument();
    expect(screen.getByText(/10,000/)).toBeInTheDocument();
  });

  it('renders category subtotals table', () => {
    const subtotals: CategorySubtotal[] = [
      {
        accountType: 'ASSET',
        accountTypeDisplayName: '資産',
        debitSubtotal: 45000,
        creditSubtotal: 0,
      },
      {
        accountType: 'LIABILITY',
        accountTypeDisplayName: '負債',
        debitSubtotal: 0,
        creditSubtotal: 25000,
      },
    ];

    render(
      <TrialBalanceSummary
        date={null}
        totalDebit={50000}
        totalCredit={30000}
        balanced={false}
        difference={20000}
        categorySubtotals={subtotals}
      />
    );

    expect(screen.getByText('勘定科目種別ごとの小計')).toBeInTheDocument();
    expect(screen.getByText('種別')).toBeInTheDocument();
    expect(screen.getByText('借方小計')).toBeInTheDocument();
    expect(screen.getByText('貸方小計')).toBeInTheDocument();
    expect(screen.getByText('資産')).toBeInTheDocument();
    expect(screen.getByText('負債')).toBeInTheDocument();
    expect(screen.getByText('45,000')).toBeInTheDocument();
    expect(screen.getByText('25,000')).toBeInTheDocument();
  });

  it('does not render category subtotals when empty', () => {
    render(
      <TrialBalanceSummary
        date={null}
        totalDebit={0}
        totalCredit={0}
        balanced={true}
        difference={0}
        categorySubtotals={[]}
      />
    );

    expect(screen.queryByText('勘定科目種別ごとの小計')).not.toBeInTheDocument();
  });
});

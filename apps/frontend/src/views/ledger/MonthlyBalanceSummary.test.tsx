import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MonthlyBalanceSummary } from './MonthlyBalanceSummary';

describe('MonthlyBalanceSummary', () => {
  it('renders account info with fiscal period and formatted totals', () => {
    render(
      <MonthlyBalanceSummary
        accountCode="1000"
        accountName="現金"
        fiscalPeriod={2024}
        openingBalance={10000}
        debitTotal={5000}
        creditTotal={3000}
        closingBalance={12000}
      />
    );

    expect(screen.getByText(/1000 現金/)).toBeInTheDocument();
    expect(screen.getByText(/2024年度/)).toBeInTheDocument();
    expect(screen.getByText('期首残高')).toBeInTheDocument();
    expect(screen.getByText('借方合計')).toBeInTheDocument();
    expect(screen.getByText('貸方合計')).toBeInTheDocument();
    expect(screen.getByText('期末残高')).toBeInTheDocument();
    expect(screen.getByText('10,000')).toBeInTheDocument();
    expect(screen.getByText('5,000')).toBeInTheDocument();
    expect(screen.getByText('3,000')).toBeInTheDocument();
    expect(screen.getByText('12,000')).toBeInTheDocument();
  });

  it('renders without fiscal period suffix when fiscalPeriod is null', () => {
    render(
      <MonthlyBalanceSummary
        accountCode="1000"
        accountName="現金"
        fiscalPeriod={null}
        openingBalance={0}
        debitTotal={0}
        creditTotal={0}
        closingBalance={0}
      />
    );

    expect(screen.getByText(/1000 現金/)).toBeInTheDocument();
    expect(screen.queryByText(/年度/)).not.toBeInTheDocument();
  });
});

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { DailyBalanceSummary } from './DailyBalanceSummary';

describe('DailyBalanceSummary', () => {
  it('renders account info and formatted totals', () => {
    render(
      <DailyBalanceSummary
        accountCode="101"
        accountName="現金"
        openingBalance={1000}
        debitTotal={2500}
        creditTotal={1500}
        closingBalance={2000}
      />
    );

    expect(screen.getByText('101 現金')).toBeInTheDocument();
    expect(screen.getByText('期首残高')).toBeInTheDocument();
    expect(screen.getByText('借方合計')).toBeInTheDocument();
    expect(screen.getByText('貸方合計')).toBeInTheDocument();
    expect(screen.getByText('期末残高')).toBeInTheDocument();

    expect(screen.getByText('1,000')).toBeInTheDocument();
    expect(screen.getByText('2,500')).toBeInTheDocument();
    expect(screen.getByText('1,500')).toBeInTheDocument();
    expect(screen.getByText('2,000')).toBeInTheDocument();
  });
});

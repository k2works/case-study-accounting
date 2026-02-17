import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SubsidiaryLedgerSummary } from './SubsidiaryLedgerSummary';

const renderSummary = (overrides = {}) =>
  render(
    <SubsidiaryLedgerSummary
      accountCode="101"
      accountName="現金"
      subAccountCode="A01"
      openingBalance={1000}
      debitTotal={2500}
      creditTotal={1500}
      closingBalance={2000}
      {...overrides}
    />
  );

describe('SubsidiaryLedgerSummary', () => {
  it('renders subsidiary account title with code, name, and sub-account', () => {
    renderSummary();
    expect(screen.getByText('科目: 101 現金 - A01')).toBeInTheDocument();
  });

  it('displays "前期繰越" label instead of "期首残高"', () => {
    renderSummary();
    expect(screen.getByText('前期繰越')).toBeInTheDocument();
    expect(screen.queryByText('期首残高')).not.toBeInTheDocument();
  });

  it('renders debit/credit totals and closing balance with formatted amounts', () => {
    renderSummary();
    expect(screen.getByText('借方合計')).toBeInTheDocument();
    expect(screen.getByText('2,500')).toBeInTheDocument();
    expect(screen.getByText('貸方合計')).toBeInTheDocument();
    expect(screen.getByText('1,500')).toBeInTheDocument();
    expect(screen.getByText('期末残高')).toBeInTheDocument();
    expect(screen.getByText('2,000')).toBeInTheDocument();
  });
});

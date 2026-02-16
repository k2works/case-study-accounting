import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BalanceSheetSummary } from './BalanceSheetSummary';

describe('BalanceSheetSummary', () => {
  it('renders date info and all totals with formatted amounts', () => {
    render(
      <BalanceSheetSummary
        date="2024-12-31"
        comparativeDate={null}
        totalAssets={100000}
        totalLiabilities={50000}
        totalEquity={50000}
        totalLiabilitiesAndEquity={100000}
        balanced={true}
        difference={0}
      />
    );

    expect(screen.getByText(/基準日: 2024-12-31/)).toBeInTheDocument();
    expect(screen.getByText('資産合計')).toBeInTheDocument();
    expect(screen.getByText('負債合計')).toBeInTheDocument();
    expect(screen.getByText('純資産合計')).toBeInTheDocument();
    expect(screen.getByText('負債・純資産合計')).toBeInTheDocument();
    expect(screen.getAllByText('100,000')).toHaveLength(2);
    expect(screen.getAllByText('50,000')).toHaveLength(2);
  });

  it('shows "一致" when balanced is true', () => {
    render(
      <BalanceSheetSummary
        date="2024-12-31"
        comparativeDate={null}
        totalAssets={100000}
        totalLiabilities={50000}
        totalEquity={50000}
        totalLiabilitiesAndEquity={100000}
        balanced={true}
        difference={0}
      />
    );

    expect(screen.getByText('一致')).toBeInTheDocument();
  });

  it('shows "不一致" with difference when balanced is false', () => {
    render(
      <BalanceSheetSummary
        date="2024-12-31"
        comparativeDate={null}
        totalAssets={100000}
        totalLiabilities={50000}
        totalEquity={40000}
        totalLiabilitiesAndEquity={90000}
        balanced={false}
        difference={10000}
      />
    );

    expect(screen.getByText(/不一致/)).toBeInTheDocument();
    expect(screen.getByText(/10,000/)).toBeInTheDocument();
  });

  it('shows "全期間" when date is null', () => {
    render(
      <BalanceSheetSummary
        date={null}
        comparativeDate={null}
        totalAssets={0}
        totalLiabilities={0}
        totalEquity={0}
        totalLiabilitiesAndEquity={0}
        balanced={true}
        difference={0}
      />
    );

    expect(screen.getByText(/全期間/)).toBeInTheDocument();
  });

  it('shows comparativeDate when provided', () => {
    render(
      <BalanceSheetSummary
        date="2024-12-31"
        comparativeDate="2023-12-31"
        totalAssets={100000}
        totalLiabilities={50000}
        totalEquity={50000}
        totalLiabilitiesAndEquity={100000}
        balanced={true}
        difference={0}
      />
    );

    expect(screen.getByText(/前期比較日: 2023-12-31/)).toBeInTheDocument();
  });
});

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProfitAndLossSummary } from './ProfitAndLossSummary';

describe('ProfitAndLossSummary', () => {
  it('renders period info and all totals with formatted amounts', () => {
    render(
      <ProfitAndLossSummary
        dateFrom="2024-01-01"
        dateTo="2024-12-31"
        comparativeDateFrom={null}
        comparativeDateTo={null}
        totalRevenue={1000000}
        totalExpense={700000}
        netIncome={300000}
      />
    );

    expect(screen.getByText(/2024-01-01 ~ 2024-12-31/)).toBeInTheDocument();
    expect(screen.getByText('収益合計')).toBeInTheDocument();
    expect(screen.getByText('費用合計')).toBeInTheDocument();
    expect(screen.getByText('当期純利益')).toBeInTheDocument();
    expect(screen.getByText('1,000,000')).toBeInTheDocument();
    expect(screen.getByText('700,000')).toBeInTheDocument();
    expect(screen.getByText('300,000')).toBeInTheDocument();
  });

  it('shows "全期間" when both dates are null', () => {
    render(
      <ProfitAndLossSummary
        dateFrom={null}
        dateTo={null}
        comparativeDateFrom={null}
        comparativeDateTo={null}
        totalRevenue={0}
        totalExpense={0}
        netIncome={0}
      />
    );

    expect(screen.getByText(/全期間/)).toBeInTheDocument();
  });

  it('shows comparative period when provided', () => {
    render(
      <ProfitAndLossSummary
        dateFrom="2024-01-01"
        dateTo="2024-12-31"
        comparativeDateFrom="2023-01-01"
        comparativeDateTo="2023-12-31"
        totalRevenue={1000000}
        totalExpense={700000}
        netIncome={300000}
      />
    );

    expect(screen.getByText(/前期比較: 2023-01-01 ~ 2023-12-31/)).toBeInTheDocument();
  });

  it('shows net income in green when positive', () => {
    render(
      <ProfitAndLossSummary
        dateFrom="2024-01-01"
        dateTo="2024-12-31"
        comparativeDateFrom={null}
        comparativeDateTo={null}
        totalRevenue={1000000}
        totalExpense={700000}
        netIncome={300000}
      />
    );

    const netIncomeValue = screen.getByText('300,000');
    expect(netIncomeValue).toHaveStyle({ color: '#16a34a' });
  });

  it('shows net income in red when negative', () => {
    render(
      <ProfitAndLossSummary
        dateFrom="2024-01-01"
        dateTo="2024-12-31"
        comparativeDateFrom={null}
        comparativeDateTo={null}
        totalRevenue={500000}
        totalExpense={700000}
        netIncome={-200000}
      />
    );

    const netIncomeValue = screen.getByText('-200,000');
    expect(netIncomeValue).toHaveStyle({ color: '#dc2626' });
  });
});

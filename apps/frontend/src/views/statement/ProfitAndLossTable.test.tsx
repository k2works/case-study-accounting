import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProfitAndLossTable } from './ProfitAndLossTable';
import type { ProfitAndLossSection } from '../../api/getProfitAndLoss';

describe('ProfitAndLossTable', () => {
  it('renders empty message when no section data', () => {
    render(
      <ProfitAndLossTable
        sections={[]}
        hasComparative={false}
        netIncome={0}
        comparativeNetIncome={null}
      />
    );
    expect(screen.getByText('損益計算書データがありません')).toBeInTheDocument();
  });

  it('renders empty message when sections have no entries', () => {
    const sections: ProfitAndLossSection[] = [
      {
        sectionType: 'REVENUE',
        sectionDisplayName: '収益の部',
        entries: [],
        subtotal: 0,
        comparativeSubtotal: null,
      },
    ];

    render(
      <ProfitAndLossTable
        sections={sections}
        hasComparative={false}
        netIncome={0}
        comparativeNetIncome={null}
      />
    );
    expect(screen.getByText('損益計算書データがありません')).toBeInTheDocument();
  });

  it('renders revenue and expense sections with net income', () => {
    const sections: ProfitAndLossSection[] = [
      {
        sectionType: 'REVENUE',
        sectionDisplayName: '収益の部',
        entries: [
          {
            accountCode: '401',
            accountName: '売上高',
            accountType: 'REVENUE',
            amount: 1000000,
            comparative: null,
          },
        ],
        subtotal: 1000000,
        comparativeSubtotal: null,
      },
      {
        sectionType: 'EXPENSE',
        sectionDisplayName: '費用の部',
        entries: [
          {
            accountCode: '501',
            accountName: '仕入高',
            accountType: 'EXPENSE',
            amount: 600000,
            comparative: null,
          },
        ],
        subtotal: 600000,
        comparativeSubtotal: null,
      },
    ];

    render(
      <ProfitAndLossTable
        sections={sections}
        hasComparative={false}
        netIncome={400000}
        comparativeNetIncome={null}
      />
    );

    expect(screen.getByText('収益の部')).toBeInTheDocument();
    expect(screen.getByText('費用の部')).toBeInTheDocument();
    expect(screen.getByText('401')).toBeInTheDocument();
    expect(screen.getByText('売上高')).toBeInTheDocument();
    expect(screen.getByText('501')).toBeInTheDocument();
    expect(screen.getByText('仕入高')).toBeInTheDocument();
    expect(screen.getByText('当期純利益')).toBeInTheDocument();
    expect(screen.getByText('400,000')).toBeInTheDocument();
  });

  it('renders column headers', () => {
    const sections: ProfitAndLossSection[] = [
      {
        sectionType: 'REVENUE',
        sectionDisplayName: '収益の部',
        entries: [
          {
            accountCode: '401',
            accountName: '売上高',
            accountType: 'REVENUE',
            amount: 1000000,
            comparative: null,
          },
        ],
        subtotal: 1000000,
        comparativeSubtotal: null,
      },
    ];

    render(
      <ProfitAndLossTable
        sections={sections}
        hasComparative={false}
        netIncome={1000000}
        comparativeNetIncome={null}
      />
    );

    expect(screen.getByText('コード')).toBeInTheDocument();
    expect(screen.getByText('勘定科目')).toBeInTheDocument();
    expect(screen.getByText('金額')).toBeInTheDocument();
  });

  it('renders comparative columns when hasComparative is true', () => {
    const sections: ProfitAndLossSection[] = [
      {
        sectionType: 'REVENUE',
        sectionDisplayName: '収益の部',
        entries: [
          {
            accountCode: '401',
            accountName: '売上高',
            accountType: 'REVENUE',
            amount: 1000000,
            comparative: {
              previousAmount: 800000,
              difference: 200000,
              changeRate: 25.0,
            },
          },
        ],
        subtotal: 1000000,
        comparativeSubtotal: {
          previousAmount: 800000,
          difference: 200000,
          changeRate: 25.0,
        },
      },
    ];

    render(
      <ProfitAndLossTable
        sections={sections}
        hasComparative={true}
        netIncome={1000000}
        comparativeNetIncome={800000}
      />
    );

    expect(screen.getByText('前期')).toBeInTheDocument();
    expect(screen.getByText('増減')).toBeInTheDocument();
    // 800,000: entry comparative + subtotal comparative + net income comparative = 3
    expect(screen.getAllByText('800,000')).toHaveLength(3);
    // 200,000: entry amount diff + subtotal diff + net income diff = 3
    expect(screen.getAllByText('200,000')).toHaveLength(3);
  });

  it('renders section subtotals', () => {
    const sections: ProfitAndLossSection[] = [
      {
        sectionType: 'REVENUE',
        sectionDisplayName: '収益の部',
        entries: [
          {
            accountCode: '401',
            accountName: '売上高',
            accountType: 'REVENUE',
            amount: 1000000,
            comparative: null,
          },
        ],
        subtotal: 1000000,
        comparativeSubtotal: null,
      },
    ];

    render(
      <ProfitAndLossTable
        sections={sections}
        hasComparative={false}
        netIncome={1000000}
        comparativeNetIncome={null}
      />
    );

    expect(screen.getByText('収益の部合計')).toBeInTheDocument();
  });

  it('renders dash for null comparative entries when hasComparative is true', () => {
    const sections: ProfitAndLossSection[] = [
      {
        sectionType: 'REVENUE',
        sectionDisplayName: '収益の部',
        entries: [
          {
            accountCode: '401',
            accountName: '売上高',
            accountType: 'REVENUE',
            amount: 1000000,
            comparative: null,
          },
        ],
        subtotal: 1000000,
        comparativeSubtotal: null,
      },
    ];

    render(
      <ProfitAndLossTable
        sections={sections}
        hasComparative={true}
        netIncome={1000000}
        comparativeNetIncome={null}
      />
    );

    expect(screen.getByText('前期')).toBeInTheDocument();
    expect(screen.getByText('増減')).toBeInTheDocument();
    const dashes = screen.getAllByText('-');
    expect(dashes.length).toBeGreaterThanOrEqual(4);
  });
});

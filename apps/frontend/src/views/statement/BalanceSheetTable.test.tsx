import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BalanceSheetTable } from './BalanceSheetTable';
import type { BalanceSheetSection } from '../../api/getBalanceSheet';

describe('BalanceSheetTable', () => {
  it('renders empty message when no section data', () => {
    render(<BalanceSheetTable sections={[]} hasComparative={false} />);
    expect(screen.getByText('貸借対照表データがありません')).toBeInTheDocument();
  });

  it('renders empty message when sections have no entries', () => {
    const sections: BalanceSheetSection[] = [
      {
        sectionType: 'ASSET',
        sectionDisplayName: '資産の部',
        entries: [],
        subtotal: 0,
        comparativeSubtotal: null,
      },
    ];

    render(<BalanceSheetTable sections={sections} hasComparative={false} />);
    expect(screen.getByText('貸借対照表データがありません')).toBeInTheDocument();
  });

  it('renders asset section and liability/equity sections', () => {
    const sections: BalanceSheetSection[] = [
      {
        sectionType: 'ASSET',
        sectionDisplayName: '資産の部',
        entries: [
          {
            accountCode: '101',
            accountName: '現金',
            accountType: 'ASSET',
            amount: 100000,
            comparative: null,
          },
        ],
        subtotal: 100000,
        comparativeSubtotal: null,
      },
      {
        sectionType: 'LIABILITY',
        sectionDisplayName: '負債の部',
        entries: [
          {
            accountCode: '201',
            accountName: '買掛金',
            accountType: 'LIABILITY',
            amount: 50000,
            comparative: null,
          },
        ],
        subtotal: 50000,
        comparativeSubtotal: null,
      },
      {
        sectionType: 'EQUITY',
        sectionDisplayName: '純資産の部',
        entries: [
          {
            accountCode: '301',
            accountName: '資本金',
            accountType: 'EQUITY',
            amount: 50000,
            comparative: null,
          },
        ],
        subtotal: 50000,
        comparativeSubtotal: null,
      },
    ];

    render(<BalanceSheetTable sections={sections} hasComparative={false} />);

    expect(screen.getByText('資産の部')).toBeInTheDocument();
    expect(screen.getByText('負債の部')).toBeInTheDocument();
    expect(screen.getByText('純資産の部')).toBeInTheDocument();
    expect(screen.getByText('101')).toBeInTheDocument();
    expect(screen.getByText('現金')).toBeInTheDocument();
    expect(screen.getByText('201')).toBeInTheDocument();
    expect(screen.getByText('買掛金')).toBeInTheDocument();
    expect(screen.getByText('301')).toBeInTheDocument();
    expect(screen.getByText('資本金')).toBeInTheDocument();
  });

  it('renders column headers', () => {
    const sections: BalanceSheetSection[] = [
      {
        sectionType: 'ASSET',
        sectionDisplayName: '資産の部',
        entries: [
          {
            accountCode: '101',
            accountName: '現金',
            accountType: 'ASSET',
            amount: 100000,
            comparative: null,
          },
        ],
        subtotal: 100000,
        comparativeSubtotal: null,
      },
    ];

    render(<BalanceSheetTable sections={sections} hasComparative={false} />);

    expect(screen.getAllByText('コード')).toHaveLength(2);
    expect(screen.getAllByText('勘定科目')).toHaveLength(2);
    expect(screen.getAllByText('金額')).toHaveLength(2);
  });

  it('renders comparative columns when hasComparative is true', () => {
    const sections: BalanceSheetSection[] = [
      {
        sectionType: 'ASSET',
        sectionDisplayName: '資産の部',
        entries: [
          {
            accountCode: '101',
            accountName: '現金',
            accountType: 'ASSET',
            amount: 100000,
            comparative: {
              previousAmount: 80000,
              difference: 20000,
              changeRate: 25.0,
            },
          },
        ],
        subtotal: 100000,
        comparativeSubtotal: {
          previousAmount: 80000,
          difference: 20000,
          changeRate: 25.0,
        },
      },
    ];

    render(<BalanceSheetTable sections={sections} hasComparative={true} />);

    expect(screen.getAllByText('前期')).toHaveLength(2);
    expect(screen.getAllByText('増減')).toHaveLength(2);
    expect(screen.getAllByText('80,000')).toHaveLength(2);
    expect(screen.getAllByText('20,000')).toHaveLength(2);
  });

  it('renders section subtotals', () => {
    const sections: BalanceSheetSection[] = [
      {
        sectionType: 'ASSET',
        sectionDisplayName: '資産の部',
        entries: [
          {
            accountCode: '101',
            accountName: '現金',
            accountType: 'ASSET',
            amount: 100000,
            comparative: null,
          },
        ],
        subtotal: 100000,
        comparativeSubtotal: null,
      },
    ];

    render(<BalanceSheetTable sections={sections} hasComparative={false} />);

    expect(screen.getByText('資産の部合計')).toBeInTheDocument();
  });
});

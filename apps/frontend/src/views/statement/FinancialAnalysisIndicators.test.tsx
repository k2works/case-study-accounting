import { describe, expect, it } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import { FinancialAnalysisIndicators } from './FinancialAnalysisIndicators';
import type { IndicatorCategory } from '../../api/getFinancialAnalysis';

const mockCategories: IndicatorCategory[] = [
  {
    categoryName: 'PROFITABILITY',
    categoryDisplayName: '収益性',
    indicators: [
      {
        name: 'ROE',
        unit: '%',
        value: 80,
        previousValue: 64.29,
        difference: 15.71,
        changeRate: 24.44,
        formula: '当期純利益 ÷ 自己資本 × 100',
        industryAverage: 8,
      },
    ],
  },
  {
    categoryName: 'SAFETY',
    categoryDisplayName: '安全性',
    indicators: [
      {
        name: '流動比率',
        unit: '%',
        value: 266.67,
        previousValue: null,
        difference: null,
        changeRate: null,
        formula: '流動資産 ÷ 流動負債 × 100',
        industryAverage: 200,
      },
    ],
  },
];

describe('FinancialAnalysisIndicators', () => {
  it('カテゴリ名が表示される', () => {
    render(<FinancialAnalysisIndicators categories={mockCategories} hasComparative={true} />);

    expect(screen.getByText('収益性')).toBeInTheDocument();
    expect(screen.getByText('安全性')).toBeInTheDocument();
  });

  it('指標名、当期値、計算式、業界平均が表示される', () => {
    render(<FinancialAnalysisIndicators categories={mockCategories} hasComparative={true} />);

    expect(screen.getByText('ROE')).toBeInTheDocument();
    expect(screen.getByText('80.00%')).toBeInTheDocument();
    expect(screen.getByText('当期純利益 ÷ 自己資本 × 100')).toBeInTheDocument();
    expect(screen.getByText('8.00%')).toBeInTheDocument();
  });

  it('hasComparative=true で前期、増減、変化率カラムが表示される', () => {
    render(<FinancialAnalysisIndicators categories={mockCategories} hasComparative={true} />);

    expect(screen.getByText('前期')).toBeInTheDocument();
    expect(screen.getByText('増減')).toBeInTheDocument();
    expect(screen.getByText('変化率')).toBeInTheDocument();
    expect(screen.getByText('64.29%')).toBeInTheDocument();
    expect(screen.getByText('15.71%')).toBeInTheDocument();
    expect(screen.getByText('24.44%')).toBeInTheDocument();
  });

  it('hasComparative=false で前期カラムが非表示', () => {
    render(<FinancialAnalysisIndicators categories={mockCategories} hasComparative={false} />);

    expect(screen.queryByText('前期')).not.toBeInTheDocument();
    expect(screen.queryByText('増減')).not.toBeInTheDocument();
    expect(screen.queryByText('変化率')).not.toBeInTheDocument();
  });

  it('空カテゴリで空テーブルが表示される', () => {
    render(<FinancialAnalysisIndicators categories={[]} hasComparative={false} />);

    expect(screen.getByTestId('financial-analysis-indicators')).toBeInTheDocument();
    expect(screen.getByText('指標名')).toBeInTheDocument();
    expect(screen.queryByText('収益性')).not.toBeInTheDocument();
  });

  it('comparative モードで previousValue が null の場合は - を表示', () => {
    render(<FinancialAnalysisIndicators categories={mockCategories} hasComparative={true} />);

    const row = screen.getByText('流動比率').closest('tr');
    expect(row).not.toBeNull();
    expect(within(row!).getAllByText('-').length).toBeGreaterThanOrEqual(1);
  });

  it('comparative モードで difference が null の場合は - を表示', () => {
    render(<FinancialAnalysisIndicators categories={mockCategories} hasComparative={true} />);

    const row = screen.getByText('流動比率').closest('tr');
    expect(row).not.toBeNull();
    expect(within(row!).getAllByText('-').length).toBeGreaterThanOrEqual(2);
  });

  it('comparative モードで changeRate が null の場合は - を表示', () => {
    render(<FinancialAnalysisIndicators categories={mockCategories} hasComparative={true} />);

    const row = screen.getByText('流動比率').closest('tr');
    expect(row).not.toBeNull();
    expect(within(row!).getAllByText('-').length).toBeGreaterThanOrEqual(3);
  });

  it('difference が正の値の場合は緑色 (#16a34a) になる', () => {
    render(<FinancialAnalysisIndicators categories={mockCategories} hasComparative={true} />);

    expect(screen.getByText('15.71%')).toHaveStyle({ color: '#16a34a' });
  });

  it('difference が負の値の場合は赤色 (#dc2626) になる', () => {
    const negativeCategories: IndicatorCategory[] = [
      {
        categoryName: 'EFFICIENCY',
        categoryDisplayName: '効率性',
        indicators: [
          {
            name: '回転率',
            unit: '%',
            value: 10,
            previousValue: 12,
            difference: -2,
            changeRate: -16.67,
            formula: '売上高 ÷ 総資産',
            industryAverage: 11,
          },
        ],
      },
    ];

    render(<FinancialAnalysisIndicators categories={negativeCategories} hasComparative={true} />);

    expect(screen.getByText('-2.00%')).toHaveStyle({ color: '#dc2626' });
  });

  it('difference が 0 または null の場合は標準色 (#333) になる', () => {
    const neutralCategories: IndicatorCategory[] = [
      {
        categoryName: 'STABILITY',
        categoryDisplayName: '安定性',
        indicators: [
          {
            name: '固定比率',
            unit: '%',
            value: 100,
            previousValue: 100,
            difference: 0,
            changeRate: 0,
            formula: '固定資産 ÷ 自己資本 × 100',
            industryAverage: 90,
          },
          {
            name: '自己資本比率',
            unit: '%',
            value: 50,
            previousValue: null,
            difference: null,
            changeRate: null,
            formula: '自己資本 ÷ 総資本 × 100',
            industryAverage: 40,
          },
        ],
      },
    ];

    render(<FinancialAnalysisIndicators categories={neutralCategories} hasComparative={true} />);

    const firstRow = screen.getByText('固定比率').closest('tr');
    expect(firstRow).not.toBeNull();
    expect(within(firstRow!).getAllByText('0.00%')[0]).toHaveStyle({ color: '#333' });

    const secondRow = screen.getByText('自己資本比率').closest('tr');
    expect(secondRow).not.toBeNull();
    const dashCells = within(secondRow!).getAllByText('-');
    expect(dashCells[1]).toHaveStyle({ color: '#333' });
  });
});

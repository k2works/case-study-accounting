import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
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
});

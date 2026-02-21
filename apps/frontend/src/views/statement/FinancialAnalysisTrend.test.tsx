import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { FinancialAnalysisTrend } from './FinancialAnalysisTrend';
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

describe('FinancialAnalysisTrend', () => {
  it('「指標比較チャート」見出しが表示される', () => {
    render(<FinancialAnalysisTrend categories={mockCategories} />);

    expect(screen.getByText('指標比較チャート')).toBeInTheDocument();
  });

  it('カテゴリ名が表示される', () => {
    render(<FinancialAnalysisTrend categories={mockCategories} />);

    expect(screen.getByText('収益性')).toBeInTheDocument();
    expect(screen.getByText('安全性')).toBeInTheDocument();
  });

  it('指標名と値が表示される', () => {
    render(<FinancialAnalysisTrend categories={mockCategories} />);

    expect(screen.getByText('ROE')).toBeInTheDocument();
    expect(screen.getByText('流動比率')).toBeInTheDocument();
    expect(screen.getByText('80.00%')).toBeInTheDocument();
    expect(screen.getByText('266.67%')).toBeInTheDocument();
  });

  it('「業界平均」ラベルが表示される', () => {
    render(<FinancialAnalysisTrend categories={mockCategories} />);

    expect(screen.getAllByText('業界平均')).toHaveLength(2);
  });

  it('空カテゴリで見出しのみ表示される', () => {
    render(<FinancialAnalysisTrend categories={[]} />);

    expect(screen.getByText('指標比較チャート')).toBeInTheDocument();
    expect(screen.queryByText('収益性')).not.toBeInTheDocument();
  });
});

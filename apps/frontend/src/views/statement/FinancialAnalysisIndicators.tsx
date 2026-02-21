import React from 'react';
import type { IndicatorCategory } from '../../api/getFinancialAnalysis';
import {
  tableStyle,
  headerCellStyle,
  headerRightStyle,
  cellStyle,
  rightCellStyle,
  sectionHeaderStyle,
} from './statementStyles';

interface FinancialAnalysisIndicatorsProps {
  categories: IndicatorCategory[];
  hasComparative: boolean;
}

const formatValue = (value: number | null | undefined, unit: string): string => {
  if (value == null) return '-';
  return `${value.toFixed(2)}${unit}`;
};

const changeColor = (diff: number | null): string => {
  if (diff == null) return '#333';
  if (diff > 0) return '#16a34a';
  if (diff < 0) return '#dc2626';
  return '#333';
};

export const FinancialAnalysisIndicators: React.FC<FinancialAnalysisIndicatorsProps> = ({
  categories,
  hasComparative,
}) => {
  return (
    <div data-testid="financial-analysis-indicators" style={{ marginTop: '16px' }}>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={headerCellStyle}>指標名</th>
            <th style={headerRightStyle}>当期</th>
            {hasComparative && <th style={headerRightStyle}>前期</th>}
            {hasComparative && <th style={headerRightStyle}>増減</th>}
            {hasComparative && <th style={headerRightStyle}>変化率</th>}
            <th style={headerCellStyle}>計算式</th>
            <th style={headerRightStyle}>業界平均</th>
          </tr>
        </thead>
        <tbody>
          {categories.map((cat) => (
            <React.Fragment key={cat.categoryName}>
              <tr>
                <td colSpan={hasComparative ? 7 : 4} style={sectionHeaderStyle}>
                  {cat.categoryDisplayName}
                </td>
              </tr>
              {cat.indicators.map((ind) => (
                <tr key={ind.name}>
                  <td style={cellStyle}>{ind.name}</td>
                  <td style={rightCellStyle}>{formatValue(ind.value, ind.unit)}</td>
                  {hasComparative && (
                    <td style={rightCellStyle}>{formatValue(ind.previousValue, ind.unit)}</td>
                  )}
                  {hasComparative && (
                    <td style={{ ...rightCellStyle, color: changeColor(ind.difference) }}>
                      {formatValue(ind.difference, ind.unit)}
                    </td>
                  )}
                  {hasComparative && (
                    <td style={{ ...rightCellStyle, color: changeColor(ind.changeRate) }}>
                      {ind.changeRate != null ? `${ind.changeRate.toFixed(2)}%` : '-'}
                    </td>
                  )}
                  <td style={{ ...cellStyle, fontSize: '12px', color: '#666' }}>{ind.formula}</td>
                  <td style={rightCellStyle}>{formatValue(ind.industryAverage, ind.unit)}</td>
                </tr>
              ))}
            </React.Fragment>
          ))}
        </tbody>
      </table>
    </div>
  );
};

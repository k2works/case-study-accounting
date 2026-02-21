import React from 'react';
import type { IndicatorCategory } from '../../api/getFinancialAnalysis';

interface FinancialAnalysisTrendProps {
  categories: IndicatorCategory[];
}

const barContainerStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  marginBottom: '8px',
};

const barLabelStyle: React.CSSProperties = {
  width: '180px',
  fontSize: '13px',
  textAlign: 'right',
  flexShrink: 0,
};

const barTrackStyle: React.CSSProperties = {
  flex: 1,
  height: '20px',
  backgroundColor: '#f0f0f0',
  borderRadius: '4px',
  position: 'relative',
  overflow: 'hidden',
};

const barStyle = (widthPercent: number, color: string): React.CSSProperties => ({
  height: '100%',
  width: `${Math.min(Math.max(widthPercent, 0), 100)}%`,
  backgroundColor: color,
  borderRadius: '4px',
  transition: 'width 0.3s ease',
});

const barValueStyle: React.CSSProperties = {
  width: '80px',
  fontSize: '13px',
  flexShrink: 0,
};

export const FinancialAnalysisTrend: React.FC<FinancialAnalysisTrendProps> = ({ categories }) => {
  // 全指標の最大値を計算してバーの幅を正規化
  const allValues = categories.flatMap((c) =>
    c.indicators.flatMap((i) => [Math.abs(i.value), Math.abs(i.industryAverage)])
  );
  const maxValue = Math.max(...allValues, 1);

  return (
    <div data-testid="financial-analysis-trend" style={{ marginTop: '24px' }}>
      <h3 style={{ fontSize: '16px', marginBottom: '16px' }}>指標比較チャート</h3>
      {categories.map((cat) => (
        <div key={cat.categoryName} style={{ marginBottom: '20px' }}>
          <div style={{ fontWeight: 600, fontSize: '14px', marginBottom: '8px', color: '#333' }}>
            {cat.categoryDisplayName}
          </div>
          {cat.indicators.map((ind) => {
            const valuePercent = (Math.abs(ind.value) / maxValue) * 100;
            const avgPercent = (Math.abs(ind.industryAverage) / maxValue) * 100;
            return (
              <div key={ind.name}>
                <div style={barContainerStyle}>
                  <div style={barLabelStyle}>{ind.name}</div>
                  <div style={barTrackStyle}>
                    <div style={barStyle(valuePercent, '#3b82f6')} />
                  </div>
                  <div style={barValueStyle}>
                    {ind.value.toFixed(2)}
                    {ind.unit}
                  </div>
                </div>
                <div style={barContainerStyle}>
                  <div style={{ ...barLabelStyle, color: '#999', fontSize: '12px' }}>業界平均</div>
                  <div style={barTrackStyle}>
                    <div style={barStyle(avgPercent, '#94a3b8')} />
                  </div>
                  <div style={{ ...barValueStyle, color: '#999' }}>
                    {ind.industryAverage.toFixed(2)}
                    {ind.unit}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      ))}
    </div>
  );
};

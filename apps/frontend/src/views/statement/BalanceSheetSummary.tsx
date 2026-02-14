import React from 'react';
import { formatCurrency } from '../../utils/formatCurrency';

interface BalanceSheetSummaryProps {
  date: string | null;
  comparativeDate: string | null;
  totalAssets: number;
  totalLiabilities: number;
  totalEquity: number;
  totalLiabilitiesAndEquity: number;
  balanced: boolean;
  difference: number;
}

const safeCurrency = (value: number | undefined | null): string => formatCurrency(value ?? 0);

export const BalanceSheetSummary: React.FC<BalanceSheetSummaryProps> = ({
  date,
  comparativeDate,
  totalAssets,
  totalLiabilities,
  totalEquity,
  totalLiabilitiesAndEquity,
  balanced,
  difference,
}) => {
  return (
    <div data-testid="balance-sheet-summary" style={{ marginTop: '16px' }}>
      <div style={{ marginBottom: '12px', fontWeight: 600 }}>
        貸借対照表 {date ? `（基準日: ${date}）` : '（全期間）'}
        {comparativeDate && (
          <span style={{ fontSize: '14px', color: '#666', marginLeft: '12px' }}>
            前期比較日: {comparativeDate}
          </span>
        )}
      </div>

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '24px', marginBottom: '12px' }}>
        <div>
          <div style={{ fontSize: '12px', color: '#666' }}>資産合計</div>
          <div style={{ fontSize: '18px', fontWeight: 600 }}>{safeCurrency(totalAssets)}</div>
        </div>
        <div>
          <div style={{ fontSize: '12px', color: '#666' }}>負債合計</div>
          <div style={{ fontSize: '18px', fontWeight: 600 }}>{safeCurrency(totalLiabilities)}</div>
        </div>
        <div>
          <div style={{ fontSize: '12px', color: '#666' }}>純資産合計</div>
          <div style={{ fontSize: '18px', fontWeight: 600 }}>{safeCurrency(totalEquity)}</div>
        </div>
        <div>
          <div style={{ fontSize: '12px', color: '#666' }}>負債・純資産合計</div>
          <div style={{ fontSize: '18px', fontWeight: 600 }}>
            {safeCurrency(totalLiabilitiesAndEquity)}
          </div>
        </div>
        <div>
          <div style={{ fontSize: '12px', color: '#666' }}>貸借一致</div>
          <div style={{ fontSize: '18px', fontWeight: 600 }}>
            {balanced ? (
              <span style={{ color: '#16a34a' }}>一致</span>
            ) : (
              <span style={{ color: '#dc2626' }}>不一致（差額: {safeCurrency(difference)}）</span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

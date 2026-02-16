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

interface SummaryItemProps {
  label: string;
  value: string;
}

const SummaryItem: React.FC<SummaryItemProps> = ({ label, value }) => (
  <div>
    <div style={{ fontSize: '12px', color: '#666' }}>{label}</div>
    <div style={{ fontSize: '18px', fontWeight: 600 }}>{value}</div>
  </div>
);

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
        <SummaryItem label="資産合計" value={safeCurrency(totalAssets)} />
        <SummaryItem label="負債合計" value={safeCurrency(totalLiabilities)} />
        <SummaryItem label="純資産合計" value={safeCurrency(totalEquity)} />
        <SummaryItem label="負債・純資産合計" value={safeCurrency(totalLiabilitiesAndEquity)} />
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

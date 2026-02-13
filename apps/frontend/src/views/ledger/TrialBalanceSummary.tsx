import React from 'react';
import { formatCurrency } from '../../utils/formatCurrency';
import type { CategorySubtotal } from '../../api/getTrialBalance';

interface TrialBalanceSummaryProps {
  date: string | null;
  totalDebit: number;
  totalCredit: number;
  balanced: boolean;
  difference: number;
  categorySubtotals: CategorySubtotal[];
}

const safeCurrency = (value: number | undefined | null): string => formatCurrency(value ?? 0);

export const TrialBalanceSummary: React.FC<TrialBalanceSummaryProps> = ({
  date,
  totalDebit,
  totalCredit,
  balanced,
  difference,
  categorySubtotals,
}) => {
  return (
    <div data-testid="trial-balance-summary" style={{ marginTop: '16px' }}>
      <div style={{ marginBottom: '12px', fontWeight: 600 }}>
        残高試算表 {date ? `（基準日: ${date}）` : '（全期間）'}
      </div>

      {/* 貸借合計 */}
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '24px', marginBottom: '12px' }}>
        <div>
          <div style={{ fontSize: '12px', color: '#666' }}>借方合計</div>
          <div style={{ fontSize: '18px', fontWeight: 600 }}>{safeCurrency(totalDebit)}</div>
        </div>
        <div>
          <div style={{ fontSize: '12px', color: '#666' }}>貸方合計</div>
          <div style={{ fontSize: '18px', fontWeight: 600 }}>{safeCurrency(totalCredit)}</div>
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

      {/* 科目種別小計 */}
      {categorySubtotals.length > 0 && (
        <div style={{ marginTop: '8px' }}>
          <div style={{ fontSize: '14px', fontWeight: 600, marginBottom: '4px' }}>
            勘定科目種別ごとの小計
          </div>
          <table style={{ borderCollapse: 'collapse', fontSize: '14px' }}>
            <thead>
              <tr>
                <th
                  style={{ padding: '4px 12px', borderBottom: '1px solid #ddd', textAlign: 'left' }}
                >
                  種別
                </th>
                <th
                  style={{
                    padding: '4px 12px',
                    borderBottom: '1px solid #ddd',
                    textAlign: 'right',
                  }}
                >
                  借方小計
                </th>
                <th
                  style={{
                    padding: '4px 12px',
                    borderBottom: '1px solid #ddd',
                    textAlign: 'right',
                  }}
                >
                  貸方小計
                </th>
              </tr>
            </thead>
            <tbody>
              {categorySubtotals.map((sub) => (
                <tr key={sub.accountType}>
                  <td style={{ padding: '4px 12px', borderBottom: '1px solid #eee' }}>
                    {sub.accountTypeDisplayName}
                  </td>
                  <td
                    style={{
                      padding: '4px 12px',
                      borderBottom: '1px solid #eee',
                      textAlign: 'right',
                    }}
                  >
                    {safeCurrency(sub.debitSubtotal)}
                  </td>
                  <td
                    style={{
                      padding: '4px 12px',
                      borderBottom: '1px solid #eee',
                      textAlign: 'right',
                    }}
                  >
                    {safeCurrency(sub.creditSubtotal)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

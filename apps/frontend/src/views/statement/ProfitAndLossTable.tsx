import React from 'react';
import type { ProfitAndLossSection } from '../../api/getProfitAndLoss';
import { tableStyle, safeCurrency } from './statementStyles';
import { TableHeader, renderSection, hasSectionData } from './StatementTableParts';

interface ProfitAndLossTableProps {
  sections: ProfitAndLossSection[];
  hasComparative: boolean;
  netIncome: number;
  comparativeNetIncome: number | null;
}

const netIncomeStyle: React.CSSProperties = {
  padding: '8px 12px',
  borderTop: '3px double #333',
  fontWeight: 700,
  fontSize: '15px',
};

const netIncomeRightStyle: React.CSSProperties = {
  ...netIncomeStyle,
  textAlign: 'right',
};

export const ProfitAndLossTable: React.FC<ProfitAndLossTableProps> = ({
  sections,
  hasComparative,
  netIncome,
  comparativeNetIncome,
}) => {
  if (!hasSectionData(sections)) {
    return (
      <div data-testid="profit-and-loss-table" style={{ marginTop: '16px' }}>
        <p>損益計算書データがありません</p>
      </div>
    );
  }

  return (
    <div data-testid="profit-and-loss-table" style={{ marginTop: '16px' }}>
      <table style={tableStyle}>
        <TableHeader hasComparative={hasComparative} />
        <tbody>
          {sections.map((s) => renderSection(s, hasComparative))}
          <tr>
            <td style={netIncomeStyle} />
            <td style={netIncomeStyle}>当期純利益</td>
            <td style={netIncomeRightStyle}>{safeCurrency(netIncome)}</td>
            {hasComparative && (
              <>
                <td style={netIncomeRightStyle}>
                  {comparativeNetIncome != null ? safeCurrency(comparativeNetIncome) : '-'}
                </td>
                <td style={netIncomeRightStyle}>
                  {comparativeNetIncome != null
                    ? safeCurrency(netIncome - comparativeNetIncome)
                    : '-'}
                </td>
              </>
            )}
          </tr>
        </tbody>
      </table>
    </div>
  );
};

import React from 'react';
import type { ProfitAndLossSection } from '../../api/getProfitAndLoss';
import { formatCurrency } from '../../utils/formatCurrency';

interface ProfitAndLossTableProps {
  sections: ProfitAndLossSection[];
  hasComparative: boolean;
  netIncome: number;
  comparativeNetIncome: number | null;
}

const safeCurrency = (value: number | undefined | null): string => formatCurrency(value ?? 0);

const cellStyle: React.CSSProperties = {
  padding: '6px 12px',
  borderBottom: '1px solid #eee',
};

const rightCellStyle: React.CSSProperties = {
  ...cellStyle,
  textAlign: 'right',
};

const headerCellStyle: React.CSSProperties = {
  padding: '8px 12px',
  borderBottom: '2px solid #333',
  fontWeight: 600,
  textAlign: 'left',
};

const headerRightStyle: React.CSSProperties = {
  ...headerCellStyle,
  textAlign: 'right',
};

const sectionHeaderStyle: React.CSSProperties = {
  padding: '8px 12px',
  borderBottom: '1px solid #ddd',
  fontWeight: 600,
  backgroundColor: '#f5f5f5',
};

const subtotalStyle: React.CSSProperties = {
  padding: '6px 12px',
  borderBottom: '2px solid #666',
  fontWeight: 600,
  textAlign: 'right',
};

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

const renderSection = (section: ProfitAndLossSection, hasComparative: boolean) => (
  <React.Fragment key={section.sectionType}>
    <tr>
      <td colSpan={hasComparative ? 5 : 3} style={sectionHeaderStyle}>
        {section.sectionDisplayName}
      </td>
    </tr>
    {section.entries.map((entry) => (
      <tr key={entry.accountCode}>
        <td style={cellStyle}>{entry.accountCode}</td>
        <td style={cellStyle}>{entry.accountName}</td>
        <td style={rightCellStyle}>{safeCurrency(entry.amount)}</td>
        {hasComparative && (
          <>
            <td style={rightCellStyle}>
              {entry.comparative ? safeCurrency(entry.comparative.previousAmount) : '-'}
            </td>
            <td style={rightCellStyle}>
              {entry.comparative ? safeCurrency(entry.comparative.difference) : '-'}
            </td>
          </>
        )}
      </tr>
    ))}
    <tr>
      <td style={cellStyle} />
      <td style={{ ...cellStyle, fontWeight: 600 }}>{section.sectionDisplayName}合計</td>
      <td style={subtotalStyle}>{safeCurrency(section.subtotal)}</td>
      {hasComparative && (
        <>
          <td style={subtotalStyle}>
            {section.comparativeSubtotal
              ? safeCurrency(section.comparativeSubtotal.previousAmount)
              : '-'}
          </td>
          <td style={subtotalStyle}>
            {section.comparativeSubtotal
              ? safeCurrency(section.comparativeSubtotal.difference)
              : '-'}
          </td>
        </>
      )}
    </tr>
  </React.Fragment>
);

interface TableHeaderProps {
  hasComparative: boolean;
}

const TableHeader: React.FC<TableHeaderProps> = ({ hasComparative }) => (
  <thead>
    <tr>
      <th style={headerCellStyle}>コード</th>
      <th style={headerCellStyle}>勘定科目</th>
      <th style={headerRightStyle}>金額</th>
      {hasComparative && (
        <>
          <th style={headerRightStyle}>前期</th>
          <th style={headerRightStyle}>増減</th>
        </>
      )}
    </tr>
  </thead>
);

const tableStyle: React.CSSProperties = {
  width: '100%',
  borderCollapse: 'collapse',
  fontSize: '14px',
};

const hasSectionData = (sections: ProfitAndLossSection[]): boolean =>
  sections.some((s) => s.entries.length > 0);

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

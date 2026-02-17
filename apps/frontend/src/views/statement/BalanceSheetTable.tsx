import React from 'react';
import type { BalanceSheetSection } from '../../api/getBalanceSheet';
import { tableStyle } from './statementStyles';
import { TableHeader, renderSection, hasSectionData } from './StatementTableParts';

interface BalanceSheetTableProps {
  sections: BalanceSheetSection[];
  hasComparative: boolean;
}

export const BalanceSheetTable: React.FC<BalanceSheetTableProps> = ({
  sections,
  hasComparative,
}) => {
  if (!hasSectionData(sections)) {
    return (
      <div data-testid="balance-sheet-table" style={{ marginTop: '16px' }}>
        <p>貸借対照表データがありません</p>
      </div>
    );
  }

  const assetSection = sections.find((s) => s.sectionType === 'ASSET');
  const liabilitySections = sections.filter(
    (s) => s.sectionType === 'LIABILITY' || s.sectionType === 'EQUITY'
  );

  return (
    <div data-testid="balance-sheet-table" style={{ marginTop: '16px' }}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        <div>
          <table style={tableStyle}>
            <TableHeader hasComparative={hasComparative} />
            <tbody>{assetSection && renderSection(assetSection, hasComparative)}</tbody>
          </table>
        </div>
        <div>
          <table style={tableStyle}>
            <TableHeader hasComparative={hasComparative} />
            <tbody>{liabilitySections.map((s) => renderSection(s, hasComparative))}</tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

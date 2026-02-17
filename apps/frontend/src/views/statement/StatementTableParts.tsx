/* eslint-disable react-refresh/only-export-components */
import React from 'react';
import type { ComparativeData } from '../../api/statementShared';
import {
  cellStyle,
  rightCellStyle,
  headerCellStyle,
  headerRightStyle,
  sectionHeaderStyle,
  subtotalStyle,
  safeCurrency,
} from './statementStyles';

export interface StatementEntry {
  accountCode: string;
  accountName: string;
  accountType: string;
  amount: number;
  comparative: ComparativeData | null;
  [key: string]: unknown;
}

export interface StatementSection {
  sectionType: string;
  sectionDisplayName: string;
  entries: StatementEntry[];
  subtotal: number;
  comparativeSubtotal: ComparativeData | null;
}

interface TableHeaderProps {
  hasComparative: boolean;
}

export const TableHeader: React.FC<TableHeaderProps> = ({ hasComparative }) => (
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

export const renderSection = (section: StatementSection, hasComparative: boolean) => (
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

export const hasSectionData = (sections: StatementSection[]): boolean =>
  sections.some((s) => s.entries.length > 0);

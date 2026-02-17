import React from 'react';
import { formatCurrency } from '../../utils/formatCurrency';

export const safeCurrency = (value: number | undefined | null): string =>
  formatCurrency(value ?? 0);

export const cellStyle: React.CSSProperties = {
  padding: '6px 12px',
  borderBottom: '1px solid #eee',
};

export const rightCellStyle: React.CSSProperties = {
  ...cellStyle,
  textAlign: 'right',
};

export const headerCellStyle: React.CSSProperties = {
  padding: '8px 12px',
  borderBottom: '2px solid #333',
  fontWeight: 600,
  textAlign: 'left',
};

export const headerRightStyle: React.CSSProperties = {
  ...headerCellStyle,
  textAlign: 'right',
};

export const sectionHeaderStyle: React.CSSProperties = {
  padding: '8px 12px',
  borderBottom: '1px solid #ddd',
  fontWeight: 600,
  backgroundColor: '#f5f5f5',
};

export const subtotalStyle: React.CSSProperties = {
  padding: '6px 12px',
  borderBottom: '2px solid #666',
  fontWeight: 600,
  textAlign: 'right',
};

export const tableStyle: React.CSSProperties = {
  width: '100%',
  borderCollapse: 'collapse',
  fontSize: '14px',
};

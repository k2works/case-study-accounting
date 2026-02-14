import React from 'react';
import { renderCurrency } from './renderCurrency';

interface CurrencyColumnDef {
  key: string;
  header: string;
  width: string;
  align: 'right';
  render: (value: unknown) => React.ReactElement;
}

export const currencyColumn = (key: string, header: string): CurrencyColumnDef => ({
  key,
  header,
  width: '140px',
  align: 'right',
  render: renderCurrency,
});

import React from 'react';
import { formatCurrency } from '../../utils/formatCurrency';

export const renderCurrency = (value: unknown): React.ReactElement => (
  <>{formatCurrency(value as number)}</>
);

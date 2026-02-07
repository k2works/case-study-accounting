import { describe, it, expect } from 'vitest';
import { formatCurrency } from './formatCurrency';

describe('formatCurrency', () => {
  it('formats number with Japanese locale separators', () => {
    expect(formatCurrency(1234567)).toBe('1,234,567');
  });

  it('formats zero correctly', () => {
    expect(formatCurrency(0)).toBe('0');
  });
});

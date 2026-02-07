import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DailyBalanceFilter } from './DailyBalanceFilter';

vi.mock('./LedgerFilterBase', () => ({
  LedgerFilterBase: ({
    testIdPrefix,
    values,
    onChange,
    onSearch,
  }: {
    testIdPrefix: string;
    values: { accountId: string; dateFrom: string; dateTo: string };
    onChange: (values: { accountId: string; dateFrom: string; dateTo: string }) => void;
    onSearch: () => void;
  }) => (
    <div data-testid="ledger-filter-base" data-prefix={testIdPrefix}>
      <div data-testid="current-account">{values.accountId}</div>
      <button type="button" onClick={() => onChange({ ...values, accountId: '99' })}>
        change
      </button>
      <button type="button" onClick={onSearch}>
        search
      </button>
    </div>
  ),
}));

describe('DailyBalanceFilter', () => {
  it('passes props to LedgerFilterBase and handles callbacks', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const onSearch = vi.fn();
    const values = { accountId: '1', dateFrom: '2024-01-01', dateTo: '2024-01-31' };

    render(<DailyBalanceFilter values={values} onChange={onChange} onSearch={onSearch} />);

    expect(screen.getByTestId('ledger-filter-base')).toHaveAttribute(
      'data-prefix',
      'daily-balance'
    );
    expect(screen.getByTestId('current-account')).toHaveTextContent('1');

    await user.click(screen.getByRole('button', { name: 'change' }));
    expect(onChange).toHaveBeenCalledWith({ ...values, accountId: '99' });

    await user.click(screen.getByRole('button', { name: 'search' }));
    expect(onSearch).toHaveBeenCalled();
  });
});

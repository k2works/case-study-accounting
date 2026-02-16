import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SubsidiaryLedgerFilter } from './SubsidiaryLedgerFilter';

const mockFetchAccounts = vi.fn();

vi.mock('./useAccountOptions', () => ({
  useAccountOptions: () => ({
    accounts: [
      { accountId: 1, accountCode: '1000', accountName: '現金' },
      { accountId: 2, accountCode: '1100', accountName: '普通預金' },
    ],
    isLoading: false,
    errorMessage: null,
    fetchAccounts: mockFetchAccounts,
  }),
}));

describe('SubsidiaryLedgerFilter', () => {
  it('renders account select, sub account input, date inputs, and search button', () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <SubsidiaryLedgerFilter
        values={{ accountCode: '', subAccountCode: '', dateFrom: '', dateTo: '' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    expect(screen.getByLabelText('勘定科目')).toBeInTheDocument();
    expect(screen.getByLabelText('補助科目')).toBeInTheDocument();
    expect(screen.getByLabelText('期間（開始）')).toBeInTheDocument();
    expect(screen.getByLabelText('期間（終了）')).toBeInTheDocument();
    expect(screen.getByText('照会')).toBeInTheDocument();
    expect(screen.getByText('勘定科目を選択')).toBeInTheDocument();
    expect(screen.getByText('1000 現金')).toBeInTheDocument();
    expect(screen.getByText('1100 普通預金')).toBeInTheDocument();
  });

  it('calls onChange when account is selected', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <SubsidiaryLedgerFilter
        values={{ accountCode: '', subAccountCode: '', dateFrom: '', dateTo: '' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    await user.selectOptions(screen.getByLabelText('勘定科目'), '1000');
    expect(onChange).toHaveBeenCalledWith({
      accountCode: '1000',
      subAccountCode: '',
      dateFrom: '',
      dateTo: '',
    });
  });

  it('calls onChange when sub account code changes', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <SubsidiaryLedgerFilter
        values={{ accountCode: '1000', subAccountCode: '', dateFrom: '', dateTo: '' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    await user.type(screen.getByLabelText('補助科目'), 'A01');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onSearch when search button is clicked', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <SubsidiaryLedgerFilter
        values={{
          accountCode: '1000',
          subAccountCode: '',
          dateFrom: '2024-01-01',
          dateTo: '2024-12-31',
        }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    await user.click(screen.getByText('照会'));
    expect(onSearch).toHaveBeenCalled();
  });
});

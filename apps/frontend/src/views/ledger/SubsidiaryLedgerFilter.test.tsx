import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SubsidiaryLedgerFilter } from './SubsidiaryLedgerFilter';
import type { SubsidiaryLedgerFilterValues } from './SubsidiaryLedgerFilter';

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

const emptyValues: SubsidiaryLedgerFilterValues = {
  accountCode: '',
  subAccountCode: '',
  dateFrom: '',
  dateTo: '',
};

describe('SubsidiaryLedgerFilter', () => {
  const onChange = vi.fn();
  const onSearch = vi.fn();

  const renderFilter = (overrides: Partial<SubsidiaryLedgerFilterValues> = {}) =>
    render(
      <SubsidiaryLedgerFilter
        values={{ ...emptyValues, ...overrides }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders account select, sub account input, date inputs, and search button', () => {
    renderFilter();
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
    renderFilter();
    await userEvent.setup().selectOptions(screen.getByLabelText('勘定科目'), '1000');
    expect(onChange).toHaveBeenCalledWith({ ...emptyValues, accountCode: '1000' });
  });

  it('calls onChange when sub account code changes', async () => {
    renderFilter({ accountCode: '1000' });
    await userEvent.setup().type(screen.getByLabelText('補助科目'), 'A01');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onSearch when search button is clicked', async () => {
    renderFilter({ accountCode: '1000', dateFrom: '2024-01-01', dateTo: '2024-12-31' });
    await userEvent.setup().click(screen.getByText('照会'));
    expect(onSearch).toHaveBeenCalled();
  });
});

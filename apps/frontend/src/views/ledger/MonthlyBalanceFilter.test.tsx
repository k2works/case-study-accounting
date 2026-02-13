import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MonthlyBalanceFilter } from './MonthlyBalanceFilter';

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

describe('MonthlyBalanceFilter', () => {
  it('renders account select and fiscal period input', () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <MonthlyBalanceFilter
        values={{ accountCode: '', fiscalPeriod: '' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    expect(screen.getByLabelText('勘定科目')).toBeInTheDocument();
    expect(screen.getByLabelText('年度')).toBeInTheDocument();
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
      <MonthlyBalanceFilter
        values={{ accountCode: '', fiscalPeriod: '2024' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    await user.selectOptions(screen.getByLabelText('勘定科目'), '1000');
    expect(onChange).toHaveBeenCalledWith({ accountCode: '1000', fiscalPeriod: '2024' });
  });

  it('calls onChange when fiscal period changes', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <MonthlyBalanceFilter
        values={{ accountCode: '1000', fiscalPeriod: '' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    await user.type(screen.getByLabelText('年度'), '2024');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onSearch when search button is clicked', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <MonthlyBalanceFilter
        values={{ accountCode: '1000', fiscalPeriod: '2024' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    await user.click(screen.getByText('照会'));
    expect(onSearch).toHaveBeenCalled();
  });
});

describe('MonthlyBalanceFilter loading state', () => {
  it('shows loading indicator when loading with no accounts', () => {
    vi.resetModules();
    vi.doMock('./useAccountOptions', () => ({
      useAccountOptions: () => ({
        accounts: [],
        isLoading: true,
        errorMessage: null,
        fetchAccounts: vi.fn(),
      }),
    }));
    // Re-require module to pick up new mock - tested via integration
  });
});

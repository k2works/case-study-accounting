import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BalanceSheetFilter } from './BalanceSheetFilter';

describe('BalanceSheetFilter', () => {
  it('renders date input, comparative date input, and search button', () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <BalanceSheetFilter
        values={{ date: '', comparativeDate: '' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    expect(screen.getByLabelText('基準日')).toBeInTheDocument();
    expect(screen.getByLabelText('前期比較日')).toBeInTheDocument();
    expect(screen.getByText('表示')).toBeInTheDocument();
  });

  it('calls onChange when date is changed', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <BalanceSheetFilter
        values={{ date: '', comparativeDate: '' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    await user.type(screen.getByLabelText('基準日'), '2024-12-31');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onChange when comparative date is changed', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <BalanceSheetFilter
        values={{ date: '2024-12-31', comparativeDate: '' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    await user.type(screen.getByLabelText('前期比較日'), '2023-12-31');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onSearch when search button is clicked', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const onSearch = vi.fn();
    render(
      <BalanceSheetFilter
        values={{ date: '2024-12-31', comparativeDate: '' }}
        onChange={onChange}
        onSearch={onSearch}
      />
    );

    await user.click(screen.getByText('表示'));
    expect(onSearch).toHaveBeenCalled();
  });
});

import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuditLogFilter } from './AuditLogFilter';

const EMPTY_VALUES = { userId: '', actionType: '', dateFrom: '', dateTo: '' };

const setup = () => {
  const onChange = vi.fn();
  const onSearch = vi.fn();
  render(<AuditLogFilter values={EMPTY_VALUES} onChange={onChange} onSearch={onSearch} />);
  return { onChange, onSearch };
};

describe('AuditLogFilter', () => {
  it('renders all filter controls and search button', () => {
    setup();
    expect(screen.getByLabelText('ユーザーID')).toBeInTheDocument();
    expect(screen.getByLabelText('アクション')).toBeInTheDocument();
    expect(screen.getByLabelText('開始日')).toBeInTheDocument();
    expect(screen.getByLabelText('終了日')).toBeInTheDocument();
    expect(screen.getByText('検索')).toBeInTheDocument();
  });

  it('calls onChange when userId is changed', async () => {
    const { onChange } = setup();
    await userEvent.setup().type(screen.getByLabelText('ユーザーID'), 'a');
    expect(onChange).toHaveBeenCalled();
  });

  it('calls onChange with updated actionType', async () => {
    const { onChange } = setup();
    await userEvent.setup().selectOptions(screen.getByLabelText('アクション'), 'LOGIN');
    expect(onChange).toHaveBeenCalledWith({ ...EMPTY_VALUES, actionType: 'LOGIN' });
  });

  it('calls onSearch when search button is clicked', async () => {
    const { onSearch } = setup();
    await userEvent.setup().click(screen.getByText('検索'));
    expect(onSearch).toHaveBeenCalled();
  });
});

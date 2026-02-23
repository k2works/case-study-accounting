import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuditLogFilter } from './AuditLogFilter';
import type { AuditLogFilterValues } from './AuditLogFilter';

const defaultValues: AuditLogFilterValues = {
  userId: '',
  actionType: '',
  dateFrom: '',
  dateTo: '',
};

describe('AuditLogFilter', () => {
  const onChange = vi.fn();
  const onSearch = vi.fn();

  const renderFilter = (values: AuditLogFilterValues = defaultValues) =>
    render(<AuditLogFilter values={values} onChange={onChange} onSearch={onSearch} />);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders all filter controls and search button', () => {
    renderFilter();

    expect(screen.getByLabelText('ユーザーID')).toBeInTheDocument();
    expect(screen.getByLabelText('アクション')).toBeInTheDocument();
    expect(screen.getByLabelText('開始日')).toBeInTheDocument();
    expect(screen.getByLabelText('終了日')).toBeInTheDocument();
    expect(screen.getByText('検索')).toBeInTheDocument();
  });

  it('calls onChange when userId is changed', async () => {
    renderFilter();

    await userEvent.setup().type(screen.getByLabelText('ユーザーID'), 'admin');

    expect(onChange).toHaveBeenCalled();
  });

  it('calls onChange when actionType is changed', async () => {
    renderFilter();

    await userEvent.setup().selectOptions(screen.getByLabelText('アクション'), 'LOGIN');

    expect(onChange).toHaveBeenCalledWith({
      userId: '',
      actionType: 'LOGIN',
      dateFrom: '',
      dateTo: '',
    });
  });

  it('calls onSearch when search button is clicked', async () => {
    renderFilter();

    await userEvent.setup().click(screen.getByText('検索'));

    expect(onSearch).toHaveBeenCalled();
  });
});

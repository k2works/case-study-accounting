import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TrialBalanceFilter } from './TrialBalanceFilter';

describe('TrialBalanceFilter', () => {
  it('renders date input and search button', () => {
    render(<TrialBalanceFilter values={{ date: '' }} onChange={vi.fn()} onSearch={vi.fn()} />);

    expect(screen.getByLabelText('基準日')).toBeInTheDocument();
    expect(screen.getByText('表示')).toBeInTheDocument();
  });

  it('calls onChange when date is changed', () => {
    const onChange = vi.fn();
    render(<TrialBalanceFilter values={{ date: '' }} onChange={onChange} onSearch={vi.fn()} />);

    fireEvent.change(screen.getByLabelText('基準日'), {
      target: { value: '2024-06-30' },
    });
    expect(onChange).toHaveBeenCalledWith({ date: '2024-06-30' });
  });

  it('calls onSearch when search button is clicked', async () => {
    const user = userEvent.setup();
    const onSearch = vi.fn();
    render(
      <TrialBalanceFilter values={{ date: '2024-06-30' }} onChange={vi.fn()} onSearch={onSearch} />
    );

    await user.click(screen.getByText('表示'));
    expect(onSearch).toHaveBeenCalled();
  });
});

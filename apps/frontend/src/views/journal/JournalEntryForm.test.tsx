import type { ComponentProps } from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { JournalEntryForm } from './JournalEntryForm';
import type { CreateJournalEntryRequest } from '../../api/createJournalEntry';

const accounts = [
  { accountId: 1, accountCode: '1000', accountName: '現金' },
  { accountId: 2, accountCode: '2000', accountName: '売上' },
];

const setup = (overrides?: Partial<ComponentProps<typeof JournalEntryForm>>) => {
  const onSubmit = vi.fn().mockResolvedValue(undefined);
  const onCancel = vi.fn();
  render(
    <JournalEntryForm
      accounts={accounts}
      onSubmit={onSubmit}
      onCancel={onCancel}
      isSubmitting={false}
      {...overrides}
    />
  );
  return { onSubmit, onCancel };
};

const setupUser = () => userEvent.setup();

const dateInput = () => screen.getByTestId('journal-entry-date-input');
const descriptionInput = () => screen.getByTestId('journal-entry-description-input');
const accountSelect = () => screen.getByTestId('journal-entry-account-0');
const debitInput = () => screen.getByTestId('journal-entry-debit-0');
const creditInput = () => screen.getByTestId('journal-entry-credit-0');
const submitButton = () => screen.getByTestId('journal-entry-submit');
const addLineButton = () => screen.getByTestId('journal-entry-add-line');

const fillHeader = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.type(dateInput(), '2024-01-31');
  await user.type(descriptionInput(), '売上計上');
};

describe('JournalEntryForm', () => {
  it('renders form fields', () => {
    setup();
    expect(dateInput()).toBeInTheDocument();
    expect(descriptionInput()).toBeInTheDocument();
    expect(accountSelect()).toBeInTheDocument();
    expect(debitInput()).toBeInTheDocument();
    expect(creditInput()).toBeInTheDocument();
    expect(addLineButton()).toBeInTheDocument();
    expect(submitButton()).toBeInTheDocument();
  });

  it('adds a new line', async () => {
    setup();
    const user = setupUser();

    await user.click(addLineButton());

    expect(screen.getByTestId('journal-entry-account-1')).toBeInTheDocument();
    expect(screen.getByTestId('journal-entry-debit-1')).toBeInTheDocument();
  });

  it('clears the opposite amount when entering debit or credit', async () => {
    setup();
    const user = setupUser();

    await user.type(debitInput(), '1000');
    expect(creditInput()).toHaveValue(null);

    await user.type(creditInput(), '2000');
    expect(debitInput()).toHaveValue(null);
  });

  it('disables submit when totals are unbalanced', async () => {
    setup();
    const user = setupUser();

    await fillHeader(user);
    await user.selectOptions(accountSelect(), '1');
    await user.type(debitInput(), '1000');

    expect(submitButton()).toBeDisabled();
    expect(screen.getByTestId('journal-entry-diff')).toHaveTextContent('差額');
  });

  it('submits balanced journal entry', async () => {
    const { onSubmit } = setup();
    const user = setupUser();

    await fillHeader(user);
    await user.selectOptions(accountSelect(), '1');
    await user.type(debitInput(), '1000');
    await user.click(addLineButton());
    await user.selectOptions(screen.getByTestId('journal-entry-account-1'), '2');
    await user.type(screen.getByTestId('journal-entry-credit-1'), '1000');

    await user.click(submitButton());

    await waitFor(() => {
      const expected: CreateJournalEntryRequest = {
        journalDate: '2024-01-31',
        description: '売上計上',
        lines: [
          { lineNumber: 1, accountId: 1, debitAmount: 1000 },
          { lineNumber: 2, accountId: 2, creditAmount: 1000 },
        ],
      };
      expect(onSubmit).toHaveBeenCalledWith(expected);
    });
  });
});

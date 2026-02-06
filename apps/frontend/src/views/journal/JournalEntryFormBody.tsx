import React from 'react';
import type { JournalEntryFormState, FormAccount } from './journalEntryFormUtils';
import { Button, ErrorMessage, MoneyDisplay } from '../common';

interface JournalEntryFormBodyProps {
  form: JournalEntryFormState;
  accounts: FormAccount[];
  isSubmitting: boolean;
  error?: string;
}

export const JournalEntryFormBody: React.FC<JournalEntryFormBodyProps> = ({
  form,
  accounts,
  isSubmitting,
  error,
}) => (
  <>
    <div className="journal-entry-form__field">
      <label htmlFor="journalDate" className="journal-entry-form__label">
        仕訳日 <span className="journal-entry-form__required">*</span>
      </label>
      <input
        id="journalDate"
        type="date"
        className="journal-entry-form__input"
        value={form.journalDate}
        onChange={form.handleDateChange}
        disabled={isSubmitting}
        data-testid="journal-entry-date-input"
      />
    </div>
    <div className="journal-entry-form__field">
      <label htmlFor="description" className="journal-entry-form__label">
        摘要 <span className="journal-entry-form__required">*</span>
      </label>
      <input
        id="description"
        type="text"
        className="journal-entry-form__input"
        value={form.description}
        onChange={form.handleDescriptionChange}
        disabled={isSubmitting}
        placeholder="摘要を入力"
        data-testid="journal-entry-description-input"
      />
    </div>

    <div className="journal-entry-form__table-wrapper">
      <table className="journal-entry-form__table">
        <thead>
          <tr>
            <th className="journal-entry-form__th journal-entry-form__th--number">行番号</th>
            <th className="journal-entry-form__th">勘定科目</th>
            <th className="journal-entry-form__th journal-entry-form__th--amount">借方金額</th>
            <th className="journal-entry-form__th journal-entry-form__th--amount">貸方金額</th>
            <th className="journal-entry-form__th journal-entry-form__th--action">操作</th>
          </tr>
        </thead>
        <tbody>
          {form.lines.map((line, index) => (
            <tr key={line.id}>
              <td className="journal-entry-form__td journal-entry-form__td--number">{index + 1}</td>
              <td className="journal-entry-form__td">
                <select
                  className="journal-entry-form__select"
                  value={line.accountId}
                  onChange={(e) => form.handleAccountChange(line.id, e.target.value)}
                  disabled={isSubmitting}
                  data-testid={`journal-entry-account-${line.id}`}
                >
                  <option value="">選択してください</option>
                  {accounts.map((account) => (
                    <option key={account.accountId} value={account.accountId}>
                      {account.accountCode} {account.accountName}
                    </option>
                  ))}
                </select>
              </td>
              <td className="journal-entry-form__td journal-entry-form__td--amount">
                <input
                  type="number"
                  inputMode="numeric"
                  min="0"
                  step="1"
                  className="journal-entry-form__input journal-entry-form__input--amount"
                  value={line.debitAmount}
                  onChange={(e) => form.handleAmountChange(line.id, 'debitAmount', e.target.value)}
                  disabled={isSubmitting}
                  data-testid={`journal-entry-debit-${line.id}`}
                />
              </td>
              <td className="journal-entry-form__td journal-entry-form__td--amount">
                <input
                  type="number"
                  inputMode="numeric"
                  min="0"
                  step="1"
                  className="journal-entry-form__input journal-entry-form__input--amount"
                  value={line.creditAmount}
                  onChange={(e) => form.handleAmountChange(line.id, 'creditAmount', e.target.value)}
                  disabled={isSubmitting}
                  data-testid={`journal-entry-credit-${line.id}`}
                />
              </td>
              <td className="journal-entry-form__td journal-entry-form__td--action">
                <Button
                  type="button"
                  variant="danger"
                  size="small"
                  onClick={() => form.handleRemoveLine(line.id)}
                  disabled={isSubmitting}
                  data-testid={`journal-entry-remove-${line.id}`}
                >
                  削除
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr className="journal-entry-form__totals">
            <td className="journal-entry-form__td" colSpan={2}>
              合計
            </td>
            <td className="journal-entry-form__td journal-entry-form__td--amount">
              <MoneyDisplay amount={form.totals.totalDebit} />
            </td>
            <td className="journal-entry-form__td journal-entry-form__td--amount">
              <MoneyDisplay amount={form.totals.totalCredit} />
            </td>
            <td className="journal-entry-form__td journal-entry-form__td--action">
              <span className="journal-entry-form__difference" data-testid="journal-entry-diff">
                差額: <MoneyDisplay amount={form.totals.difference} showSign colorize />
              </span>
            </td>
          </tr>
        </tfoot>
      </table>
    </div>

    <div className="journal-entry-form__actions">
      <Button
        type="button"
        variant="secondary"
        onClick={form.handleAddLine}
        disabled={isSubmitting}
        data-testid="journal-entry-add-line"
      >
        行追加
      </Button>
    </div>

    {(form.formError || error) && (
      <div className="journal-entry-form__error" data-testid="journal-entry-error">
        <ErrorMessage message={form.formError || error || ''} />
      </div>
    )}
  </>
);

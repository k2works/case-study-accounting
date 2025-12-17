# 第7章: 財務会計共通コンポーネント

本章では、財務会計システム特有の共通コンポーネントを実装します。金額入力、勘定科目選択、仕訳明細入力、期間選択など、複式簿記の概念を UI に反映したコンポーネントを解説します。

## 7.1 金額入力コンポーネント

### 7.1.1 要件

金額入力コンポーネントには以下の要件があります。

- 3桁カンマ区切りの表示
- 負数（マイナス金額）の入力対応
- decimal.js による精度保証
- フォーカス時は数値のみ表示
- ブラー時にフォーマット表示

### 7.1.2 MoneyInput コンポーネント

**src/views/common/MoneyInput.tsx**:

```typescript
import React, { useState, useRef, useCallback, useEffect } from 'react';
import Decimal from 'decimal.js';
import './MoneyInput.css';

interface Props {
  value: number | string | null;
  onChange: (value: number | null) => void;
  id?: string;
  name?: string;
  placeholder?: string;
  disabled?: boolean;
  readOnly?: boolean;
  allowNegative?: boolean;
  maxValue?: number;
  minValue?: number;
  className?: string;
  error?: boolean;
  onFocus?: () => void;
  onBlur?: () => void;
}

// 数値を3桁カンマ区切りにフォーマット
const formatNumber = (value: number | string | null): string => {
  if (value === null || value === '') return '';

  const decimal = new Decimal(value);
  const isNegative = decimal.isNegative();
  const absValue = decimal.abs().toFixed(0);
  const formatted = absValue.replace(/\B(?=(\d{3})+(?!\d))/g, ',');

  return isNegative ? `-${formatted}` : formatted;
};

// フォーマットされた文字列から数値を解析
const parseFormattedNumber = (value: string): number | null => {
  if (!value || value === '-') return null;

  // カンマを除去
  const cleaned = value.replace(/,/g, '');

  // 数値に変換
  const num = Number(cleaned);
  return isNaN(num) ? null : num;
};

export const MoneyInput: React.FC<Props> = ({
  value,
  onChange,
  id,
  name,
  placeholder = '0',
  disabled = false,
  readOnly = false,
  allowNegative = false,
  maxValue,
  minValue,
  className = '',
  error = false,
  onFocus,
  onBlur,
}) => {
  const [displayValue, setDisplayValue] = useState<string>('');
  const [isFocused, setIsFocused] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  // 外部値が変更された時に表示値を更新
  useEffect(() => {
    if (!isFocused) {
      setDisplayValue(formatNumber(value));
    }
  }, [value, isFocused]);

  // フォーカス時の処理
  const handleFocus = useCallback(
    (e: React.FocusEvent<HTMLInputElement>) => {
      setIsFocused(true);

      // カンマを除去した数値を表示
      if (value !== null && value !== '') {
        const numValue = new Decimal(value).toFixed(0);
        setDisplayValue(numValue);
      }

      // 全選択
      setTimeout(() => {
        inputRef.current?.select();
      }, 0);

      onFocus?.();
    },
    [value, onFocus]
  );

  // ブラー時の処理
  const handleBlur = useCallback(
    (e: React.FocusEvent<HTMLInputElement>) => {
      setIsFocused(false);

      const parsed = parseFormattedNumber(displayValue);

      // 範囲チェック
      let finalValue = parsed;
      if (finalValue !== null) {
        if (maxValue !== undefined && finalValue > maxValue) {
          finalValue = maxValue;
        }
        if (minValue !== undefined && finalValue < minValue) {
          finalValue = minValue;
        }
        if (!allowNegative && finalValue < 0) {
          finalValue = Math.abs(finalValue);
        }
      }

      onChange(finalValue);
      setDisplayValue(formatNumber(finalValue));

      onBlur?.();
    },
    [displayValue, onChange, maxValue, minValue, allowNegative, onBlur]
  );

  // 入力時の処理
  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const inputValue = e.target.value;

      // 許可される文字のみを残す
      let cleaned = inputValue.replace(/[^0-9,-]/g, '');

      // マイナス符号の処理
      if (!allowNegative) {
        cleaned = cleaned.replace(/-/g, '');
      } else {
        // マイナス符号は先頭のみ許可
        const hasNegative = cleaned.startsWith('-');
        cleaned = cleaned.replace(/-/g, '');
        if (hasNegative) {
          cleaned = '-' + cleaned;
        }
      }

      setDisplayValue(cleaned);
    },
    [allowNegative]
  );

  // キー入力時の処理（矢印キーで増減）
  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'ArrowUp' || e.key === 'ArrowDown') {
        e.preventDefault();

        const currentValue = parseFormattedNumber(displayValue) ?? 0;
        const step = e.shiftKey ? 100 : e.ctrlKey ? 1000 : 1;
        const newValue =
          e.key === 'ArrowUp' ? currentValue + step : currentValue - step;

        // 範囲チェック
        let finalValue = newValue;
        if (!allowNegative && finalValue < 0) {
          finalValue = 0;
        }
        if (maxValue !== undefined && finalValue > maxValue) {
          finalValue = maxValue;
        }
        if (minValue !== undefined && finalValue < minValue) {
          finalValue = minValue;
        }

        setDisplayValue(String(finalValue));
        onChange(finalValue);
      }
    },
    [displayValue, onChange, allowNegative, maxValue, minValue]
  );

  return (
    <div className={`money-input ${error ? 'has-error' : ''} ${className}`}>
      <span className="money-input__prefix">¥</span>
      <input
        ref={inputRef}
        type="text"
        inputMode="numeric"
        id={id}
        name={name}
        value={displayValue}
        onChange={handleChange}
        onFocus={handleFocus}
        onBlur={handleBlur}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        disabled={disabled}
        readOnly={readOnly}
        className="money-input__field"
        autoComplete="off"
      />
    </div>
  );
};
```

**src/views/common/MoneyInput.css**:

```css
.money-input {
  display: flex;
  align-items: center;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: white;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.money-input:focus-within {
  border-color: #1a237e;
  box-shadow: 0 0 0 3px rgba(26, 35, 126, 0.1);
}

.money-input.has-error {
  border-color: #d32f2f;
}

.money-input__prefix {
  padding: 10px 8px 10px 12px;
  color: #666;
  font-weight: 500;
  user-select: none;
}

.money-input__field {
  flex: 1;
  padding: 10px 12px 10px 0;
  border: none;
  background: transparent;
  font-size: 1rem;
  text-align: right;
  outline: none;
  font-family: 'Roboto Mono', monospace;
}

.money-input__field::placeholder {
  color: #999;
}

.money-input__field:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}
```

### 7.1.3 使用例

```typescript
import React, { useState } from 'react';
import { MoneyInput } from '@/views/common/MoneyInput';
import { FormField } from '@/views/common/FormField';

export const ExampleForm: React.FC = () => {
  const [amount, setAmount] = useState<number | null>(null);

  return (
    <FormField label="金額" htmlFor="amount" required>
      <MoneyInput
        id="amount"
        value={amount}
        onChange={setAmount}
        allowNegative={false}
        maxValue={999999999}
      />
    </FormField>
  );
};
```

---

## 7.2 勘定科目選択コンポーネント

### 7.2.1 AccountSelector コンポーネント

入力フィールドとモーダルを組み合わせた勘定科目選択コンポーネントです。

**src/views/common/AccountSelector.tsx**:

```typescript
import React, { useState, useMemo } from 'react';
import { useGetAccounts } from '@/api/generated/account/account';
import { AccountResponse } from '@/api/model';
import { AccountSelectModal } from './AccountSelectModal';
import { FiChevronDown, FiX } from 'react-icons/fi';
import './AccountSelector.css';

interface Props {
  value?: string;
  onChange: (accountCode: string | undefined, account?: AccountResponse) => void;
  placeholder?: string;
  disabled?: boolean;
  bsplFilter?: 'B' | 'P';
  error?: boolean;
  id?: string;
  name?: string;
}

export const AccountSelector: React.FC<Props> = ({
  value,
  onChange,
  placeholder = '勘定科目を選択...',
  disabled = false,
  bsplFilter,
  error = false,
  id,
  name,
}) => {
  const { data: accounts } = useGetAccounts();
  const [isModalOpen, setIsModalOpen] = useState(false);

  // 選択中の勘定科目を取得
  const selectedAccount = useMemo(() => {
    if (!value || !accounts) return null;
    return accounts.find((a) => a.accountCode === value) ?? null;
  }, [value, accounts]);

  const handleSelect = (account: AccountResponse) => {
    onChange(account.accountCode, account);
  };

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation();
    onChange(undefined);
  };

  return (
    <>
      <div
        className={`account-selector ${disabled ? 'is-disabled' : ''} ${
          error ? 'has-error' : ''
        }`}
        onClick={() => !disabled && setIsModalOpen(true)}
        role="button"
        tabIndex={disabled ? -1 : 0}
        id={id}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            !disabled && setIsModalOpen(true);
          }
        }}
      >
        <input type="hidden" name={name} value={value ?? ''} />

        {selectedAccount ? (
          <div className="account-selector__selected">
            <span className="account-selector__code">
              {selectedAccount.accountCode}
            </span>
            <span className="account-selector__name">
              {selectedAccount.accountName}
            </span>
            {!disabled && (
              <button
                type="button"
                className="account-selector__clear"
                onClick={handleClear}
                aria-label="クリア"
              >
                <FiX />
              </button>
            )}
          </div>
        ) : (
          <span className="account-selector__placeholder">{placeholder}</span>
        )}

        <FiChevronDown className="account-selector__arrow" />
      </div>

      <AccountSelectModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSelect={handleSelect}
        selectedAccountCode={value}
        bsplFilter={bsplFilter}
      />
    </>
  );
};
```

**src/views/common/AccountSelector.css**:

```css
.account-selector {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: white;
  cursor: pointer;
  transition: border-color 0.2s;
  min-height: 42px;
}

.account-selector:hover:not(.is-disabled) {
  border-color: #999;
}

.account-selector:focus {
  outline: none;
  border-color: #1a237e;
  box-shadow: 0 0 0 3px rgba(26, 35, 126, 0.1);
}

.account-selector.is-disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.account-selector.has-error {
  border-color: #d32f2f;
}

.account-selector__selected {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.account-selector__code {
  font-family: 'Roboto Mono', monospace;
  color: #666;
  font-size: 0.875rem;
}

.account-selector__name {
  flex: 1;
}

.account-selector__clear {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  border-radius: 50%;
  transition: background-color 0.2s, color 0.2s;
}

.account-selector__clear:hover {
  background-color: #f0f0f0;
  color: #333;
}

.account-selector__placeholder {
  color: #999;
  flex: 1;
}

.account-selector__arrow {
  color: #666;
  margin-left: 8px;
}
```

### 7.2.2 階層構造の構築フック

**src/hooks/useAccountTree.ts**:

```typescript
import { useMemo } from 'react';
import { AccountResponse } from '@/api/model';

export interface AccountTreeNode {
  account: AccountResponse;
  children: AccountTreeNode[];
  level: number;
}

export interface AccountTree {
  roots: AccountTreeNode[];
  flatNodes: AccountTreeNode[];
}

// 要素区分の表示順
const ELEMENT_ORDER: Record<string, number> = {
  資産: 1,
  負債: 2,
  純資産: 3,
  収益: 4,
  費用: 5,
};

export const useAccountTree = (
  accounts: AccountResponse[] | undefined,
  bsplFilter?: 'B' | 'P'
): AccountTree => {
  return useMemo(() => {
    if (!accounts) {
      return { roots: [], flatNodes: [] };
    }

    // フィルタリング
    let filtered = accounts;
    if (bsplFilter) {
      filtered = accounts.filter((a) => a.bsplType === bsplFilter);
    }

    // 表示順でソート
    const sorted = [...filtered].sort((a, b) => {
      // まず要素区分でソート
      const orderA = ELEMENT_ORDER[a.elementType] ?? 99;
      const orderB = ELEMENT_ORDER[b.elementType] ?? 99;
      if (orderA !== orderB) return orderA - orderB;

      // 次に表示順でソート
      return a.displayOrder - b.displayOrder;
    });

    // フラットノードを作成
    const flatNodes: AccountTreeNode[] = sorted.map((account) => ({
      account,
      children: [],
      level: 0,
    }));

    return {
      roots: flatNodes,
      flatNodes,
    };
  }, [accounts, bsplFilter]);
};
```

### 7.2.3 勘定科目フィルタフック

**src/hooks/useAccountFilter.ts**:

```typescript
import { useMemo, useState, useCallback } from 'react';
import { AccountResponse } from '@/api/model';

interface UseAccountFilterOptions {
  accounts: AccountResponse[] | undefined;
  initialBsplType?: 'B' | 'P' | '';
  initialElementType?: string;
}

interface UseAccountFilterResult {
  filteredAccounts: AccountResponse[];
  bsplType: 'B' | 'P' | '';
  setBsplType: (type: 'B' | 'P' | '') => void;
  elementType: string;
  setElementType: (type: string) => void;
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  clearFilters: () => void;
}

export const useAccountFilter = ({
  accounts,
  initialBsplType = '',
  initialElementType = '',
}: UseAccountFilterOptions): UseAccountFilterResult => {
  const [bsplType, setBsplType] = useState<'B' | 'P' | ''>(initialBsplType);
  const [elementType, setElementType] = useState(initialElementType);
  const [searchQuery, setSearchQuery] = useState('');

  const filteredAccounts = useMemo(() => {
    if (!accounts) return [];

    return accounts.filter((account) => {
      // BS/PL フィルタ
      if (bsplType && account.bsplType !== bsplType) {
        return false;
      }

      // 要素区分フィルタ
      if (elementType && account.elementType !== elementType) {
        return false;
      }

      // 検索クエリフィルタ
      if (searchQuery) {
        const query = searchQuery.toLowerCase();
        return (
          account.accountCode.includes(query) ||
          account.accountName.toLowerCase().includes(query) ||
          account.accountKana?.toLowerCase().includes(query)
        );
      }

      return true;
    });
  }, [accounts, bsplType, elementType, searchQuery]);

  const clearFilters = useCallback(() => {
    setBsplType('');
    setElementType('');
    setSearchQuery('');
  }, []);

  return {
    filteredAccounts,
    bsplType,
    setBsplType,
    elementType,
    setElementType,
    searchQuery,
    setSearchQuery,
    clearFilters,
  };
};
```

---

## 7.3 仕訳明細入力コンポーネント

### 7.3.1 JournalDetailInput コンポーネント

複式簿記の仕訳明細を入力するためのコンポーネントです。

**src/views/journal/JournalDetailInput.tsx**:

```typescript
import React, { useCallback } from 'react';
import { MoneyInput } from '@/views/common/MoneyInput';
import { AccountSelector } from '@/views/common/AccountSelector';
import { FiPlus, FiTrash2 } from 'react-icons/fi';
import './JournalDetailInput.css';

export interface JournalDetail {
  id: string;
  accountCode: string;
  accountName: string;
  debitAmount: number | null;
  creditAmount: number | null;
  description: string;
  taxCode?: string;
}

interface Props {
  details: JournalDetail[];
  onChange: (details: JournalDetail[]) => void;
  errors?: Record<string, Record<string, string>>;
  disabled?: boolean;
}

// 新しい明細行を生成
const createEmptyDetail = (): JournalDetail => ({
  id: crypto.randomUUID(),
  accountCode: '',
  accountName: '',
  debitAmount: null,
  creditAmount: null,
  description: '',
});

export const JournalDetailInput: React.FC<Props> = ({
  details,
  onChange,
  errors = {},
  disabled = false,
}) => {
  // 明細の追加
  const handleAdd = useCallback(() => {
    onChange([...details, createEmptyDetail()]);
  }, [details, onChange]);

  // 明細の削除
  const handleRemove = useCallback(
    (id: string) => {
      if (details.length <= 1) return;
      onChange(details.filter((d) => d.id !== id));
    },
    [details, onChange]
  );

  // 明細の更新
  const handleUpdate = useCallback(
    (id: string, field: keyof JournalDetail, value: unknown) => {
      onChange(
        details.map((d) => {
          if (d.id !== id) return d;

          const updated = { ...d, [field]: value };

          // 借方金額が入力されたら貸方をクリア、逆も同様
          if (field === 'debitAmount' && value) {
            updated.creditAmount = null;
          } else if (field === 'creditAmount' && value) {
            updated.debitAmount = null;
          }

          return updated;
        })
      );
    },
    [details, onChange]
  );

  // 勘定科目選択時の処理
  const handleAccountSelect = useCallback(
    (id: string, accountCode: string | undefined, account?: unknown) => {
      onChange(
        details.map((d) => {
          if (d.id !== id) return d;
          return {
            ...d,
            accountCode: accountCode ?? '',
            accountName: (account as { accountName?: string })?.accountName ?? '',
          };
        })
      );
    },
    [details, onChange]
  );

  return (
    <div className="journal-detail-input">
      <table className="journal-detail-input__table">
        <thead>
          <tr>
            <th className="col-account">勘定科目</th>
            <th className="col-debit">借方金額</th>
            <th className="col-credit">貸方金額</th>
            <th className="col-description">摘要</th>
            <th className="col-actions"></th>
          </tr>
        </thead>
        <tbody>
          {details.map((detail, index) => {
            const rowErrors = errors[detail.id] || {};

            return (
              <tr key={detail.id}>
                <td className="col-account">
                  <AccountSelector
                    value={detail.accountCode}
                    onChange={(code, account) =>
                      handleAccountSelect(detail.id, code, account)
                    }
                    disabled={disabled}
                    error={!!rowErrors.accountCode}
                  />
                </td>
                <td className="col-debit">
                  <MoneyInput
                    value={detail.debitAmount}
                    onChange={(value) =>
                      handleUpdate(detail.id, 'debitAmount', value)
                    }
                    disabled={disabled || !!detail.creditAmount}
                    error={!!rowErrors.debitAmount}
                  />
                </td>
                <td className="col-credit">
                  <MoneyInput
                    value={detail.creditAmount}
                    onChange={(value) =>
                      handleUpdate(detail.id, 'creditAmount', value)
                    }
                    disabled={disabled || !!detail.debitAmount}
                    error={!!rowErrors.creditAmount}
                  />
                </td>
                <td className="col-description">
                  <input
                    type="text"
                    value={detail.description}
                    onChange={(e) =>
                      handleUpdate(detail.id, 'description', e.target.value)
                    }
                    disabled={disabled}
                    placeholder="明細摘要"
                    className={rowErrors.description ? 'has-error' : ''}
                  />
                </td>
                <td className="col-actions">
                  <button
                    type="button"
                    onClick={() => handleRemove(detail.id)}
                    disabled={disabled || details.length <= 1}
                    className="journal-detail-input__remove"
                    title="削除"
                  >
                    <FiTrash2 />
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <button
        type="button"
        onClick={handleAdd}
        disabled={disabled}
        className="journal-detail-input__add"
      >
        <FiPlus />
        行を追加
      </button>
    </div>
  );
};
```

**src/views/journal/JournalDetailInput.css**:

```css
.journal-detail-input {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.journal-detail-input__table {
  width: 100%;
  border-collapse: collapse;
}

.journal-detail-input__table th {
  padding: 12px;
  background-color: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
  font-weight: 500;
  text-align: left;
}

.journal-detail-input__table td {
  padding: 8px;
  border-bottom: 1px solid #e0e0e0;
  vertical-align: top;
}

.journal-detail-input__table tr:last-child td {
  border-bottom: none;
}

/* カラム幅 */
.col-account {
  width: 30%;
}

.col-debit,
.col-credit {
  width: 20%;
}

.col-description {
  width: 25%;
}

.col-actions {
  width: 48px;
  text-align: center;
}

/* 入力フィールド */
.journal-detail-input__table td input[type='text'] {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.875rem;
}

.journal-detail-input__table td input[type='text']:focus {
  outline: none;
  border-color: #1a237e;
  box-shadow: 0 0 0 3px rgba(26, 35, 126, 0.1);
}

.journal-detail-input__table td input.has-error {
  border-color: #d32f2f;
}

/* ボタン */
.journal-detail-input__remove {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s, color 0.2s;
}

.journal-detail-input__remove:hover:not(:disabled) {
  background-color: #ffebee;
  color: #d32f2f;
}

.journal-detail-input__remove:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.journal-detail-input__add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  background-color: #f5f5f5;
  border: none;
  color: #1a237e;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.journal-detail-input__add:hover:not(:disabled) {
  background-color: #e8eaf6;
}

.journal-detail-input__add:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
```

### 7.3.2 貸借バランス表示コンポーネント

**src/views/journal/BalanceIndicator.tsx**:

```typescript
import React from 'react';
import Decimal from 'decimal.js';
import { MoneyDisplay } from '@/views/common/MoneyDisplay';
import { FiCheck, FiAlertCircle } from 'react-icons/fi';
import './BalanceIndicator.css';

interface Props {
  totalDebit: number | Decimal;
  totalCredit: number | Decimal;
}

export const BalanceIndicator: React.FC<Props> = ({
  totalDebit,
  totalCredit,
}) => {
  const debit = new Decimal(totalDebit);
  const credit = new Decimal(totalCredit);
  const difference = debit.minus(credit).abs();
  const isBalanced = difference.isZero();

  return (
    <div className={`balance-indicator ${isBalanced ? 'is-balanced' : 'is-unbalanced'}`}>
      <div className="balance-indicator__row">
        <span className="balance-indicator__label">借方合計</span>
        <span className="balance-indicator__value">
          <MoneyDisplay amount={debit.toNumber()} />
        </span>
      </div>

      <div className="balance-indicator__row">
        <span className="balance-indicator__label">貸方合計</span>
        <span className="balance-indicator__value">
          <MoneyDisplay amount={credit.toNumber()} />
        </span>
      </div>

      <div className="balance-indicator__status">
        {isBalanced ? (
          <>
            <FiCheck className="balance-indicator__icon" />
            <span>貸借一致</span>
          </>
        ) : (
          <>
            <FiAlertCircle className="balance-indicator__icon" />
            <span>
              差額: <MoneyDisplay amount={difference.toNumber()} />
            </span>
          </>
        )}
      </div>
    </div>
  );
};
```

**src/views/journal/BalanceIndicator.css**:

```css
.balance-indicator {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 12px 16px;
  border-radius: 4px;
  font-size: 0.875rem;
}

.balance-indicator.is-balanced {
  background-color: #e8f5e9;
  border: 1px solid #4caf50;
}

.balance-indicator.is-unbalanced {
  background-color: #ffebee;
  border: 1px solid #d32f2f;
}

.balance-indicator__row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.balance-indicator__label {
  color: #666;
}

.balance-indicator__value {
  font-weight: 600;
  font-family: 'Roboto Mono', monospace;
}

.balance-indicator__status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  font-weight: 500;
}

.balance-indicator.is-balanced .balance-indicator__status {
  color: #2e7d32;
}

.balance-indicator.is-unbalanced .balance-indicator__status {
  color: #c62828;
}

.balance-indicator__icon {
  font-size: 1.25rem;
}
```

### 7.3.3 仕訳入力フォームの統合例

**src/views/journal/JournalEntryForm.tsx**:

```typescript
import React from 'react';
import { FormField } from '@/views/common/FormField';
import { JournalDetailInput, JournalDetail } from './JournalDetailInput';
import { BalanceIndicator } from './BalanceIndicator';
import { useBalanceValidation } from '@/hooks/useBalanceValidation';
import { Button } from '@/views/common/Button';
import './JournalEntryForm.css';

interface Props {
  journalDate: string;
  onJournalDateChange: (date: string) => void;
  description: string;
  onDescriptionChange: (description: string) => void;
  details: JournalDetail[];
  onDetailsChange: (details: JournalDetail[]) => void;
  onSubmit: () => void;
  onCancel: () => void;
  isSubmitting: boolean;
  errors?: Record<string, string>;
  detailErrors?: Record<string, Record<string, string>>;
}

export const JournalEntryForm: React.FC<Props> = ({
  journalDate,
  onJournalDateChange,
  description,
  onDescriptionChange,
  details,
  onDetailsChange,
  onSubmit,
  onCancel,
  isSubmitting,
  errors = {},
  detailErrors = {},
}) => {
  const { totalDebit, totalCredit, isBalanced } = useBalanceValidation(details);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isBalanced) {
      onSubmit();
    }
  };

  return (
    <form className="journal-entry-form" onSubmit={handleSubmit}>
      <div className="journal-entry-form__header">
        <FormField
          label="伝票日付"
          htmlFor="journalDate"
          required
          error={errors.journalDate}
        >
          <input
            id="journalDate"
            type="date"
            value={journalDate}
            onChange={(e) => onJournalDateChange(e.target.value)}
            disabled={isSubmitting}
          />
        </FormField>

        <FormField
          label="摘要"
          htmlFor="description"
          required
          error={errors.description}
        >
          <input
            id="description"
            type="text"
            value={description}
            onChange={(e) => onDescriptionChange(e.target.value)}
            placeholder="取引内容を入力..."
            disabled={isSubmitting}
          />
        </FormField>
      </div>

      <div className="journal-entry-form__details">
        <h3>仕訳明細</h3>
        <JournalDetailInput
          details={details}
          onChange={onDetailsChange}
          errors={detailErrors}
          disabled={isSubmitting}
        />
      </div>

      <div className="journal-entry-form__balance">
        <BalanceIndicator totalDebit={totalDebit} totalCredit={totalCredit} />
      </div>

      <div className="journal-entry-form__actions">
        <Button
          type="button"
          variant="secondary"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          キャンセル
        </Button>
        <Button
          type="submit"
          variant="primary"
          disabled={!isBalanced || isSubmitting}
          loading={isSubmitting}
        >
          登録
        </Button>
      </div>
    </form>
  );
};
```

---

## 7.4 期間選択コンポーネント

### 7.4.1 PeriodSelector コンポーネント

会計期間を選択するためのコンポーネントです。

**src/views/common/PeriodSelector.tsx**:

```typescript
import React, { useState, useRef, useEffect } from 'react';
import { useAccountingPeriod } from '@/providers/AccountingPeriodProvider';
import { FiCalendar, FiChevronDown, FiChevronLeft, FiChevronRight } from 'react-icons/fi';
import './PeriodSelector.css';

export const PeriodSelector: React.FC = () => {
  const { currentPeriod, setPeriod, isMonthly, setIsMonthly } =
    useAccountingPeriod();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // ドロップダウン外クリックで閉じる
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // 年度を変更
  const handleYearChange = (delta: number) => {
    setPeriod({
      ...currentPeriod,
      year: currentPeriod.year + delta,
      startDate: `${currentPeriod.year + delta}-04-01`,
      endDate: `${currentPeriod.year + delta + 1}-03-31`,
      month: undefined,
    });
  };

  // 月を選択
  const handleMonthSelect = (month: number) => {
    const year = month >= 4 ? currentPeriod.year : currentPeriod.year + 1;
    const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month, 0).getDate();
    const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`;

    setPeriod({
      ...currentPeriod,
      month,
      startDate,
      endDate,
    });
    setIsMonthly(true);
    setIsOpen(false);
  };

  // 年度全体を選択
  const handleSelectFullYear = () => {
    setPeriod({
      ...currentPeriod,
      month: undefined,
      startDate: `${currentPeriod.year}-04-01`,
      endDate: `${currentPeriod.year + 1}-03-31`,
    });
    setIsMonthly(false);
    setIsOpen(false);
  };

  // 月の配列（4月始まり）
  const months = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3];

  const displayText = isMonthly && currentPeriod.month
    ? `${currentPeriod.year}年度 ${currentPeriod.month}月`
    : `${currentPeriod.year}年度`;

  return (
    <div className="period-selector" ref={dropdownRef}>
      <button
        className="period-selector__trigger"
        onClick={() => setIsOpen(!isOpen)}
        type="button"
      >
        <FiCalendar className="period-selector__icon" />
        <span className="period-selector__text">{displayText}</span>
        <FiChevronDown
          className={`period-selector__arrow ${isOpen ? 'is-open' : ''}`}
        />
      </button>

      {isOpen && (
        <div className="period-selector__dropdown">
          <div className="period-selector__year-nav">
            <button
              type="button"
              onClick={() => handleYearChange(-1)}
              className="period-selector__nav-button"
            >
              <FiChevronLeft />
            </button>
            <span className="period-selector__year">
              {currentPeriod.year}年度
            </span>
            <button
              type="button"
              onClick={() => handleYearChange(1)}
              className="period-selector__nav-button"
            >
              <FiChevronRight />
            </button>
          </div>

          <button
            type="button"
            className={`period-selector__full-year ${
              !isMonthly ? 'is-selected' : ''
            }`}
            onClick={handleSelectFullYear}
          >
            年度全体
          </button>

          <div className="period-selector__months">
            {months.map((month) => (
              <button
                key={month}
                type="button"
                className={`period-selector__month ${
                  isMonthly && currentPeriod.month === month ? 'is-selected' : ''
                }`}
                onClick={() => handleMonthSelect(month)}
              >
                {month}月
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
```

**src/views/common/PeriodSelector.css**:

```css
.period-selector {
  position: relative;
}

.period-selector__trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  color: white;
  cursor: pointer;
  transition: background-color 0.2s;
}

.period-selector__trigger:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.period-selector__icon {
  font-size: 1rem;
}

.period-selector__text {
  font-weight: 500;
}

.period-selector__arrow {
  font-size: 0.875rem;
  transition: transform 0.2s;
}

.period-selector__arrow.is-open {
  transform: rotate(180deg);
}

.period-selector__dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 8px;
  padding: 16px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 100;
  min-width: 280px;
}

.period-selector__year-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.period-selector__year {
  font-weight: 600;
  color: #333;
}

.period-selector__nav-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.period-selector__nav-button:hover {
  background-color: #f0f0f0;
}

.period-selector__full-year {
  width: 100%;
  padding: 10px;
  margin-bottom: 12px;
  background-color: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s, border-color 0.2s;
}

.period-selector__full-year:hover {
  background-color: #e8eaf6;
}

.period-selector__full-year.is-selected {
  background-color: #e8eaf6;
  border-color: #1a237e;
  color: #1a237e;
}

.period-selector__months {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.period-selector__month {
  padding: 10px 8px;
  background-color: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s, border-color 0.2s;
}

.period-selector__month:hover {
  background-color: #e8eaf6;
}

.period-selector__month.is-selected {
  background-color: #1a237e;
  color: white;
}
```

### 7.4.2 日付範囲選択コンポーネント

**src/views/common/FiscalDateRangePicker.tsx**:

```typescript
import React, { useMemo } from 'react';
import { useAccountingPeriod } from '@/providers/AccountingPeriodProvider';
import './FiscalDateRangePicker.css';

interface Props {
  startDate: string;
  endDate: string;
  onStartDateChange: (date: string) => void;
  onEndDateChange: (date: string) => void;
  restrictToCurrentPeriod?: boolean;
}

export const FiscalDateRangePicker: React.FC<Props> = ({
  startDate,
  endDate,
  onStartDateChange,
  onEndDateChange,
  restrictToCurrentPeriod = true,
}) => {
  const { currentPeriod } = useAccountingPeriod();

  // 会計期間に基づく制限
  const { minDate, maxDate } = useMemo(() => {
    if (restrictToCurrentPeriod) {
      return {
        minDate: currentPeriod.startDate,
        maxDate: currentPeriod.endDate,
      };
    }
    return { minDate: undefined, maxDate: undefined };
  }, [currentPeriod, restrictToCurrentPeriod]);

  // クイック選択オプション
  const quickSelections = useMemo(() => {
    const today = new Date();
    const thisMonth = today.getMonth() + 1;
    const thisYear = today.getFullYear();

    return [
      {
        label: '今月',
        start: `${thisYear}-${String(thisMonth).padStart(2, '0')}-01`,
        end: `${thisYear}-${String(thisMonth).padStart(2, '0')}-${new Date(
          thisYear,
          thisMonth,
          0
        ).getDate()}`,
      },
      {
        label: '先月',
        start: `${thisMonth === 1 ? thisYear - 1 : thisYear}-${String(
          thisMonth === 1 ? 12 : thisMonth - 1
        ).padStart(2, '0')}-01`,
        end: `${thisMonth === 1 ? thisYear - 1 : thisYear}-${String(
          thisMonth === 1 ? 12 : thisMonth - 1
        ).padStart(2, '0')}-${new Date(
          thisMonth === 1 ? thisYear - 1 : thisYear,
          thisMonth === 1 ? 12 : thisMonth - 1,
          0
        ).getDate()}`,
      },
      {
        label: '今期',
        start: currentPeriod.startDate,
        end: currentPeriod.endDate,
      },
    ];
  }, [currentPeriod]);

  const handleQuickSelect = (start: string, end: string) => {
    onStartDateChange(start);
    onEndDateChange(end);
  };

  return (
    <div className="fiscal-date-range-picker">
      <div className="fiscal-date-range-picker__quick">
        {quickSelections.map((option) => (
          <button
            key={option.label}
            type="button"
            className="fiscal-date-range-picker__quick-button"
            onClick={() => handleQuickSelect(option.start, option.end)}
          >
            {option.label}
          </button>
        ))}
      </div>

      <div className="fiscal-date-range-picker__inputs">
        <div className="fiscal-date-range-picker__field">
          <label>開始日</label>
          <input
            type="date"
            value={startDate}
            onChange={(e) => onStartDateChange(e.target.value)}
            min={minDate}
            max={endDate || maxDate}
          />
        </div>
        <span className="fiscal-date-range-picker__separator">〜</span>
        <div className="fiscal-date-range-picker__field">
          <label>終了日</label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => onEndDateChange(e.target.value)}
            min={startDate || minDate}
            max={maxDate}
          />
        </div>
      </div>
    </div>
  );
};
```

---

## 7.5 財務諸表表示コンポーネント

### 7.5.1 貸借対照表表示

**src/views/statement/BalanceSheetView.tsx**:

```typescript
import React from 'react';
import { MoneyDisplay } from '@/views/common/MoneyDisplay';
import { BalanceSheetResponse } from '@/api/model';
import './BalanceSheetView.css';

interface Props {
  data: BalanceSheetResponse;
  showComparison?: boolean;
  previousData?: BalanceSheetResponse;
}

export const BalanceSheetView: React.FC<Props> = ({
  data,
  showComparison = false,
  previousData,
}) => {
  return (
    <div className="balance-sheet-view">
      <h2 className="balance-sheet-view__title">貸借対照表</h2>
      <p className="balance-sheet-view__period">
        {data.periodStart} 〜 {data.periodEnd}
      </p>

      <div className="balance-sheet-view__container">
        {/* 借方（資産） */}
        <div className="balance-sheet-view__section">
          <h3 className="balance-sheet-view__section-title">資産の部</h3>

          <table className="balance-sheet-view__table">
            <thead>
              <tr>
                <th>勘定科目</th>
                <th className="amount">当期</th>
                {showComparison && <th className="amount">前期</th>}
                {showComparison && <th className="amount">増減</th>}
              </tr>
            </thead>
            <tbody>
              {data.assets.items.map((item) => {
                const prevItem = previousData?.assets.items.find(
                  (p) => p.accountCode === item.accountCode
                );
                const diff = prevItem
                  ? item.balance - prevItem.balance
                  : undefined;

                return (
                  <tr
                    key={item.accountCode}
                    className={item.isSubtotal ? 'is-subtotal' : ''}
                  >
                    <td
                      style={{
                        paddingLeft: `${(item.level || 0) * 20 + 12}px`,
                      }}
                    >
                      {item.accountName}
                    </td>
                    <td className="amount">
                      <MoneyDisplay amount={item.balance} />
                    </td>
                    {showComparison && (
                      <td className="amount">
                        {prevItem && (
                          <MoneyDisplay amount={prevItem.balance} />
                        )}
                      </td>
                    )}
                    {showComparison && (
                      <td className="amount">
                        {diff !== undefined && (
                          <MoneyDisplay
                            amount={diff}
                            showSign
                            colorBySign
                          />
                        )}
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
            <tfoot>
              <tr className="total-row">
                <td>資産合計</td>
                <td className="amount">
                  <MoneyDisplay amount={data.assets.total} />
                </td>
                {showComparison && (
                  <td className="amount">
                    {previousData && (
                      <MoneyDisplay amount={previousData.assets.total} />
                    )}
                  </td>
                )}
                {showComparison && (
                  <td className="amount">
                    {previousData && (
                      <MoneyDisplay
                        amount={data.assets.total - previousData.assets.total}
                        showSign
                        colorBySign
                      />
                    )}
                  </td>
                )}
              </tr>
            </tfoot>
          </table>
        </div>

        {/* 貸方（負債・純資産） */}
        <div className="balance-sheet-view__section">
          <h3 className="balance-sheet-view__section-title">負債の部</h3>
          <table className="balance-sheet-view__table">
            <thead>
              <tr>
                <th>勘定科目</th>
                <th className="amount">当期</th>
                {showComparison && <th className="amount">前期</th>}
                {showComparison && <th className="amount">増減</th>}
              </tr>
            </thead>
            <tbody>
              {data.liabilities.items.map((item) => (
                <tr
                  key={item.accountCode}
                  className={item.isSubtotal ? 'is-subtotal' : ''}
                >
                  <td
                    style={{
                      paddingLeft: `${(item.level || 0) * 20 + 12}px`,
                    }}
                  >
                    {item.accountName}
                  </td>
                  <td className="amount">
                    <MoneyDisplay amount={item.balance} />
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr className="subtotal-row">
                <td>負債合計</td>
                <td className="amount">
                  <MoneyDisplay amount={data.liabilities.total} />
                </td>
              </tr>
            </tfoot>
          </table>

          <h3 className="balance-sheet-view__section-title">純資産の部</h3>
          <table className="balance-sheet-view__table">
            <tbody>
              {data.equity.items.map((item) => (
                <tr
                  key={item.accountCode}
                  className={item.isSubtotal ? 'is-subtotal' : ''}
                >
                  <td
                    style={{
                      paddingLeft: `${(item.level || 0) * 20 + 12}px`,
                    }}
                  >
                    {item.accountName}
                  </td>
                  <td className="amount">
                    <MoneyDisplay amount={item.balance} />
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr className="subtotal-row">
                <td>純資産合計</td>
                <td className="amount">
                  <MoneyDisplay amount={data.equity.total} />
                </td>
              </tr>
              <tr className="total-row">
                <td>負債純資産合計</td>
                <td className="amount">
                  <MoneyDisplay
                    amount={data.liabilities.total + data.equity.total}
                  />
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>
  );
};
```

### 7.5.2 損益計算書表示

**src/views/statement/ProfitLossView.tsx**:

```typescript
import React from 'react';
import { MoneyDisplay } from '@/views/common/MoneyDisplay';
import { ProfitLossResponse } from '@/api/model';
import './ProfitLossView.css';

interface Props {
  data: ProfitLossResponse;
  showComparison?: boolean;
  previousData?: ProfitLossResponse;
}

export const ProfitLossView: React.FC<Props> = ({
  data,
  showComparison = false,
  previousData,
}) => {
  // 段階利益の計算
  const grossProfit = data.sales.total - data.costOfSales.total;
  const operatingProfit = grossProfit - data.sgaExpenses.total;
  const ordinaryProfit =
    operatingProfit +
    data.nonOperatingIncome.total -
    data.nonOperatingExpenses.total;
  const netIncome =
    ordinaryProfit +
    data.extraordinaryIncome.total -
    data.extraordinaryLosses.total -
    data.incomeTax;

  return (
    <div className="profit-loss-view">
      <h2 className="profit-loss-view__title">損益計算書</h2>
      <p className="profit-loss-view__period">
        {data.periodStart} 〜 {data.periodEnd}
      </p>

      <table className="profit-loss-view__table">
        <thead>
          <tr>
            <th>勘定科目</th>
            <th className="amount">金額</th>
            {showComparison && <th className="amount">前期</th>}
          </tr>
        </thead>
        <tbody>
          {/* 売上高 */}
          <tr className="section-header">
            <td colSpan={showComparison ? 3 : 2}>売上高</td>
          </tr>
          {data.sales.items.map((item) => (
            <tr key={item.accountCode}>
              <td className="indent-1">{item.accountName}</td>
              <td className="amount">
                <MoneyDisplay amount={item.balance} />
              </td>
            </tr>
          ))}
          <tr className="subtotal-row">
            <td>売上高合計</td>
            <td className="amount">
              <MoneyDisplay amount={data.sales.total} />
            </td>
          </tr>

          {/* 売上原価 */}
          <tr className="section-header">
            <td colSpan={showComparison ? 3 : 2}>売上原価</td>
          </tr>
          {data.costOfSales.items.map((item) => (
            <tr key={item.accountCode}>
              <td className="indent-1">{item.accountName}</td>
              <td className="amount">
                <MoneyDisplay amount={item.balance} />
              </td>
            </tr>
          ))}
          <tr className="subtotal-row">
            <td>売上原価合計</td>
            <td className="amount">
              <MoneyDisplay amount={data.costOfSales.total} />
            </td>
          </tr>

          {/* 売上総利益 */}
          <tr className="profit-row">
            <td>売上総利益</td>
            <td className="amount">
              <MoneyDisplay amount={grossProfit} colorBySign />
            </td>
          </tr>

          {/* 販売費及び一般管理費 */}
          <tr className="section-header">
            <td colSpan={showComparison ? 3 : 2}>販売費及び一般管理費</td>
          </tr>
          {data.sgaExpenses.items.map((item) => (
            <tr key={item.accountCode}>
              <td className="indent-1">{item.accountName}</td>
              <td className="amount">
                <MoneyDisplay amount={item.balance} />
              </td>
            </tr>
          ))}
          <tr className="subtotal-row">
            <td>販管費合計</td>
            <td className="amount">
              <MoneyDisplay amount={data.sgaExpenses.total} />
            </td>
          </tr>

          {/* 営業利益 */}
          <tr className="profit-row major">
            <td>営業利益</td>
            <td className="amount">
              <MoneyDisplay amount={operatingProfit} colorBySign />
            </td>
          </tr>

          {/* 経常利益 */}
          <tr className="profit-row major">
            <td>経常利益</td>
            <td className="amount">
              <MoneyDisplay amount={ordinaryProfit} colorBySign />
            </td>
          </tr>

          {/* 当期純利益 */}
          <tr className="profit-row final">
            <td>当期純利益</td>
            <td className="amount">
              <MoneyDisplay amount={netIncome} colorBySign />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  );
};
```

---

## 7.6 まとめ

本章では、財務会計システム特有の共通コンポーネントを実装しました。

### 重要ポイント

1. **MoneyInput**: decimal.js による精度保証、3桁カンマ表示
2. **AccountSelector**: 勘定科目の階層表示と検索機能
3. **JournalDetailInput**: 複式簿記の仕訳明細入力
4. **BalanceIndicator**: リアルタイム貸借バランス表示
5. **PeriodSelector**: 会計期間（年度/月次）の選択
6. **財務諸表表示**: 貸借対照表、損益計算書のレイアウト

### 次章の内容

第8章では、認証・ユーザー管理機能について解説します。ログイン画面、AuthProvider、ユーザー CRUD の実装を扱います。

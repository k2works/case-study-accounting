import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {
  type PatternItemBase,
  createInitialErrors,
  inputClassName,
  safeText,
  hasFormErrors,
  validateItemErrors,
  createEmptyItem,
  normalizeItems,
  sanitizePatternItems,
  validateCommonFields,
  PatternFormFields,
  PatternItemsSection,
  PatternFormShell,
  PatternItemRow,
} from './PatternFormCommon';

/* ---------- Utility Functions ---------- */

describe('createInitialErrors', () => {
  it('指定数の空エラー配列を生成する', () => {
    const errors = createInitialErrors(3);
    expect(errors.itemErrors).toHaveLength(3);
    expect(errors.itemErrors.every((e) => Object.keys(e).length === 0)).toBe(true);
  });
});

describe('inputClassName', () => {
  it('エラーなしの場合ベースクラスのみ', () => {
    expect(inputClassName('my-input', false)).toBe('my-input');
  });

  it('エラーありの場合 is-error を付与する', () => {
    expect(inputClassName('my-input', true)).toBe('my-input is-error');
  });
});

describe('safeText', () => {
  it('undefined を空文字にする', () => {
    expect(safeText(undefined)).toBe('');
  });

  it('値がある場合はそのまま返す', () => {
    expect(safeText('hello')).toBe('hello');
  });
});

describe('hasFormErrors', () => {
  it('エラーがない場合 false を返す', () => {
    expect(hasFormErrors(createInitialErrors(1))).toBe(false);
  });

  it('patternName エラーがある場合 true を返す', () => {
    const errors = { ...createInitialErrors(1), patternName: 'required' };
    expect(hasFormErrors(errors)).toBe(true);
  });

  it('itemErrors にエラーがある場合 true を返す', () => {
    const errors = createInitialErrors(1);
    errors.itemErrors[0].accountCode = 'required';
    expect(hasFormErrors(errors)).toBe(true);
  });
});

describe('validateItemErrors', () => {
  it('空の項目にエラーを設定する', () => {
    const items = [{ debitCreditType: '', accountCode: '', amountFormula: '' }];
    const errors = createInitialErrors(1);
    validateItemErrors(items, errors);

    expect(errors.itemErrors[0].debitCreditType).toBeDefined();
    expect(errors.itemErrors[0].accountCode).toBeDefined();
    expect(errors.itemErrors[0].amountFormula).toBeDefined();
  });

  it('入力済みの項目にはエラーを設定しない', () => {
    const items = [{ debitCreditType: 'D', accountCode: '1100', amountFormula: 'amount' }];
    const errors = createInitialErrors(1);
    validateItemErrors(items, errors);

    expect(errors.itemErrors[0].debitCreditType).toBeUndefined();
    expect(errors.itemErrors[0].accountCode).toBeUndefined();
    expect(errors.itemErrors[0].amountFormula).toBeUndefined();
  });
});

describe('createEmptyItem', () => {
  it('指定行番号の空アイテムを生成する', () => {
    const item = createEmptyItem(3);
    expect(item.lineNumber).toBe(3);
    expect(item.debitCreditType).toBe('');
    expect(item.accountCode).toBe('');
    expect(item.amountFormula).toBe('');
  });
});

describe('normalizeItems', () => {
  it('空配列の場合デフォルト 1 行を返す', () => {
    const result = normalizeItems([] as PatternItemBase[]);
    expect(result).toHaveLength(1);
    expect(result[0].lineNumber).toBe(1);
  });

  it('要素がある場合はそのまま返す', () => {
    const items = [createEmptyItem(1), createEmptyItem(2)];
    expect(normalizeItems(items)).toEqual(items);
  });
});

describe('sanitizePatternItems', () => {
  it('行番号を振り直し、値をトリムする', () => {
    const items = [
      {
        lineNumber: 99,
        debitCreditType: 'D',
        accountCode: ' 1100 ',
        amountFormula: ' amount ',
        descriptionTemplate: ' desc ',
      },
    ];
    const result = sanitizePatternItems(items);

    expect(result[0].lineNumber).toBe(1);
    expect(result[0].accountCode).toBe('1100');
    expect(result[0].amountFormula).toBe('amount');
    expect(result[0].descriptionTemplate).toBe('desc');
  });

  it('空の descriptionTemplate は undefined にする', () => {
    const items = [
      {
        lineNumber: 1,
        debitCreditType: 'C',
        accountCode: '4100',
        amountFormula: 'amount',
        descriptionTemplate: '  ',
      },
    ];
    const result = sanitizePatternItems(items);
    expect(result[0].descriptionTemplate).toBeUndefined();
  });
});

describe('validateCommonFields', () => {
  it('共通フィールドのバリデーションエラーを返す', () => {
    const formData = {
      patternName: '',
      sourceTableName: '',
      items: [],
    };
    const errors = validateCommonFields(formData);

    expect(errors.patternName).toBeDefined();
    expect(errors.sourceTableName).toBeDefined();
    expect(errors.items).toBeDefined();
  });

  it('正常値の場合エラーなし', () => {
    const formData = {
      patternName: '売上パターン',
      sourceTableName: 'sales',
      items: [
        {
          lineNumber: 1,
          debitCreditType: 'D',
          accountCode: '1100',
          amountFormula: 'amount',
        },
      ],
    };
    const errors = validateCommonFields(formData);
    expect(hasFormErrors(errors)).toBe(false);
  });
});

/* ---------- Components ---------- */

describe('PatternFormFields', () => {
  it('パターン名・ソーステーブル名・説明フィールドを表示する', () => {
    render(
      <PatternFormFields
        cssPrefix="test"
        formData={{ patternName: 'テスト', sourceTableName: 'tbl', description: '説明' }}
        errors={createInitialErrors(0)}
        isSubmitting={false}
        onChange={vi.fn()}
      />
    );

    expect(screen.getByTestId('pattern-name-input')).toHaveValue('テスト');
    expect(screen.getByTestId('source-table-input')).toHaveValue('tbl');
    expect(screen.getByTestId('description-input')).toHaveValue('説明');
  });
});

describe('PatternItemsSection', () => {
  it('明細行と追加ボタンを表示する', () => {
    render(
      <PatternItemsSection
        cssPrefix="test"
        items={[createEmptyItem(1)]}
        errors={createInitialErrors(1)}
        isSubmitting={false}
        keyPrefix="test-item"
        onAddItem={vi.fn()}
        onItemChange={vi.fn()}
        onRemoveItem={vi.fn()}
      />
    );

    expect(screen.getByTestId('add-item-button')).toBeInTheDocument();
    expect(screen.getByTestId('item-row-0')).toBeInTheDocument();
  });

  it('行追加ボタンのクリックでコールバックが呼ばれる', async () => {
    const user = userEvent.setup();
    const onAddItem = vi.fn();
    render(
      <PatternItemsSection
        cssPrefix="test"
        items={[createEmptyItem(1)]}
        errors={createInitialErrors(1)}
        isSubmitting={false}
        keyPrefix="test-item"
        onAddItem={onAddItem}
        onItemChange={vi.fn()}
        onRemoveItem={vi.fn()}
      />
    );

    await user.click(screen.getByTestId('add-item-button'));
    expect(onAddItem).toHaveBeenCalledTimes(1);
  });
});

describe('PatternItemRow', () => {
  it('明細行のフィールドを表示する', () => {
    const item = {
      lineNumber: 1,
      debitCreditType: 'D',
      accountCode: '1100',
      amountFormula: 'amount',
      descriptionTemplate: 'テスト',
    };
    render(
      <PatternItemRow
        index={0}
        item={item}
        error={{}}
        isSubmitting={false}
        removeDisabled={false}
        cssPrefix="test"
        onItemChange={vi.fn()}
        onRemove={vi.fn()}
      />
    );

    expect(screen.getByTestId('item-row-0')).toBeInTheDocument();
  });
});

describe('PatternFormShell', () => {
  it('フォーム・エラー・送信ボタンを含むシェルを表示する', () => {
    render(
      <PatternFormShell
        cssPrefix="test"
        config={{
          formTestId: 'test-form',
          errorTestId: 'test-error',
          submitTestId: 'test-submit',
          submitLabel: '登録',
          submittingLabel: '登録中...',
          keyPrefix: 'test-item',
        }}
        submitError="テストエラー"
        isSubmitting={false}
        onSubmit={vi.fn()}
        formData={{ patternName: 'テスト', sourceTableName: 'tbl' }}
        errors={createInitialErrors(1)}
        items={[createEmptyItem(1)]}
        onFieldChange={vi.fn()}
        onAddItem={vi.fn()}
        onItemChange={vi.fn()}
        onRemoveItem={vi.fn()}
      />
    );

    expect(screen.getByTestId('test-form')).toBeInTheDocument();
    expect(screen.getByTestId('test-error')).toBeInTheDocument();
    expect(screen.getByTestId('test-submit')).toHaveTextContent('登録');
  });

  it('送信中の場合ボタンラベルが変わる', () => {
    render(
      <PatternFormShell
        cssPrefix="test"
        config={{
          formTestId: 'test-form',
          errorTestId: 'test-error',
          submitTestId: 'test-submit',
          submitLabel: '登録',
          submittingLabel: '登録中...',
          keyPrefix: 'test-item',
        }}
        submitError={null}
        isSubmitting={true}
        onSubmit={vi.fn()}
        formData={{ patternName: '', sourceTableName: '' }}
        errors={createInitialErrors(1)}
        items={[createEmptyItem(1)]}
        onFieldChange={vi.fn()}
        onAddItem={vi.fn()}
        onItemChange={vi.fn()}
        onRemoveItem={vi.fn()}
      />
    );

    expect(screen.getByTestId('test-submit')).toHaveTextContent('登録中...');
    expect(screen.getByTestId('test-submit')).toBeDisabled();
  });

  it('beforeFields と afterFields のスロットを表示する', () => {
    render(
      <PatternFormShell
        cssPrefix="test"
        config={{
          formTestId: 'test-form',
          errorTestId: 'test-error',
          submitTestId: 'test-submit',
          submitLabel: '登録',
          submittingLabel: '登録中...',
          keyPrefix: 'test-item',
        }}
        submitError={null}
        isSubmitting={false}
        onSubmit={vi.fn()}
        formData={{ patternName: '', sourceTableName: '' }}
        errors={createInitialErrors(1)}
        items={[createEmptyItem(1)]}
        onFieldChange={vi.fn()}
        onAddItem={vi.fn()}
        onItemChange={vi.fn()}
        onRemoveItem={vi.fn()}
        beforeFields={<div data-testid="before-slot">before</div>}
        afterFields={<div data-testid="after-slot">after</div>}
      />
    );

    expect(screen.getByTestId('before-slot')).toBeInTheDocument();
    expect(screen.getByTestId('after-slot')).toBeInTheDocument();
  });
});

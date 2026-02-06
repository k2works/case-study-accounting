-- 仕訳テーブルを設計に合わせて拡張（3層構造対応）

-- 第1層: journal_entries に不足カラムを追加
ALTER TABLE journal_entries
    ADD COLUMN IF NOT EXISTS voucher_number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS input_date DATE,
    ADD COLUMN IF NOT EXISTS is_closing_entry INTEGER DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS is_single_entry INTEGER DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS voucher_type INTEGER DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS is_recurring INTEGER DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS employee_code VARCHAR(10),
    ADD COLUMN IF NOT EXISTS department_code VARCHAR(5),
    ADD COLUMN IF NOT EXISTS is_red_slip INTEGER DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS red_black_voucher_number VARCHAR(20);

-- 伝票番号のユニーク制約とインデックス追加
CREATE UNIQUE INDEX IF NOT EXISTS idx_journal_entries_voucher_number
    ON journal_entries(voucher_number)
    WHERE voucher_number IS NOT NULL;

-- コメント追加
COMMENT ON COLUMN journal_entries.voucher_number IS '仕訳伝票番号';
COMMENT ON COLUMN journal_entries.input_date IS '入力日（システムへの入力日）';
COMMENT ON COLUMN journal_entries.is_closing_entry IS '決算仕訳フラグ（0=通常仕訳、1=決算仕訳）';
COMMENT ON COLUMN journal_entries.is_single_entry IS '単振フラグ（0=複合仕訳、1=単一仕訳）';
COMMENT ON COLUMN journal_entries.voucher_type IS '仕訳伝票区分';
COMMENT ON COLUMN journal_entries.is_recurring IS '定期計上フラグ';
COMMENT ON COLUMN journal_entries.employee_code IS '社員コード';
COMMENT ON COLUMN journal_entries.department_code IS '部門コード';
COMMENT ON COLUMN journal_entries.is_red_slip IS '赤伝フラグ（0=通常、1=赤伝票）';
COMMENT ON COLUMN journal_entries.red_black_voucher_number IS '赤黒伝票番号';

-- インデックス追加
CREATE INDEX IF NOT EXISTS idx_journal_entries_is_closing ON journal_entries(is_closing_entry);
CREATE INDEX IF NOT EXISTS idx_journal_entries_is_red_slip ON journal_entries(is_red_slip);
CREATE INDEX IF NOT EXISTS idx_journal_entries_department ON journal_entries(department_code);

-- CHECK制約
ALTER TABLE journal_entries
    ADD CONSTRAINT check_is_closing_entry CHECK (is_closing_entry IN (0, 1)),
    ADD CONSTRAINT check_is_red_slip CHECK (is_red_slip IN (0, 1)),
    ADD CONSTRAINT check_red_slip_requires_voucher_number CHECK (
        (is_red_slip = 0) OR
        (is_red_slip = 1 AND red_black_voucher_number IS NOT NULL)
    );

-- 第2層: journal_entry_lines に行摘要カラムを追加
ALTER TABLE journal_entry_lines
    ADD COLUMN IF NOT EXISTS line_description VARCHAR(1000);

COMMENT ON COLUMN journal_entry_lines.line_description IS '行摘要';

-- journal_entry_lines に複合ユニーク制約を追加（journal_entry_id, line_number）
-- 注意: 外部キー参照のために先にユニーク制約を追加する必要がある
ALTER TABLE journal_entry_lines
    DROP CONSTRAINT IF EXISTS journal_entry_lines_unique_entry_line;
ALTER TABLE journal_entry_lines
    ADD CONSTRAINT journal_entry_lines_unique_entry_line
    UNIQUE (journal_entry_id, line_number);

-- 第3層: 仕訳貸借明細テーブル（借方・貸方の詳細情報）
CREATE TABLE IF NOT EXISTS journal_entry_debit_credit (
    journal_entry_id INTEGER NOT NULL,
    line_number INTEGER NOT NULL,
    debit_credit_type CHAR(1) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'JPY' NOT NULL,
    exchange_rate NUMERIC(10,4) DEFAULT 1.0000 NOT NULL,
    department_code VARCHAR(5),
    project_code VARCHAR(10),
    account_code VARCHAR(10) NOT NULL,
    sub_account_code VARCHAR(10),
    amount NUMERIC(15,2) NOT NULL,
    base_currency_amount NUMERIC(15,2) NOT NULL,
    tax_category VARCHAR(2),
    tax_rate INTEGER,
    tax_calculation_category VARCHAR(2),
    due_date DATE,
    is_cash_flow INTEGER DEFAULT 0 NOT NULL,
    segment_code VARCHAR(10),
    counter_account_code VARCHAR(10),
    counter_sub_account_code VARCHAR(10),
    tag_code VARCHAR(1),
    tag_content VARCHAR(60),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (journal_entry_id, line_number, debit_credit_type),
    FOREIGN KEY (journal_entry_id, line_number)
        REFERENCES journal_entry_lines (journal_entry_id, line_number) ON DELETE CASCADE,
    FOREIGN KEY (account_code)
        REFERENCES accounts (code)
);

-- インデックス作成
CREATE INDEX IF NOT EXISTS idx_journal_entry_debit_credit_entry
    ON journal_entry_debit_credit(journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_journal_entry_debit_credit_account
    ON journal_entry_debit_credit(account_code);
CREATE INDEX IF NOT EXISTS idx_journal_entry_debit_credit_department
    ON journal_entry_debit_credit(department_code);
CREATE INDEX IF NOT EXISTS idx_journal_entry_debit_credit_project
    ON journal_entry_debit_credit(project_code);

-- CHECK制約
ALTER TABLE journal_entry_debit_credit
    ADD CONSTRAINT check_debit_credit_type CHECK (debit_credit_type IN ('D', 'C')),
    ADD CONSTRAINT check_amount_positive CHECK (amount >= 0),
    ADD CONSTRAINT check_exchange_rate_positive CHECK (exchange_rate > 0);

-- コメント追加
COMMENT ON TABLE journal_entry_debit_credit IS '仕訳貸借明細（借方・貸方の詳細情報）';
COMMENT ON COLUMN journal_entry_debit_credit.debit_credit_type IS 'D=借方（Debit）、C=貸方（Credit）';
COMMENT ON COLUMN journal_entry_debit_credit.currency_code IS '通貨コード';
COMMENT ON COLUMN journal_entry_debit_credit.exchange_rate IS '為替レート';
COMMENT ON COLUMN journal_entry_debit_credit.department_code IS '部門コード';
COMMENT ON COLUMN journal_entry_debit_credit.project_code IS 'プロジェクトコード';
COMMENT ON COLUMN journal_entry_debit_credit.account_code IS '勘定科目コード';
COMMENT ON COLUMN journal_entry_debit_credit.sub_account_code IS '補助科目コード';
COMMENT ON COLUMN journal_entry_debit_credit.amount IS '仕訳金額';
COMMENT ON COLUMN journal_entry_debit_credit.base_currency_amount IS '基軸換算仕訳金額';
COMMENT ON COLUMN journal_entry_debit_credit.tax_category IS '消費税区分';
COMMENT ON COLUMN journal_entry_debit_credit.tax_rate IS '消費税率';
COMMENT ON COLUMN journal_entry_debit_credit.tax_calculation_category IS '消費税計算区分';
COMMENT ON COLUMN journal_entry_debit_credit.due_date IS '期日';
COMMENT ON COLUMN journal_entry_debit_credit.is_cash_flow IS '資金繰フラグ（0=影響なし、1=影響あり）';
COMMENT ON COLUMN journal_entry_debit_credit.segment_code IS 'セグメントコード';
COMMENT ON COLUMN journal_entry_debit_credit.counter_account_code IS '相手勘定科目コード';
COMMENT ON COLUMN journal_entry_debit_credit.counter_sub_account_code IS '相手補助科目コード';
COMMENT ON COLUMN journal_entry_debit_credit.tag_code IS '付箋コード';
COMMENT ON COLUMN journal_entry_debit_credit.tag_content IS '付箋内容';

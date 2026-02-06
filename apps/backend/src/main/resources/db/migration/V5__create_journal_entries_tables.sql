-- 仕訳テーブル
CREATE TABLE journal_entries (
    id SERIAL PRIMARY KEY,
    journal_date DATE NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 仕訳明細テーブル
CREATE TABLE journal_entry_lines (
    id SERIAL PRIMARY KEY,
    journal_entry_id INTEGER NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
    line_number INTEGER NOT NULL,
    account_id INTEGER NOT NULL REFERENCES accounts(id),
    debit_amount DECIMAL(15, 2) DEFAULT 0,
    credit_amount DECIMAL(15, 2) DEFAULT 0,
    CONSTRAINT chk_debit_or_credit CHECK (
        (debit_amount > 0 AND credit_amount = 0) OR
        (debit_amount = 0 AND credit_amount > 0)
    )
);

-- インデックス
CREATE INDEX idx_journal_entries_date ON journal_entries(journal_date);
CREATE INDEX idx_journal_entries_status ON journal_entries(status);
CREATE INDEX idx_journal_entry_lines_journal_entry ON journal_entry_lines(journal_entry_id);

-- コメント
COMMENT ON TABLE journal_entries IS '仕訳テーブル';
COMMENT ON COLUMN journal_entries.id IS '仕訳ID';
COMMENT ON COLUMN journal_entries.journal_date IS '仕訳日';
COMMENT ON COLUMN journal_entries.description IS '摘要';
COMMENT ON COLUMN journal_entries.status IS '仕訳ステータス（DRAFT:下書き, PENDING:承認待ち, APPROVED:承認済み, CONFIRMED:確定済み）';
COMMENT ON COLUMN journal_entries.created_by IS '作成者ID';
COMMENT ON COLUMN journal_entries.created_at IS '作成日時';
COMMENT ON COLUMN journal_entries.updated_at IS '更新日時';

COMMENT ON TABLE journal_entry_lines IS '仕訳明細テーブル';
COMMENT ON COLUMN journal_entry_lines.id IS '仕訳明細ID';
COMMENT ON COLUMN journal_entry_lines.journal_entry_id IS '仕訳ID';
COMMENT ON COLUMN journal_entry_lines.line_number IS '行番号';
COMMENT ON COLUMN journal_entry_lines.account_id IS '勘定科目ID';
COMMENT ON COLUMN journal_entry_lines.debit_amount IS '借方金額';
COMMENT ON COLUMN journal_entry_lines.credit_amount IS '貸方金額';

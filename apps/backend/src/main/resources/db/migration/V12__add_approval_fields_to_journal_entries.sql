-- 承認者と承認日時フィールドを追加
ALTER TABLE journal_entries ADD COLUMN approved_by VARCHAR(36);
ALTER TABLE journal_entries ADD COLUMN approved_at TIMESTAMP;

-- 外部キー制約
ALTER TABLE journal_entries ADD CONSTRAINT fk_journal_entries_approver FOREIGN KEY (approved_by) REFERENCES users(id);

-- インデックス
CREATE INDEX idx_journal_entries_approved_by ON journal_entries(approved_by);

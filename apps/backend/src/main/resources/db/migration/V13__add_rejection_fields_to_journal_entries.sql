-- V13: 仕訳差し戻しフィールド追加
ALTER TABLE journal_entries ADD COLUMN rejected_by VARCHAR(36);
ALTER TABLE journal_entries ADD COLUMN rejected_at TIMESTAMP;
ALTER TABLE journal_entries ADD COLUMN rejection_reason VARCHAR(500);

ALTER TABLE journal_entries ADD CONSTRAINT fk_journal_entries_rejected_by
    FOREIGN KEY (rejected_by) REFERENCES users(id);

CREATE INDEX idx_journal_entries_rejected_by ON journal_entries(rejected_by);

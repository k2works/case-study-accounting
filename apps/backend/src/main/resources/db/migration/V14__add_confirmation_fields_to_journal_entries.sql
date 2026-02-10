ALTER TABLE journal_entries ADD COLUMN confirmed_by VARCHAR(36);
ALTER TABLE journal_entries ADD COLUMN confirmed_at TIMESTAMP;
ALTER TABLE journal_entries ADD CONSTRAINT fk_journal_entries_confirmed_by
    FOREIGN KEY (confirmed_by) REFERENCES users(id);
CREATE INDEX idx_journal_entries_confirmed_by ON journal_entries(confirmed_by);

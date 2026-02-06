-- journal_entries テーブルに version カラムを追加（楽観的ロック用）
ALTER TABLE journal_entries ADD COLUMN version INTEGER NOT NULL DEFAULT 1;

COMMENT ON COLUMN journal_entries.version IS '楽観的ロック用バージョン番号';

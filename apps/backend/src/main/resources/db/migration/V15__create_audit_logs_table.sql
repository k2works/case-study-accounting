-- 監査ログテーブル
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(50),
    description TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- インデックス
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action_type ON audit_logs(action_type);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- コメント
COMMENT ON TABLE audit_logs IS '監査ログ（システム操作の履歴）';
COMMENT ON COLUMN audit_logs.user_id IS '操作ユーザーID';
COMMENT ON COLUMN audit_logs.action_type IS '操作種別（LOGIN, LOGOUT, CREATE, UPDATE, DELETE, APPROVE, REJECT, CONFIRM）';
COMMENT ON COLUMN audit_logs.entity_type IS '操作対象エンティティ種別（JOURNAL_ENTRY, ACCOUNT, USER, AUTO_JOURNAL_PATTERN）';
COMMENT ON COLUMN audit_logs.entity_id IS '操作対象エンティティID';
COMMENT ON COLUMN audit_logs.description IS '操作内容の説明';
COMMENT ON COLUMN audit_logs.ip_address IS '操作元IPアドレス';
COMMENT ON COLUMN audit_logs.created_at IS '操作日時';

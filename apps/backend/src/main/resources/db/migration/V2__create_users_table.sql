-- ユーザーテーブル
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE NOT NULL,
    failed_login_attempts INTEGER DEFAULT 0 NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- インデックス
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- コメント
COMMENT ON TABLE users IS 'ユーザーマスタ';
COMMENT ON COLUMN users.id IS 'ユーザーID';
COMMENT ON COLUMN users.email IS 'メールアドレス（ログインID）';
COMMENT ON COLUMN users.password_hash IS 'パスワードハッシュ（SHA-256 with Salt）';
COMMENT ON COLUMN users.name IS 'ユーザー名';
COMMENT ON COLUMN users.role IS 'ロール（ADMIN:管理者, MANAGER:経理責任者, STAFF:経理担当者, VIEWER:閲覧者）';
COMMENT ON COLUMN users.account_locked IS 'アカウントロック状態';
COMMENT ON COLUMN users.failed_login_attempts IS 'ログイン失敗回数';
COMMENT ON COLUMN users.last_login_at IS '最終ログイン日時';
COMMENT ON COLUMN users.created_at IS '作成日時';
COMMENT ON COLUMN users.updated_at IS '更新日時';

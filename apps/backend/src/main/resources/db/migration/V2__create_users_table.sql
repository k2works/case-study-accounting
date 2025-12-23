-- ユーザーテーブル
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    locked BOOLEAN NOT NULL DEFAULT false,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- インデックス
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(active);

-- コメント
COMMENT ON TABLE users IS 'ユーザーテーブル';
COMMENT ON COLUMN users.id IS 'ユーザーID (UUID)';
COMMENT ON COLUMN users.username IS 'ユーザー名（ログインID）';
COMMENT ON COLUMN users.email IS 'メールアドレス';
COMMENT ON COLUMN users.password IS 'パスワード（BCryptハッシュ）';
COMMENT ON COLUMN users.display_name IS '表示名';
COMMENT ON COLUMN users.role IS 'ロール（ADMIN:管理者, MANAGER:経理責任者, USER:経理担当者, VIEWER:閲覧者）';
COMMENT ON COLUMN users.active IS 'アカウント有効フラグ';
COMMENT ON COLUMN users.locked IS 'アカウントロックフラグ';
COMMENT ON COLUMN users.failed_login_attempts IS 'ログイン失敗回数';
COMMENT ON COLUMN users.last_login_at IS '最終ログイン日時';
COMMENT ON COLUMN users.created_at IS '作成日時';
COMMENT ON COLUMN users.updated_at IS '更新日時';

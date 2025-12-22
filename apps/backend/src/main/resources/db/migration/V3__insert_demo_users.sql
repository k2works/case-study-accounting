-- デモユーザーの追加
-- パスワード: password123 (実際のハッシュはアプリケーションで生成)

INSERT INTO users (email, password_hash, name, role, account_locked, failed_login_attempts, created_at, updated_at)
VALUES 
    -- 管理者ユーザー (password: admin123)
    ('admin@example.com', 'iE9AkaTHqmyN2SLRZWPxCJuvwPLY9NqeHVdvWjZ1vHpwcJZMHw==', '管理者', 'ADMIN', false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- 経理責任者 (password: manager123)
    ('manager@example.com', 'tWoBxL9oFO4hPQqPw7fJNW0kKvhRGDYrpCZnWmvXo1PkrF9FxA==', '経理責任者', 'MANAGER', false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- 経理担当者 (password: staff123)
    ('staff@example.com', 'mPvKjH3eNL8uQrSt6YfWdX1lBnMcTaDpvRzxOgKmWjF2hGE4Yw==', '経理担当者', 'STAFF', false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- 閲覧者 (password: viewer123)
    ('viewer@example.com', 'xQwEr5TyUiOpAsD9FgHjKl0ZxCvBnMqWeRtYuIoPsDfGhJkL2A==', '閲覧者', 'VIEWER', false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- テスト用ユーザーデータ
-- パスワードは全て "Password123!" (BCryptハッシュ)

INSERT INTO users (id, username, email, password, display_name, role, active, locked, failed_login_attempts)
VALUES
    -- 管理者
    ('11111111-1111-1111-1111-111111111111', 'admin', 'admin@example.com',
     '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
     '管理者', 'ADMIN', true, false, 0),

    -- 経理責任者
    ('22222222-2222-2222-2222-222222222222', 'manager', 'manager@example.com',
     '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
     '経理責任者', 'MANAGER', true, false, 0),

    -- 経理担当者
    ('33333333-3333-3333-3333-333333333333', 'user', 'user@example.com',
     '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
     '経理担当者', 'USER', true, false, 0),

    -- 閲覧者
    ('44444444-4444-4444-4444-444444444444', 'viewer', 'viewer@example.com',
     '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
     '閲覧者', 'VIEWER', true, false, 0),

    -- ロック済みユーザー（テスト用）
    ('55555555-5555-5555-5555-555555555555', 'locked', 'locked@example.com',
     '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
     'ロック済みユーザー', 'USER', true, true, 3);

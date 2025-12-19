-- PostgreSQL 初期化スクリプト
-- データベースの初期設定を行う

-- タイムゾーンの設定
SET timezone = 'Asia/Tokyo';

-- ログ出力
DO $$
BEGIN
    RAISE NOTICE 'PostgreSQL database initialized successfully';
END $$;

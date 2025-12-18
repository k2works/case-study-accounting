-- 勘定科目テーブル
CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- インデックス
CREATE INDEX idx_accounts_code ON accounts(code);
CREATE INDEX idx_accounts_account_type ON accounts(account_type);

-- コメント
COMMENT ON TABLE accounts IS '勘定科目マスタ';
COMMENT ON COLUMN accounts.id IS '勘定科目ID';
COMMENT ON COLUMN accounts.code IS '勘定科目コード';
COMMENT ON COLUMN accounts.name IS '勘定科目名';
COMMENT ON COLUMN accounts.account_type IS '勘定科目区分（ASSET:資産, LIABILITY:負債, EQUITY:純資産, REVENUE:収益, EXPENSE:費用）';
COMMENT ON COLUMN accounts.created_at IS '作成日時';
COMMENT ON COLUMN accounts.updated_at IS '更新日時';

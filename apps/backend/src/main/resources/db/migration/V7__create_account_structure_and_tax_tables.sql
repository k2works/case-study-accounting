-- 勘定科目構成マスタ（チルダ連結方式で階層構造を管理）
CREATE TABLE IF NOT EXISTS account_structures (
    account_code VARCHAR(20) PRIMARY KEY,
    account_path VARCHAR(200) NOT NULL,
    hierarchy_level INTEGER NOT NULL DEFAULT 1,
    parent_account_code VARCHAR(20),
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_account_structure_account
        FOREIGN KEY (account_code)
        REFERENCES accounts(code)
        ON DELETE CASCADE
);

COMMENT ON TABLE account_structures IS '勘定科目の階層構造を管理するマスタテーブル';
COMMENT ON COLUMN account_structures.account_code IS '勘定科目コード';
COMMENT ON COLUMN account_structures.account_path IS 'チルダ連結形式のパス（例: 11~11000~11190~11110）';
COMMENT ON COLUMN account_structures.hierarchy_level IS '階層の深さ（ルート=1）';
COMMENT ON COLUMN account_structures.parent_account_code IS '親科目のコード';
COMMENT ON COLUMN account_structures.display_order IS '同じ階層内での表示順序';

-- パスでの検索を高速化するためのインデックス
CREATE INDEX IF NOT EXISTS idx_account_structures_path
    ON account_structures (account_path);

-- 親科目での検索を高速化するためのインデックス
CREATE INDEX IF NOT EXISTS idx_account_structures_parent
    ON account_structures (parent_account_code)
    WHERE parent_account_code IS NOT NULL;

-- 課税取引マスタ
CREATE TABLE IF NOT EXISTS tax_transactions (
    tax_code VARCHAR(2) PRIMARY KEY,
    tax_name VARCHAR(20) NOT NULL,
    tax_rate DECIMAL(5, 3) NOT NULL DEFAULT 0.000,
    description VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT check_tax_rate CHECK (tax_rate >= 0 AND tax_rate <= 1)
);

COMMENT ON TABLE tax_transactions IS '消費税の課税取引区分を管理するマスタテーブル';
COMMENT ON COLUMN tax_transactions.tax_code IS '課税取引コード（01:課税、02:非課税、03:免税、04:不課税）';
COMMENT ON COLUMN tax_transactions.tax_name IS '課税取引名';
COMMENT ON COLUMN tax_transactions.tax_rate IS '適用される税率（0.10 = 10%）';
COMMENT ON COLUMN tax_transactions.description IS '課税取引の説明';
COMMENT ON COLUMN tax_transactions.is_active IS '有効な課税取引区分かどうか';

-- 課税取引マスタの初期データ投入
INSERT INTO tax_transactions (tax_code, tax_name, tax_rate, description) VALUES
    ('01', '課税', 0.10, '消費税が課税される取引'),
    ('02', '非課税', 0.000, '消費税が非課税の取引（土地の譲渡、住宅の貸付など）'),
    ('03', '免税', 0.000, '消費税が免税の取引（輸出取引など）'),
    ('04', '不課税', 0.000, '消費税の対象外の取引（給与、配当など）');

-- 外部キー制約を accounts テーブルに追加
ALTER TABLE accounts
    ADD CONSTRAINT fk_accounts_tax_transaction
    FOREIGN KEY (tax_transaction_code)
    REFERENCES tax_transactions(tax_code);

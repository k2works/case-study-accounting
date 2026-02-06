-- 勘定科目マスタに設計ドキュメントで定義されている不足カラムを追加

-- 勘定科目種別のENUM型を作成
DO $$ BEGIN
    CREATE TYPE account_type_enum AS ENUM ('資産', '負債', '純資産', '収益', '費用');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- カラム追加
ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS kana VARCHAR(40),
    ADD COLUMN IF NOT EXISTS bspl_category CHAR(1),
    ADD COLUMN IF NOT EXISTS transaction_element_category CHAR(1),
    ADD COLUMN IF NOT EXISTS expense_category CHAR(1),
    ADD COLUMN IF NOT EXISTS is_summary_account BOOLEAN DEFAULT false NOT NULL,
    ADD COLUMN IF NOT EXISTS display_order INTEGER,
    ADD COLUMN IF NOT EXISTS is_aggregation_target BOOLEAN DEFAULT true NOT NULL,
    ADD COLUMN IF NOT EXISTS balance DECIMAL(15,2) DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS tax_transaction_code VARCHAR(2);

-- コメント追加
COMMENT ON COLUMN accounts.kana IS '勘定科目カナ';
COMMENT ON COLUMN accounts.bspl_category IS 'BSPL区分（B:貸借対照表, P:損益計算書）';
COMMENT ON COLUMN accounts.transaction_element_category IS '取引要素区分（1:資産, 2:負債, 3:純資産, 4:収益, 5:費用）';
COMMENT ON COLUMN accounts.expense_category IS '費用区分（1:売上原価, 2:販売費及び一般管理費, 3:営業外費用）';
COMMENT ON COLUMN accounts.is_summary_account IS '合計科目（true: 集計科目, false: 明細科目）';
COMMENT ON COLUMN accounts.display_order IS '表示順序';
COMMENT ON COLUMN accounts.is_aggregation_target IS '集計対象';
COMMENT ON COLUMN accounts.balance IS '残高';
COMMENT ON COLUMN accounts.tax_transaction_code IS '課税取引コード';

-- インデックス追加
CREATE INDEX IF NOT EXISTS idx_accounts_bspl_category ON accounts(bspl_category);
CREATE INDEX IF NOT EXISTS idx_accounts_display_order ON accounts(display_order);

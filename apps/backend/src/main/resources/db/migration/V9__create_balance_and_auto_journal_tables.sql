-- 日次勘定科目残高テーブル
CREATE TABLE IF NOT EXISTS daily_account_balances (
    posting_date DATE NOT NULL,
    account_code VARCHAR(10) NOT NULL,
    sub_account_code VARCHAR(10) NOT NULL DEFAULT '',
    department_code VARCHAR(5) NOT NULL DEFAULT '',
    project_code VARCHAR(10) NOT NULL DEFAULT '',
    is_closing_entry INTEGER NOT NULL DEFAULT 0,
    debit_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    credit_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (
        posting_date,
        account_code,
        sub_account_code,
        department_code,
        project_code,
        is_closing_entry
    ),
    FOREIGN KEY (account_code)
        REFERENCES accounts (code)
);

-- インデックス作成
CREATE INDEX IF NOT EXISTS idx_daily_account_balances_date
    ON daily_account_balances(posting_date);
CREATE INDEX IF NOT EXISTS idx_daily_account_balances_account
    ON daily_account_balances(account_code);
CREATE INDEX IF NOT EXISTS idx_daily_account_balances_department
    ON daily_account_balances(department_code);
CREATE INDEX IF NOT EXISTS idx_daily_account_balances_project
    ON daily_account_balances(project_code);

-- CHECK制約
ALTER TABLE daily_account_balances
    ADD CONSTRAINT check_daily_debit_amount CHECK (debit_amount >= 0),
    ADD CONSTRAINT check_daily_credit_amount CHECK (credit_amount >= 0),
    ADD CONSTRAINT check_daily_is_closing_entry CHECK (is_closing_entry IN (0, 1));

-- コメント追加
COMMENT ON TABLE daily_account_balances IS '日次勘定科目残高（日ごとの借方・貸方金額を記録）';
COMMENT ON COLUMN daily_account_balances.posting_date IS '起票日（実際の取引発生日）';
COMMENT ON COLUMN daily_account_balances.sub_account_code IS '補助科目（得意先、仕入先など）';
COMMENT ON COLUMN daily_account_balances.department_code IS '部門別管理用';
COMMENT ON COLUMN daily_account_balances.project_code IS 'プロジェクト別管理用';
COMMENT ON COLUMN daily_account_balances.is_closing_entry IS '0=通常仕訳、1=決算仕訳';

-- 月次勘定科目残高テーブル
CREATE TABLE IF NOT EXISTS monthly_account_balances (
    fiscal_period INTEGER NOT NULL,
    month INTEGER NOT NULL,
    account_code VARCHAR(10) NOT NULL,
    sub_account_code VARCHAR(10) NOT NULL DEFAULT '',
    department_code VARCHAR(5) NOT NULL DEFAULT '',
    project_code VARCHAR(10) NOT NULL DEFAULT '',
    is_closing_entry INTEGER NOT NULL DEFAULT 0,
    opening_balance NUMERIC(15,2) NOT NULL DEFAULT 0,
    debit_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    credit_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    closing_balance NUMERIC(15,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (
        fiscal_period,
        month,
        account_code,
        sub_account_code,
        department_code,
        project_code,
        is_closing_entry
    ),
    FOREIGN KEY (account_code)
        REFERENCES accounts (code)
);

-- インデックス作成
CREATE INDEX IF NOT EXISTS idx_monthly_account_balances_period_month
    ON monthly_account_balances(fiscal_period, month);
CREATE INDEX IF NOT EXISTS idx_monthly_account_balances_account
    ON monthly_account_balances(account_code);

-- CHECK制約
ALTER TABLE monthly_account_balances
    ADD CONSTRAINT check_month_range CHECK (month >= 1 AND month <= 12);

-- コメント追加
COMMENT ON TABLE monthly_account_balances IS '月次勘定科目残高（月ごとの月初残高・借方・貸方金額・月末残高を記録）';
COMMENT ON COLUMN monthly_account_balances.fiscal_period IS '会計年度（例：2025）';
COMMENT ON COLUMN monthly_account_balances.month IS '月度（1〜12）';
COMMENT ON COLUMN monthly_account_balances.opening_balance IS '月初時点の残高';
COMMENT ON COLUMN monthly_account_balances.closing_balance IS '月末時点の残高（月初残高 + 借方金額 - 貸方金額）';

-- 自動仕訳管理テーブル
CREATE TABLE IF NOT EXISTS auto_journal_management (
    id BIGSERIAL PRIMARY KEY,
    source_table_name VARCHAR(100) NOT NULL UNIQUE,
    last_processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE auto_journal_management IS '自動仕訳の処理状況を管理';
COMMENT ON COLUMN auto_journal_management.source_table_name IS '処理対象のソーステーブル名';
COMMENT ON COLUMN auto_journal_management.last_processed_at IS '最終処理日時';

-- 自動仕訳パターンテーブル
CREATE TABLE IF NOT EXISTS auto_journal_patterns (
    id BIGSERIAL PRIMARY KEY,
    pattern_code VARCHAR(20) NOT NULL UNIQUE,
    pattern_name VARCHAR(100) NOT NULL,
    source_table_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE auto_journal_patterns IS '自動仕訳の生成パターン定義';

-- 自動仕訳パターン明細テーブル
CREATE TABLE IF NOT EXISTS auto_journal_pattern_items (
    id BIGSERIAL PRIMARY KEY,
    pattern_id BIGINT NOT NULL,
    line_number INTEGER NOT NULL,
    debit_credit_type CHAR(1) NOT NULL,
    account_code VARCHAR(10) NOT NULL,
    amount_formula VARCHAR(200) NOT NULL,
    description_template VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (pattern_id) REFERENCES auto_journal_patterns (id) ON DELETE CASCADE,
    FOREIGN KEY (account_code) REFERENCES accounts (code),
    CONSTRAINT check_pattern_item_dc_type CHECK (debit_credit_type IN ('D', 'C'))
);

COMMENT ON TABLE auto_journal_pattern_items IS '自動仕訳パターンの借方・貸方明細';
COMMENT ON COLUMN auto_journal_pattern_items.amount_formula IS '金額計算式（例: 売上金額 * 1.10）';
COMMENT ON COLUMN auto_journal_pattern_items.debit_credit_type IS 'D=借方、C=貸方';

-- 自動仕訳実行ログテーブル
CREATE TABLE IF NOT EXISTS auto_journal_logs (
    id BIGSERIAL PRIMARY KEY,
    pattern_id BIGINT NOT NULL,
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_count INTEGER NOT NULL DEFAULT 0,
    generated_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    message VARCHAR(500),
    error_detail TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (pattern_id) REFERENCES auto_journal_patterns (id),
    CONSTRAINT check_log_status CHECK (status IN ('SUCCESS', 'PARTIAL', 'FAILED'))
);

COMMENT ON TABLE auto_journal_logs IS '自動仕訳の実行履歴';
COMMENT ON COLUMN auto_journal_logs.status IS 'SUCCESS=成功, PARTIAL=一部成功, FAILED=失敗';

-- インデックス作成
CREATE INDEX IF NOT EXISTS idx_auto_journal_patterns_source
    ON auto_journal_patterns(source_table_name);
CREATE INDEX IF NOT EXISTS idx_auto_journal_logs_pattern
    ON auto_journal_logs(pattern_id);
CREATE INDEX IF NOT EXISTS idx_auto_journal_logs_executed_at
    ON auto_journal_logs(executed_at);

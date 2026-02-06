-- 総勘定元帳ビュー
CREATE OR REPLACE VIEW general_ledger AS
SELECT
    d.posting_date,
    a.code AS account_code,
    a.name AS account_name,
    a.bspl_category,
    d.sub_account_code,
    d.department_code,
    d.project_code,
    d.debit_amount,
    d.credit_amount,
    -- ウィンドウ関数で累積残高を計算
    SUM(d.debit_amount - d.credit_amount) OVER (
        PARTITION BY d.account_code, d.sub_account_code, d.department_code, d.project_code
        ORDER BY d.posting_date
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS balance
FROM daily_account_balances d
INNER JOIN accounts a ON d.account_code = a.code
WHERE d.is_closing_entry = 0  -- 通常仕訳のみ
ORDER BY d.account_code, d.posting_date;

COMMENT ON VIEW general_ledger IS '総勘定元帳ビュー';

-- 試算表ビュー
CREATE OR REPLACE VIEW trial_balance AS
SELECT
    a.code AS account_code,
    a.name AS account_name,
    a.bspl_category,
    COALESCE(SUM(d.debit_amount), 0) AS total_debit,
    COALESCE(SUM(d.credit_amount), 0) AS total_credit,
    COALESCE(SUM(d.debit_amount), 0) - COALESCE(SUM(d.credit_amount), 0) AS balance
FROM accounts a
LEFT JOIN daily_account_balances d
    ON a.code = d.account_code
    AND d.is_closing_entry = 0  -- 通常仕訳のみ
GROUP BY a.code, a.name, a.bspl_category
ORDER BY a.code;

COMMENT ON VIEW trial_balance IS '試算表ビュー';

-- 貸借対照表ビュー（BS: Balance Sheet）
CREATE OR REPLACE VIEW balance_sheet AS
SELECT
    a.code AS account_code,
    a.name AS account_name,
    a.account_type,
    COALESCE(SUM(d.debit_amount), 0) - COALESCE(SUM(d.credit_amount), 0) AS balance
FROM accounts a
LEFT JOIN daily_account_balances d
    ON a.code = d.account_code
    AND d.is_closing_entry = 0
WHERE a.bspl_category = 'B'  -- 貸借対照表項目のみ
GROUP BY a.code, a.name, a.account_type
ORDER BY a.code;

COMMENT ON VIEW balance_sheet IS '貸借対照表ビュー';

-- 損益計算書ビュー（PL: Profit and Loss）
CREATE OR REPLACE VIEW profit_and_loss AS
SELECT
    a.code AS account_code,
    a.name AS account_name,
    a.account_type,
    COALESCE(SUM(d.debit_amount), 0) AS total_debit,
    COALESCE(SUM(d.credit_amount), 0) AS total_credit,
    CASE
        WHEN a.account_type IN ('EXPENSE', '費用') THEN
            COALESCE(SUM(d.debit_amount), 0) - COALESCE(SUM(d.credit_amount), 0)
        WHEN a.account_type IN ('REVENUE', '収益') THEN
            COALESCE(SUM(d.credit_amount), 0) - COALESCE(SUM(d.debit_amount), 0)
        ELSE 0
    END AS amount
FROM accounts a
LEFT JOIN daily_account_balances d
    ON a.code = d.account_code
    AND d.is_closing_entry = 0
WHERE a.bspl_category = 'P'  -- 損益計算書項目のみ
GROUP BY a.code, a.name, a.account_type
ORDER BY a.code;

COMMENT ON VIEW profit_and_loss IS '損益計算書ビュー';

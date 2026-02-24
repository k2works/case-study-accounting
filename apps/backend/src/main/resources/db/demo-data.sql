-- Demo Data for Accounting System
-- 勘定科目マスタ（デモ用初期データ）

-- 資産勘定
INSERT INTO accounts (code, name, account_type) VALUES ('111', '現金', 'ASSET');
INSERT INTO accounts (code, name, account_type) VALUES ('112', '当座預金', 'ASSET');
INSERT INTO accounts (code, name, account_type) VALUES ('113', '普通預金', 'ASSET');
INSERT INTO accounts (code, name, account_type) VALUES ('131', '売掛金', 'ASSET');
INSERT INTO accounts (code, name, account_type) VALUES ('141', '商品', 'ASSET');
INSERT INTO accounts (code, name, account_type) VALUES ('151', '前払金', 'ASSET');
INSERT INTO accounts (code, name, account_type) VALUES ('161', '建物', 'ASSET');
INSERT INTO accounts (code, name, account_type) VALUES ('162', '備品', 'ASSET');
INSERT INTO accounts (code, name, account_type) VALUES ('163', '車両運搬具', 'ASSET');
INSERT INTO accounts (code, name, account_type) VALUES ('164', '土地', 'ASSET');

-- 負債勘定
INSERT INTO accounts (code, name, account_type) VALUES ('211', '買掛金', 'LIABILITY');
INSERT INTO accounts (code, name, account_type) VALUES ('212', '未払金', 'LIABILITY');
INSERT INTO accounts (code, name, account_type) VALUES ('213', '前受金', 'LIABILITY');
INSERT INTO accounts (code, name, account_type) VALUES ('221', '短期借入金', 'LIABILITY');
INSERT INTO accounts (code, name, account_type) VALUES ('231', '長期借入金', 'LIABILITY');

-- 純資産勘定
INSERT INTO accounts (code, name, account_type) VALUES ('311', '資本金', 'EQUITY');
INSERT INTO accounts (code, name, account_type) VALUES ('321', '利益剰余金', 'EQUITY');

-- 収益勘定
INSERT INTO accounts (code, name, account_type) VALUES ('411', '売上高', 'REVENUE');
INSERT INTO accounts (code, name, account_type) VALUES ('421', '受取利息', 'REVENUE');
INSERT INTO accounts (code, name, account_type) VALUES ('431', '雑収入', 'REVENUE');

-- 費用勘定
INSERT INTO accounts (code, name, account_type) VALUES ('511', '仕入高', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('521', '給与手当', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('522', '法定福利費', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('531', '旅費交通費', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('532', '通信費', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('533', '消耗品費', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('534', '水道光熱費', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('535', '地代家賃', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('541', '減価償却費', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('551', '支払利息', 'EXPENSE');
INSERT INTO accounts (code, name, account_type) VALUES ('561', '雑損失', 'EXPENSE');

-- テスト用ユーザーデータ
-- パスワードは全て "Password123!" (BCryptハッシュ)

-- 管理者
INSERT INTO users (id, username, email, password, display_name, role, active, locked, failed_login_attempts)
VALUES ('11111111-1111-1111-1111-111111111111', 'admin', 'admin@example.com',
        '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
        '管理者', 'ADMIN', true, false, 0);

-- 経理責任者
INSERT INTO users (id, username, email, password, display_name, role, active, locked, failed_login_attempts)
VALUES ('22222222-2222-2222-2222-222222222222', 'manager', 'manager@example.com',
        '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
        '経理責任者', 'MANAGER', true, false, 0);

-- 経理担当者
INSERT INTO users (id, username, email, password, display_name, role, active, locked, failed_login_attempts)
VALUES ('33333333-3333-3333-3333-333333333333', 'user', 'user@example.com',
        '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
        '経理担当者', 'USER', true, false, 0);

-- 閲覧者
INSERT INTO users (id, username, email, password, display_name, role, active, locked, failed_login_attempts)
VALUES ('44444444-4444-4444-4444-444444444444', 'viewer', 'viewer@example.com',
        '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
        '閲覧者', 'VIEWER', true, false, 0);

-- ロック済みユーザー（テスト用）
INSERT INTO users (id, username, email, password, display_name, role, active, locked, failed_login_attempts)
VALUES ('55555555-5555-5555-5555-555555555555', 'locked', 'locked@example.com',
        '$2a$10$EiuiSA3w15Y5qSkj8U.pJ.WO8zPtMknpujS0I7w9hYaFba9Z2nwqK',
        'ロック済みユーザー', 'USER', true, true, 3);

-- ============================================
-- bspl_category 設定（試算表・BS・PL 表示用）
-- ============================================
UPDATE accounts SET bspl_category = 'B' WHERE account_type IN ('ASSET', 'LIABILITY', 'EQUITY');
UPDATE accounts SET bspl_category = 'P' WHERE account_type IN ('REVENUE', 'EXPENSE');

-- ============================================
-- 仕訳データ（5件）
-- ============================================
-- #1 DRAFT: 現金売上
INSERT INTO journal_entries (id, journal_date, description, status, created_by, voucher_number, version)
VALUES (1, DATE '2024-04-01', '現金売上', 'DRAFT', '11111111-1111-1111-1111-111111111111', 'V-202404-001', 1);
-- #2 DRAFT: 仕入支払
INSERT INTO journal_entries (id, journal_date, description, status, created_by, voucher_number, version)
VALUES (2, DATE '2024-04-05', '仕入支払', 'DRAFT', '11111111-1111-1111-1111-111111111111', 'V-202404-002', 1);
-- #3 APPROVED: 経費精算
INSERT INTO journal_entries (id, journal_date, description, status, created_by, voucher_number, version, approved_by, approved_at)
VALUES (3, DATE '2024-04-10', '経費精算', 'APPROVED', '11111111-1111-1111-1111-111111111111', 'V-202404-003', 1,
        '22222222-2222-2222-2222-222222222222', TIMESTAMP '2024-04-10 10:00:00');
-- #4 CONFIRMED: 給与支払
INSERT INTO journal_entries (id, journal_date, description, status, created_by, voucher_number, version, approved_by, approved_at, confirmed_by, confirmed_at)
VALUES (4, DATE '2024-04-15', '給与支払', 'CONFIRMED', '11111111-1111-1111-1111-111111111111', 'V-202404-004', 1,
        '22222222-2222-2222-2222-222222222222', TIMESTAMP '2024-04-15 10:00:00',
        '22222222-2222-2222-2222-222222222222', TIMESTAMP '2024-04-15 11:00:00');
-- #5 PENDING: 備品購入
INSERT INTO journal_entries (id, journal_date, description, status, created_by, voucher_number, version)
VALUES (5, DATE '2024-04-20', '備品購入', 'PENDING', '11111111-1111-1111-1111-111111111111', 'V-202404-005', 1);

-- Identity カウンタリセット（後続の自動採番と衝突しないように）
ALTER TABLE journal_entries ALTER COLUMN id RESTART WITH 100;

-- ============================================
-- 仕訳明細データ
-- ============================================
-- Entry 1: 現金売上 (借方: 現金 10,000 / 貸方: 売上高 10,000)
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (1, 1, (SELECT id FROM accounts WHERE code = '111'), 10000.00, 0.00);
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (1, 2, (SELECT id FROM accounts WHERE code = '411'), 0.00, 10000.00);

-- Entry 2: 仕入支払 (借方: 仕入高 5,000 / 貸方: 現金 5,000)
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (2, 1, (SELECT id FROM accounts WHERE code = '511'), 5000.00, 0.00);
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (2, 2, (SELECT id FROM accounts WHERE code = '111'), 0.00, 5000.00);

-- Entry 3: 経費精算 (借方: 旅費交通費 3,000 / 貸方: 現金 3,000)
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (3, 1, (SELECT id FROM accounts WHERE code = '531'), 3000.00, 0.00);
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (3, 2, (SELECT id FROM accounts WHERE code = '111'), 0.00, 3000.00);

-- Entry 4: 給与支払 (借方: 給与手当 200,000 / 貸方: 普通預金 200,000)
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (4, 1, (SELECT id FROM accounts WHERE code = '521'), 200000.00, 0.00);
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (4, 2, (SELECT id FROM accounts WHERE code = '113'), 0.00, 200000.00);

-- Entry 5: 備品購入 (借方: 備品 50,000 / 貸方: 買掛金 50,000)
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (5, 1, (SELECT id FROM accounts WHERE code = '162'), 50000.00, 0.00);
INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit_amount, credit_amount)
VALUES (5, 2, (SELECT id FROM accounts WHERE code = '211'), 0.00, 50000.00);

-- ============================================
-- 仕訳貸借明細データ
-- ============================================
-- Entry 1: 現金売上
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (1, 1, 'D', '111', 10000.00, 10000.00);
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (1, 2, 'C', '411', 10000.00, 10000.00);

-- Entry 2: 仕入支払
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (2, 1, 'D', '511', 5000.00, 5000.00);
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (2, 2, 'C', '111', 5000.00, 5000.00);

-- Entry 3: 経費精算
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (3, 1, 'D', '531', 3000.00, 3000.00);
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (3, 2, 'C', '111', 3000.00, 3000.00);

-- Entry 4: 給与支払
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (4, 1, 'D', '521', 200000.00, 200000.00);
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (4, 2, 'C', '113', 200000.00, 200000.00);

-- Entry 5: 備品購入
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (5, 1, 'D', '162', 50000.00, 50000.00);
INSERT INTO journal_entry_debit_credit (journal_entry_id, line_number, debit_credit_type, account_code, amount, base_currency_amount)
VALUES (5, 2, 'C', '211', 50000.00, 50000.00);

-- ============================================
-- 自動仕訳パターン（2件）
-- ============================================
INSERT INTO auto_journal_patterns (id, pattern_code, pattern_name, source_table_name, description, is_active)
VALUES (1, 'PAT001', '売上仕訳', 'sales', '売上取引から自動仕訳を生成', true);
INSERT INTO auto_journal_patterns (id, pattern_code, pattern_name, source_table_name, description, is_active)
VALUES (2, 'PAT002', '仕入仕訳', 'purchases', '仕入取引から自動仕訳を生成', true);

ALTER TABLE auto_journal_patterns ALTER COLUMN id RESTART WITH 100;

-- パターン明細
-- PAT001: 借方 売掛金(131) / 貸方 売上高(411)
INSERT INTO auto_journal_pattern_items (id, pattern_id, line_number, debit_credit_type, account_code, amount_formula, description_template)
VALUES (1, 1, 1, 'D', '131', 'amount', '売上（売掛金）');
INSERT INTO auto_journal_pattern_items (id, pattern_id, line_number, debit_credit_type, account_code, amount_formula, description_template)
VALUES (2, 1, 2, 'C', '411', 'amount', '売上高計上');
-- PAT002: 借方 仕入高(511) / 貸方 買掛金(211)
INSERT INTO auto_journal_pattern_items (id, pattern_id, line_number, debit_credit_type, account_code, amount_formula, description_template)
VALUES (3, 2, 1, 'D', '511', 'amount', '仕入（仕入高）');
INSERT INTO auto_journal_pattern_items (id, pattern_id, line_number, debit_credit_type, account_code, amount_formula, description_template)
VALUES (4, 2, 2, 'C', '211', 'amount', '買掛金計上');

ALTER TABLE auto_journal_pattern_items ALTER COLUMN id RESTART WITH 100;

-- ============================================
-- 監査ログ（5件）
-- ============================================
INSERT INTO audit_logs (user_id, action_type, entity_type, entity_id, description, ip_address, created_at)
VALUES ('admin', 'LOGIN', NULL, NULL, '管理者ログイン', '192.168.1.1', TIMESTAMP '2024-04-01 08:00:00');
INSERT INTO audit_logs (user_id, action_type, entity_type, entity_id, description, ip_address, created_at)
VALUES ('admin', 'CREATE', 'JOURNAL_ENTRY', '1', '仕訳伝票作成: 現金売上', '192.168.1.1', TIMESTAMP '2024-04-01 09:00:00');
INSERT INTO audit_logs (user_id, action_type, entity_type, entity_id, description, ip_address, created_at)
VALUES ('manager', 'APPROVE', 'JOURNAL_ENTRY', '3', '仕訳伝票承認: 経費精算', '192.168.1.2', TIMESTAMP '2024-04-10 10:00:00');
INSERT INTO audit_logs (user_id, action_type, entity_type, entity_id, description, ip_address, created_at)
VALUES ('admin', 'LOGOUT', NULL, NULL, '管理者ログアウト', '192.168.1.1', TIMESTAMP '2024-04-10 18:00:00');
INSERT INTO audit_logs (user_id, action_type, entity_type, entity_id, description, ip_address, created_at)
VALUES ('user', 'LOGIN', NULL, NULL, '経理担当者ログイン', '192.168.1.3', TIMESTAMP '2024-04-11 08:30:00');

-- ============================================
-- 日次残高（仕訳関連勘定科目）
-- ============================================
-- 現金(111): 5日分
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-01', '111', '', '', '', 0, 10000.00, 0.00);
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-05', '111', '', '', '', 0, 0.00, 5000.00);
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-10', '111', '', '', '', 0, 0.00, 3000.00);
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-12', '111', '', '', '', 0, 15000.00, 0.00);
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-18', '111', '', '', '', 0, 0.00, 8000.00);
-- 売上高(411): 収益
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-01', '411', '', '', '', 0, 0.00, 10000.00);
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-10', '411', '', '', '', 0, 0.00, 25000.00);
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-20', '411', '', '', '', 0, 0.00, 30000.00);
-- 仕入高(511): 費用
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-05', '511', '', '', '', 0, 5000.00, 0.00);
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-15', '511', '', '', '', 0, 12000.00, 0.00);
-- 旅費交通費(531): 費用
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-10', '531', '', '', '', 0, 3000.00, 0.00);
-- 給与手当(521): 費用
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-15', '521', '', '', '', 0, 200000.00, 0.00);
-- 普通預金(113): 資産
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-01', '113', '', '', '', 0, 500000.00, 0.00);
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-15', '113', '', '', '', 0, 0.00, 200000.00);
-- 備品(162): 資産
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-20', '162', '', '', '', 0, 50000.00, 0.00);
-- 買掛金(211): 負債
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-20', '211', '', '', '', 0, 0.00, 50000.00);
-- 資本金(311): 純資産（初期出資）
INSERT INTO daily_account_balances (posting_date, account_code, sub_account_code, department_code, project_code, is_closing_entry, debit_amount, credit_amount)
VALUES (DATE '2024-04-01', '311', '', '', '', 0, 0.00, 1000000.00);

-- ============================================
-- 月次残高（現金 111 + 売上高 411、12か月分）
-- ============================================
-- 現金(111): 資産勘定（借方残高）
INSERT INTO monthly_account_balances (fiscal_period, "month", account_code, sub_account_code, department_code, project_code, is_closing_entry, opening_balance, debit_amount, credit_amount, closing_balance)
VALUES
(2024, 1, '111', '', '', '', 0, 1000000.00, 500000.00, 300000.00, 1200000.00),
(2024, 2, '111', '', '', '', 0, 1200000.00, 450000.00, 350000.00, 1300000.00),
(2024, 3, '111', '', '', '', 0, 1300000.00, 600000.00, 400000.00, 1500000.00),
(2024, 4, '111', '', '', '', 0, 1500000.00, 480000.00, 380000.00, 1600000.00),
(2024, 5, '111', '', '', '', 0, 1600000.00, 520000.00, 420000.00, 1700000.00),
(2024, 6, '111', '', '', '', 0, 1700000.00, 550000.00, 450000.00, 1800000.00),
(2024, 7, '111', '', '', '', 0, 1800000.00, 500000.00, 400000.00, 1900000.00),
(2024, 8, '111', '', '', '', 0, 1900000.00, 600000.00, 500000.00, 2000000.00),
(2024, 9, '111', '', '', '', 0, 2000000.00, 550000.00, 450000.00, 2100000.00),
(2024, 10, '111', '', '', '', 0, 2100000.00, 500000.00, 400000.00, 2200000.00),
(2024, 11, '111', '', '', '', 0, 2200000.00, 600000.00, 500000.00, 2300000.00),
(2024, 12, '111', '', '', '', 0, 2300000.00, 700000.00, 600000.00, 2400000.00);

-- 売上高(411): 収益勘定（貸方残高）
INSERT INTO monthly_account_balances (fiscal_period, "month", account_code, sub_account_code, department_code, project_code, is_closing_entry, opening_balance, debit_amount, credit_amount, closing_balance)
VALUES
(2024, 1, '411', '', '', '', 0, 0.00, 0.00, 500000.00, 500000.00),
(2024, 2, '411', '', '', '', 0, 500000.00, 0.00, 450000.00, 950000.00),
(2024, 3, '411', '', '', '', 0, 950000.00, 0.00, 600000.00, 1550000.00),
(2024, 4, '411', '', '', '', 0, 1550000.00, 0.00, 480000.00, 2030000.00),
(2024, 5, '411', '', '', '', 0, 2030000.00, 0.00, 520000.00, 2550000.00),
(2024, 6, '411', '', '', '', 0, 2550000.00, 0.00, 550000.00, 3100000.00),
(2024, 7, '411', '', '', '', 0, 3100000.00, 0.00, 500000.00, 3600000.00),
(2024, 8, '411', '', '', '', 0, 3600000.00, 0.00, 600000.00, 4200000.00),
(2024, 9, '411', '', '', '', 0, 4200000.00, 0.00, 550000.00, 4750000.00),
(2024, 10, '411', '', '', '', 0, 4750000.00, 0.00, 500000.00, 5250000.00),
(2024, 11, '411', '', '', '', 0, 5250000.00, 0.00, 600000.00, 5850000.00),
(2024, 12, '411', '', '', '', 0, 5850000.00, 0.00, 700000.00, 6550000.00);

-- ============================================
-- 勘定科目構成（親子関係）
-- ============================================
INSERT INTO account_structures (account_code, account_path, hierarchy_level, parent_account_code, display_order)
VALUES ('111', '111', 1, NULL, 1);
INSERT INTO account_structures (account_code, account_path, hierarchy_level, parent_account_code, display_order)
VALUES ('112', '111~112', 2, '111', 2);
INSERT INTO account_structures (account_code, account_path, hierarchy_level, parent_account_code, display_order)
VALUES ('113', '111~113', 2, '111', 3);

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

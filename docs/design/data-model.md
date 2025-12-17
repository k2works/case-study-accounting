# データモデル設計

## 概要

本ドキュメントは、財務会計システムのデータモデル設計を定義します。テスト駆動開発（TDD）のアプローチに基づき、段階的にデータモデルを構築・改善していく方針を採用しています。

### 設計原則

1. **TDD データベース設計**: 要求をテストコードで表現し、最小限のスキーマから始めて段階的に改善
2. **複式簿記の原理**: 「借方合計 = 貸方合計」をデータベーススキーマで保証
3. **チルダ連結方式**: 勘定科目の階層構造を効率的に管理
4. **3 層構造仕訳**: 仕訳ヘッダー、仕訳明細、仕訳貸借明細の 3 層で複合仕訳に対応
5. **残高即時更新**: UPSERT による効率的な残高管理

---

## データモデル全体像

### ER 図

```plantuml
@startuml
title 財務会計システム - データモデル全体像

!define MASTER_COLOR #E8F5E9
!define TRANSACTION_COLOR #E3F2FD
!define BALANCE_COLOR #FFF3E0
!define USER_COLOR #F3E5F5
!define AUDIT_COLOR #ECEFF1

' マスタデータ
package "マスタデータ" MASTER_COLOR {
  entity "勘定科目マスタ" as Account {
    * **勘定科目ID**: SERIAL <<PK>>
    --
    * **勘定科目コード**: VARCHAR(20) <<UK>>
    * **勘定科目名**: VARCHAR(100)
    * **勘定科目種別**: ENUM
    o **勘定科目カナ**: VARCHAR(40)
    o **BSPL区分**: CHAR(1)
    o **取引要素区分**: CHAR(1)
    o **費用区分**: CHAR(1)
    * **合計科目**: BOOLEAN
    o **表示順序**: INTEGER
    * **集計対象**: BOOLEAN
    * **残高**: DECIMAL(15,2)
    o **課税取引コード**: VARCHAR(2) <<FK>>
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "勘定科目構成マスタ" as AccountStructure {
    * **勘定科目コード**: VARCHAR(20) <<PK,FK>>
    --
    * **勘定科目パス**: VARCHAR(200)
    * **階層レベル**: INTEGER
    o **親科目コード**: VARCHAR(20)
    * **表示順序**: INTEGER
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "課税取引マスタ" as TaxTransaction {
    * **課税取引コード**: VARCHAR(2) <<PK>>
    --
    * **課税取引名**: VARCHAR(20)
    * **税率**: DECIMAL(5,3)
    o **説明**: VARCHAR(200)
    * **有効フラグ**: BOOLEAN
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "会計期間マスタ" as AccountingPeriod {
    * **会計期間ID**: SERIAL <<PK>>
    --
    * **会計年度**: INTEGER
    * **期間名**: VARCHAR(50)
    * **開始日**: DATE
    * **終了日**: DATE
    * **締め状態**: VARCHAR(20)
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }
}

' トランザクションデータ
package "トランザクションデータ" TRANSACTION_COLOR {
  entity "仕訳" as Journal {
    * **仕訳伝票番号**: VARCHAR(20) <<PK>>
    --
    * **起票日**: DATE
    * **入力日**: DATE
    * **決算仕訳フラグ**: INTEGER
    * **単振フラグ**: INTEGER
    * **仕訳伝票区分**: INTEGER
    * **定期計上フラグ**: INTEGER
    o **社員コード**: VARCHAR(10)
    o **部門コード**: VARCHAR(5)
    * **赤伝フラグ**: INTEGER
    o **赤黒伝票番号**: VARCHAR(20)
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "仕訳明細" as JournalDetail {
    * **仕訳伝票番号**: VARCHAR(20) <<PK,FK>>
    * **仕訳行番号**: INTEGER <<PK>>
    --
    * **行摘要**: VARCHAR(1000)
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "仕訳貸借明細" as JournalDetailItem {
    * **仕訳伝票番号**: VARCHAR(20) <<PK,FK>>
    * **仕訳行番号**: INTEGER <<PK,FK>>
    * **仕訳行貸借区分**: CHAR(1) <<PK>>
    --
    * **通貨コード**: VARCHAR(3)
    * **為替レート**: NUMERIC(10,4)
    o **部門コード**: VARCHAR(5)
    o **プロジェクトコード**: VARCHAR(10)
    * **勘定科目コード**: VARCHAR(10) <<FK>>
    o **補助科目コード**: VARCHAR(10)
    * **仕訳金額**: NUMERIC(15,2)
    * **基軸換算仕訳金額**: NUMERIC(15,2)
    o **消費税区分**: VARCHAR(2)
    o **消費税率**: INTEGER
    o **消費税計算区分**: VARCHAR(2)
    o **期日**: DATE
    * **資金繰フラグ**: INTEGER
    o **セグメントコード**: VARCHAR(10)
    o **相手勘定科目コード**: VARCHAR(10)
    o **付箋コード**: VARCHAR(1)
    o **付箋内容**: VARCHAR(60)
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }
}

' 残高データ
package "残高データ" BALANCE_COLOR {
  entity "日次勘定科目残高" as DailyBalance {
    * **起票日**: DATE <<PK>>
    * **勘定科目コード**: VARCHAR(10) <<PK,FK>>
    * **補助科目コード**: VARCHAR(10) <<PK>>
    * **部門コード**: VARCHAR(5) <<PK>>
    * **プロジェクトコード**: VARCHAR(10) <<PK>>
    * **決算仕訳フラグ**: INTEGER <<PK>>
    --
    * **借方金額**: NUMERIC(15,2)
    * **貸方金額**: NUMERIC(15,2)
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "月次勘定科目残高" as MonthlyBalance {
    * **決算期**: INTEGER <<PK>>
    * **月度**: INTEGER <<PK>>
    * **勘定科目コード**: VARCHAR(10) <<PK,FK>>
    * **補助科目コード**: VARCHAR(10) <<PK>>
    * **部門コード**: VARCHAR(5) <<PK>>
    * **プロジェクトコード**: VARCHAR(10) <<PK>>
    * **決算仕訳フラグ**: INTEGER <<PK>>
    --
    * **月初残高**: NUMERIC(15,2)
    * **借方金額**: NUMERIC(15,2)
    * **貸方金額**: NUMERIC(15,2)
    * **月末残高**: NUMERIC(15,2)
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }
}

' 自動仕訳
package "自動仕訳" TRANSACTION_COLOR {
  entity "自動仕訳管理" as AutoJournalManagement {
    * **自動仕訳管理ID**: BIGSERIAL <<PK>>
    --
    * **ソーステーブル名**: VARCHAR(100) <<UK>>
    * **最終処理日時**: TIMESTAMP
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "自動仕訳パターン" as AutoJournalPattern {
    * **自動仕訳パターンID**: BIGSERIAL <<PK>>
    --
    * **パターンコード**: VARCHAR(20) <<UK>>
    * **パターン名**: VARCHAR(100)
    * **ソーステーブル名**: VARCHAR(100)
    o **説明**: VARCHAR(500)
    * **有効フラグ**: BOOLEAN
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "自動仕訳パターン明細" as AutoJournalPatternItem {
    * **自動仕訳パターン明細ID**: BIGSERIAL <<PK>>
    --
    * **自動仕訳パターンID**: BIGINT <<FK>>
    * **行番号**: INTEGER
    * **貸借区分**: CHAR(1)
    * **勘定科目コード**: VARCHAR(10) <<FK>>
    * **金額計算式**: VARCHAR(200)
    o **摘要テンプレート**: VARCHAR(200)
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "自動仕訳実行ログ" as AutoJournalLog {
    * **自動仕訳実行ログID**: BIGSERIAL <<PK>>
    --
    * **自動仕訳パターンID**: BIGINT <<FK>>
    * **実行日時**: TIMESTAMP
    * **処理件数**: INTEGER
    * **生成件数**: INTEGER
    * **ステータス**: VARCHAR(20)
    o **メッセージ**: VARCHAR(500)
    o **エラー詳細**: TEXT
    * **作成日時**: TIMESTAMP
  }
}

' ユーザー管理
package "ユーザー管理" USER_COLOR {
  entity "ユーザー" as User {
    * **ユーザーID**: BIGSERIAL <<PK>>
    --
    * **ユーザー名**: VARCHAR(50) <<UK>>
    * **メールアドレス**: VARCHAR(255) <<UK>>
    * **パスワードハッシュ**: VARCHAR(255)
    * **表示名**: VARCHAR(100)
    * **ステータス**: VARCHAR(20)
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "ロール" as Role {
    * **ロールID**: BIGSERIAL <<PK>>
    --
    * **ロール名**: VARCHAR(50) <<UK>>
    o **説明**: VARCHAR(255)
    * **作成日時**: TIMESTAMP
    * **更新日時**: TIMESTAMP
  }

  entity "ユーザーロール" as UserRole {
    * **ユーザーID**: BIGINT <<PK,FK>>
    * **ロールID**: BIGINT <<PK,FK>>
    --
    * **作成日時**: TIMESTAMP
  }
}

' 監査・履歴
package "監査・履歴" AUDIT_COLOR {
  entity "操作履歴" as OperationHistory {
    * **操作履歴ID**: BIGSERIAL <<PK>>
    --
    * **ユーザーID**: BIGINT <<FK>>
    * **操作種別**: VARCHAR(50)
    * **操作対象**: VARCHAR(100)
    o **操作内容**: TEXT
    * **操作日時**: TIMESTAMP
    o **IPアドレス**: VARCHAR(45)
  }

  entity "仕訳変更履歴" as JournalChangeHistory {
    * **変更履歴ID**: BIGSERIAL <<PK>>
    --
    * **仕訳伝票番号**: VARCHAR(20)
    * **変更種別**: VARCHAR(20)
    * **変更前データ**: JSONB
    * **変更後データ**: JSONB
    * **変更者ID**: BIGINT <<FK>>
    * **変更日時**: TIMESTAMP
  }

  entity "ログイン履歴" as LoginHistory {
    * **ログイン履歴ID**: BIGSERIAL <<PK>>
    --
    * **ユーザーID**: BIGINT <<FK>>
    * **ログイン日時**: TIMESTAMP
    o **ログアウト日時**: TIMESTAMP
    o **IPアドレス**: VARCHAR(45)
    o **ユーザーエージェント**: VARCHAR(500)
    * **成功フラグ**: BOOLEAN
  }
}

' リレーションシップ
Account ||--o| AccountStructure : "1:0..1"
Account }o--o| TaxTransaction : "N:0..1"

Journal ||--|{ JournalDetail : "1:N"
JournalDetail ||--|{ JournalDetailItem : "1:N"
JournalDetailItem }o--|| Account : "参照"

Account ||--o{ DailyBalance : "1:N"
Account ||--o{ MonthlyBalance : "1:N"

AutoJournalPattern ||--o{ AutoJournalPatternItem : "1:N"
AutoJournalPattern ||--o{ AutoJournalLog : "1:N"
AutoJournalPatternItem }o--|| Account : "参照"

User ||--o{ UserRole : "1:N"
Role ||--o{ UserRole : "1:N"

User ||--o{ OperationHistory : "1:N"
User ||--o{ JournalChangeHistory : "1:N"
User ||--o{ LoginHistory : "1:N"

@enduml
```

---

## マスタデータ

### 勘定科目マスタ

財務会計システムの基礎となる勘定科目情報を管理します。

```plantuml
@startuml
entity "勘定科目マスタ" as Account {
  * **勘定科目ID**: SERIAL <<PK>>
  --
  * **勘定科目コード**: VARCHAR(20) <<UK>>
  * **勘定科目名**: VARCHAR(100)
  * **勘定科目種別**: ENUM
  o **勘定科目カナ**: VARCHAR(40)
  o **BSPL区分**: CHAR(1)
  o **取引要素区分**: CHAR(1)
  o **費用区分**: CHAR(1)
  * **合計科目**: BOOLEAN
  o **表示順序**: INTEGER
  * **集計対象**: BOOLEAN
  * **残高**: DECIMAL(15,2)
  o **課税取引コード**: VARCHAR(2) <<FK>>
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

note right of Account
  **勘定科目種別**
  - 資産
  - 負債
  - 純資産
  - 収益
  - 費用

  **BSPL区分**
  - B: 貸借対照表
  - P: 損益計算書

  **取引要素区分**
  - 1: 資産
  - 2: 負債
  - 3: 純資産
  - 4: 収益
  - 5: 費用
end note
@enduml
```

#### 項目定義

| 項目名 | データ型 | 必須 | 説明 |
|--------|----------|------|------|
| 勘定科目ID | SERIAL | ○ | 主キー（自動採番） |
| 勘定科目コード | VARCHAR(20) | ○ | 勘定科目を一意に識別するコード |
| 勘定科目名 | VARCHAR(100) | ○ | 勘定科目の正式名称 |
| 勘定科目種別 | ENUM | ○ | 資産、負債、純資産、収益、費用 |
| 勘定科目カナ | VARCHAR(40) | - | 検索用のカナ表記 |
| BSPL区分 | CHAR(1) | - | B: 貸借対照表、P: 損益計算書 |
| 取引要素区分 | CHAR(1) | - | 1〜5 の数値で会計 5 要素を表現 |
| 費用区分 | CHAR(1) | - | 1: 売上原価、2: 販管費、3: 営業外費用 |
| 合計科目 | BOOLEAN | ○ | true: 集計科目、false: 明細科目 |
| 表示順序 | INTEGER | - | 財務諸表での表示順 |
| 集計対象 | BOOLEAN | ○ | true: 集計対象、false: 集計対象外 |
| 残高 | DECIMAL(15,2) | ○ | 現在の残高 |
| 課税取引コード | VARCHAR(2) | - | 課税取引マスタへの外部キー |

### 勘定科目構成マスタ

勘定科目の階層構造を**チルダ連結方式**で管理します。

```plantuml
@startuml
entity "勘定科目構成マスタ" as AccountStructure {
  * **勘定科目コード**: VARCHAR(20) <<PK,FK>>
  --
  * **勘定科目パス**: VARCHAR(200)
  * **階層レベル**: INTEGER
  o **親科目コード**: VARCHAR(20)
  * **表示順序**: INTEGER
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

note right of AccountStructure
  **チルダ連結方式**

  勘定科目パス: "11~11000~11190~11110"
  ├─ 11: 資産の部
  ├─ 11000: 流動資産
  ├─ 11190: 現金及び預金
  └─ 11110: 現金

  階層レベル: 4
  親科目コード: 11190
end note
@enduml
```

#### チルダ連結方式の利点

| 利点 | 説明 |
|------|------|
| シンプルなデータ構造 | 親子関係を 1 つのカラムで表現 |
| 効率的な階層検索 | LIKE 演算子で子孫科目を一括取得 |
| 集計処理の高速化 | 特定科目配下の残高集計が容易 |
| 柔軟な階層構造 | 任意の深さの階層に対応 |

#### 科目ツリー構造

```plantuml
@startwbs
title 貸借対照表の科目ツリー構造（部分）

* 貸借対照表
** 資産の部
*** 流動資産
**** 現金及び預金
***** 現金
***** 当座預金
***** 普通預金
*** 固定資産
**** 有形固定資産
***** 建物
***** 機械及び装置
**** 無形固定資産
**** 投資等
** 負債の部
*** 流動負債
**** 支払手形
**** 買掛金
*** 固定負債
** 資本の部
*** 資本金
*** 利益剰余金
@endwbs
```

### 課税取引マスタ

消費税の課税区分を管理します。

```plantuml
@startuml
entity "課税取引マスタ" as TaxTransaction {
  * **課税取引コード**: VARCHAR(2) <<PK>>
  --
  * **課税取引名**: VARCHAR(20)
  * **税率**: DECIMAL(5,3)
  o **説明**: VARCHAR(200)
  * **有効フラグ**: BOOLEAN
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

note right of TaxTransaction
  **課税取引コード**
  - 01: 課税（10%）
  - 02: 非課税（0%）
  - 03: 免税（0%）
  - 04: 不課税（0%）

  税率の制約:
  0 <= 税率 <= 1
  (0.10 = 10%)
end note
@enduml
```

#### 課税取引の種類

| 課税取引コード | 課税取引名 | 税率 | 説明 |
|----------------|------------|------|------|
| 01 | 課税 | 10% | 消費税が課税される取引 |
| 02 | 非課税 | 0% | 土地の譲渡、住宅の貸付など |
| 03 | 免税 | 0% | 輸出取引など |
| 04 | 不課税 | 0% | 給与、配当など |

### 会計期間マスタ

会計年度と期間を管理します。

```plantuml
@startuml
entity "会計期間マスタ" as AccountingPeriod {
  * **会計期間ID**: SERIAL <<PK>>
  --
  * **会計年度**: INTEGER
  * **期間名**: VARCHAR(50)
  * **開始日**: DATE
  * **終了日**: DATE
  * **締め状態**: VARCHAR(20)
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

note right of AccountingPeriod
  **締め状態**
  - OPEN: 入力可能
  - CLOSING: 締め処理中
  - CLOSED: 締め完了

  **状態遷移**
  OPEN -> CLOSING -> CLOSED
end note
@enduml
```

---

## トランザクションデータ

### 3 層構造仕訳テーブル

仕訳データは 3 層構造で管理し、複合仕訳（1 つの取引が複数の借方・貸方を持つ）に対応します。

```plantuml
@startuml
title 仕訳データの3層構造

entity "仕訳" as Journal {
  * **仕訳伝票番号**: VARCHAR(20) <<PK>>
  --
  * **起票日**: DATE
  * **入力日**: DATE
  * **決算仕訳フラグ**: INTEGER
  * **単振フラグ**: INTEGER
  * **仕訳伝票区分**: INTEGER
  * **定期計上フラグ**: INTEGER
  o **社員コード**: VARCHAR(10)
  o **部門コード**: VARCHAR(5)
  * **赤伝フラグ**: INTEGER
  o **赤黒伝票番号**: VARCHAR(20)
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

entity "仕訳明細" as JournalDetail {
  * **仕訳伝票番号**: VARCHAR(20) <<PK,FK>>
  * **仕訳行番号**: INTEGER <<PK>>
  --
  * **行摘要**: VARCHAR(1000)
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

entity "仕訳貸借明細" as JournalDetailItem {
  * **仕訳伝票番号**: VARCHAR(20) <<PK,FK>>
  * **仕訳行番号**: INTEGER <<PK,FK>>
  * **仕訳行貸借区分**: CHAR(1) <<PK>>
  --
  * **通貨コード**: VARCHAR(3)
  * **為替レート**: NUMERIC(10,4)
  o **部門コード**: VARCHAR(5)
  o **プロジェクトコード**: VARCHAR(10)
  * **勘定科目コード**: VARCHAR(10)
  o **補助科目コード**: VARCHAR(10)
  * **仕訳金額**: NUMERIC(15,2)
  * **基軸換算仕訳金額**: NUMERIC(15,2)
  o **消費税区分**: VARCHAR(2)
  o **消費税率**: INTEGER
  o **消費税計算区分**: VARCHAR(2)
  o **期日**: DATE
  * **資金繰フラグ**: INTEGER
  o **セグメントコード**: VARCHAR(10)
  o **相手勘定科目コード**: VARCHAR(10)
  o **付箋コード**: VARCHAR(1)
  o **付箋内容**: VARCHAR(60)
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

Journal ||--|{ JournalDetail : "1:N"
JournalDetail ||--|{ JournalDetailItem : "1:N"

note right of Journal
  **仕訳ヘッダー**
  ・伝票単位の基本情報
  ・起票日 vs 入力日の区別
  ・決算仕訳/赤伝の区別
end note

note right of JournalDetail
  **仕訳明細**
  ・行単位の情報
  ・行摘要を保持
end note

note right of JournalDetailItem
  **仕訳貸借明細**
  ・借方/貸方の詳細
  ・金額・科目・多次元情報

  仕訳行貸借区分:
  D=借方, C=貸方
end note
@enduml
```

#### 3 層構造の各層の役割

| 層 | テーブル名 | 役割 | 主キー |
|----|----------|------|--------|
| 第 1 層 | 仕訳 | 伝票単位の基本情報 | 仕訳伝票番号 |
| 第 2 層 | 仕訳明細 | 行単位の情報（行摘要） | 仕訳伝票番号 + 仕訳行番号 |
| 第 3 層 | 仕訳貸借明細 | 借方・貸方の詳細 | 仕訳伝票番号 + 仕訳行番号 + 仕訳行貸借区分 |

#### 起票日と入力日の区別

| 項目 | 説明 | 例 |
|------|------|-----|
| 起票日 | 実際の取引が発生した日付 | 2025/01/15（納品日） |
| 入力日 | システムに入力した日付 | 2025/01/20（経理処理日） |

#### 赤黒処理

仕訳の修正は赤黒処理で行い、監査証跡を確保します。

```plantuml
@startuml
title 赤黒処理の流れ

rectangle "元の仕訳\nJE-001" as original {
  card "借方: 交際費 10,000円\n貸方: 現金 10,000円"
}

rectangle "赤伝票\nJE-002" as red {
  card "借方: 交際費 -10,000円\n貸方: 現金 -10,000円\n**赤伝フラグ=1**\n**赤黒伝票番号=JE-001**"
}

rectangle "黒伝票\nJE-003" as black {
  card "借方: 会議費 10,000円\n貸方: 現金 10,000円\n**赤黒伝票番号=JE-002**"
}

original --> red : 取消
red --> black : 再入力
@enduml
```

### 自動仕訳

日付管理方式による自動仕訳テーブルを設計します。

```plantuml
@startuml
entity "自動仕訳管理" as AutoJournalManagement {
  * **自動仕訳管理ID**: BIGSERIAL <<PK>>
  --
  * **ソーステーブル名**: VARCHAR(100) <<UK>>
  * **最終処理日時**: TIMESTAMP
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

entity "自動仕訳パターン" as AutoJournalPattern {
  * **自動仕訳パターンID**: BIGSERIAL <<PK>>
  --
  * **パターンコード**: VARCHAR(20) <<UK>>
  * **パターン名**: VARCHAR(100)
  * **ソーステーブル名**: VARCHAR(100)
  o **説明**: VARCHAR(500)
  * **有効フラグ**: BOOLEAN
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

entity "自動仕訳パターン明細" as AutoJournalPatternItem {
  * **自動仕訳パターン明細ID**: BIGSERIAL <<PK>>
  --
  * **自動仕訳パターンID**: BIGINT <<FK>>
  * **行番号**: INTEGER
  * **貸借区分**: CHAR(1)
  * **勘定科目コード**: VARCHAR(10) <<FK>>
  * **金額計算式**: VARCHAR(200)
  o **摘要テンプレート**: VARCHAR(200)
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

entity "自動仕訳実行ログ" as AutoJournalLog {
  * **自動仕訳実行ログID**: BIGSERIAL <<PK>>
  --
  * **自動仕訳パターンID**: BIGINT <<FK>>
  * **実行日時**: TIMESTAMP
  * **処理件数**: INTEGER
  * **生成件数**: INTEGER
  * **ステータス**: VARCHAR(20)
  o **メッセージ**: VARCHAR(500)
  o **エラー詳細**: TEXT
  * **作成日時**: TIMESTAMP
}

AutoJournalPattern ||--o{ AutoJournalPatternItem : "1:N"
AutoJournalPattern ||--o{ AutoJournalLog : "1:N"

note right of AutoJournalManagement
  **日付管理方式**
  ・ソースデータを変更しない
  ・最終処理日時で差分処理
  ・再処理が容易
end note
@enduml
```

---

## 残高データ

### 日次勘定科目残高

仕訳入力時に即時更新（UPSERT）される日次の残高テーブルです。

```plantuml
@startuml
entity "日次勘定科目残高" as DailyBalance {
  * **起票日**: DATE <<PK>>
  * **勘定科目コード**: VARCHAR(10) <<PK,FK>>
  * **補助科目コード**: VARCHAR(10) <<PK>>
  * **部門コード**: VARCHAR(5) <<PK>>
  * **プロジェクトコード**: VARCHAR(10) <<PK>>
  * **決算仕訳フラグ**: INTEGER <<PK>>
  --
  * **借方金額**: NUMERIC(15,2)
  * **貸方金額**: NUMERIC(15,2)
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

note right of DailyBalance
  **複合主キー（6項目）**

  仕訳入力時に ON CONFLICT DO UPDATE で
  同日・同科目・同部門の残高を累積

  これにより多次元管理が可能:
  ・部門別残高
  ・プロジェクト別残高
  ・補助科目別残高
end note
@enduml
```

### 月次勘定科目残高

日次残高から月次集計される残高テーブルです。

```plantuml
@startuml
entity "月次勘定科目残高" as MonthlyBalance {
  * **決算期**: INTEGER <<PK>>
  * **月度**: INTEGER <<PK>>
  * **勘定科目コード**: VARCHAR(10) <<PK,FK>>
  * **補助科目コード**: VARCHAR(10) <<PK>>
  * **部門コード**: VARCHAR(5) <<PK>>
  * **プロジェクトコード**: VARCHAR(10) <<PK>>
  * **決算仕訳フラグ**: INTEGER <<PK>>
  --
  * **月初残高**: NUMERIC(15,2)
  * **借方金額**: NUMERIC(15,2)
  * **貸方金額**: NUMERIC(15,2)
  * **月末残高**: NUMERIC(15,2)
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

note right of MonthlyBalance
  **月次残高の計算式**

  月末残高 = 月初残高 + 借方金額 - 貸方金額

  月次締め処理で日次残高を集計して更新
end note
@enduml
```

### 残高管理の全体フロー

```plantuml
@startuml
title 残高管理の全体像

card "仕訳データ" {
  rectangle "仕訳" as journal
  rectangle "仕訳明細" as detail
  rectangle "仕訳貸借明細" as item
  journal --> detail
  detail --> item
}

card "残高データ" {
  rectangle "日次勘定科目残高" as daily {
    card "起票日ごとの\n借方・貸方金額"
  }

  rectangle "月次勘定科目残高" as monthly {
    card "月初残高\n借方・貸方金額\n月末残高"
  }
}

card "財務帳票" {
  rectangle "総勘定元帳" as ledger
  rectangle "試算表" as trial
  rectangle "貸借対照表" as bs
  rectangle "損益計算書" as pl
}

item -down-> daily : "即時更新\n(UPSERT)"
daily -down-> monthly : "月次集計"
daily -down-> ledger : "ビュー生成"
daily -down-> trial : "ビュー生成"
monthly -down-> bs : "財務諸表生成"
monthly -down-> pl : "財務諸表生成"
@enduml
```

---

## ユーザー管理

### ユーザー・ロール

```plantuml
@startuml
entity "ユーザー" as User {
  * **ユーザーID**: BIGSERIAL <<PK>>
  --
  * **ユーザー名**: VARCHAR(50) <<UK>>
  * **メールアドレス**: VARCHAR(255) <<UK>>
  * **パスワードハッシュ**: VARCHAR(255)
  * **表示名**: VARCHAR(100)
  * **ステータス**: VARCHAR(20)
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

entity "ロール" as Role {
  * **ロールID**: BIGSERIAL <<PK>>
  --
  * **ロール名**: VARCHAR(50) <<UK>>
  o **説明**: VARCHAR(255)
  * **作成日時**: TIMESTAMP
  * **更新日時**: TIMESTAMP
}

entity "ユーザーロール" as UserRole {
  * **ユーザーID**: BIGINT <<PK,FK>>
  * **ロールID**: BIGINT <<PK,FK>>
  --
  * **作成日時**: TIMESTAMP
}

User ||--o{ UserRole : "1:N"
Role ||--o{ UserRole : "1:N"

note right of User
  **ステータス**
  - ACTIVE: 有効
  - INACTIVE: 無効
  - LOCKED: ロック
end note

note right of Role
  **主要ロール**
  - ADMIN: 管理者
  - ACCOUNTANT: 経理担当者
  - VIEWER: 閲覧者
end note
@enduml
```

---

## 監査・履歴

### 監査テーブル群

```plantuml
@startuml
entity "操作履歴" as OperationHistory {
  * **操作履歴ID**: BIGSERIAL <<PK>>
  --
  * **ユーザーID**: BIGINT <<FK>>
  * **操作種別**: VARCHAR(50)
  * **操作対象**: VARCHAR(100)
  o **操作内容**: TEXT
  * **操作日時**: TIMESTAMP
  o **IPアドレス**: VARCHAR(45)
}

entity "仕訳変更履歴" as JournalChangeHistory {
  * **変更履歴ID**: BIGSERIAL <<PK>>
  --
  * **仕訳伝票番号**: VARCHAR(20)
  * **変更種別**: VARCHAR(20)
  * **変更前データ**: JSONB
  * **変更後データ**: JSONB
  * **変更者ID**: BIGINT <<FK>>
  * **変更日時**: TIMESTAMP
}

entity "ログイン履歴" as LoginHistory {
  * **ログイン履歴ID**: BIGSERIAL <<PK>>
  --
  * **ユーザーID**: BIGINT <<FK>>
  * **ログイン日時**: TIMESTAMP
  o **ログアウト日時**: TIMESTAMP
  o **IPアドレス**: VARCHAR(45)
  o **ユーザーエージェント**: VARCHAR(500)
  * **成功フラグ**: BOOLEAN
}

note right of JournalChangeHistory
  **変更種別**
  - CREATE: 新規作成
  - UPDATE: 更新
  - DELETE: 削除
  - REVERSE: 赤伝取消

  変更前後のデータを JSONB で保存し
  完全な監査証跡を確保
end note
@enduml
```

---

## 主要テーブル一覧

### マスタ系テーブル

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| 勘定科目マスタ | 勘定科目の基本情報 | 勘定科目ID |
| 勘定科目構成マスタ | 勘定科目の階層構造 | 勘定科目コード |
| 課税取引マスタ | 消費税の課税区分 | 課税取引コード |
| 会計期間マスタ | 会計年度と期間 | 会計期間ID |

### トランザクション系テーブル

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| 仕訳 | 仕訳ヘッダー | 仕訳伝票番号 |
| 仕訳明細 | 行単位の情報 | 仕訳伝票番号 + 仕訳行番号 |
| 仕訳貸借明細 | 借方・貸方の詳細 | 仕訳伝票番号 + 仕訳行番号 + 仕訳行貸借区分 |
| 自動仕訳管理 | 自動仕訳の処理状況 | 自動仕訳管理ID |
| 自動仕訳パターン | 仕訳生成パターン定義 | 自動仕訳パターンID |
| 自動仕訳パターン明細 | パターンの明細 | 自動仕訳パターン明細ID |
| 自動仕訳実行ログ | 実行履歴 | 自動仕訳実行ログID |

### 残高系テーブル

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| 日次勘定科目残高 | 日次の借方・貸方金額 | 起票日 + 勘定科目コード + 補助科目コード + 部門コード + プロジェクトコード + 決算仕訳フラグ |
| 月次勘定科目残高 | 月次の残高推移 | 決算期 + 月度 + 勘定科目コード + 補助科目コード + 部門コード + プロジェクトコード + 決算仕訳フラグ |

### ユーザー・監査系テーブル

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| ユーザー | ユーザー情報 | ユーザーID |
| ロール | ロール定義 | ロールID |
| ユーザーロール | ユーザーとロールの関連 | ユーザーID + ロールID |
| 操作履歴 | 操作の監査ログ | 操作履歴ID |
| 仕訳変更履歴 | 仕訳の変更履歴 | 変更履歴ID |
| ログイン履歴 | ログインの履歴 | ログイン履歴ID |

---

## データ整合性の保証

### 複式簿記の原理

仕訳ごとに「借方合計 = 貸方合計」を保証します。

```sql
-- 仕訳残高チェックビュー
CREATE OR REPLACE VIEW 仕訳残高チェック AS
SELECT
  "仕訳伝票番号",
  SUM(CASE WHEN "仕訳行貸借区分" = 'D' THEN "仕訳金額" ELSE 0 END) AS 借方合計,
  SUM(CASE WHEN "仕訳行貸借区分" = 'C' THEN "仕訳金額" ELSE 0 END) AS 貸方合計,
  SUM(CASE WHEN "仕訳行貸借区分" = 'D' THEN "仕訳金額" ELSE 0 END) -
  SUM(CASE WHEN "仕訳行貸借区分" = 'C' THEN "仕訳金額" ELSE 0 END) AS 差額
FROM "仕訳貸借明細"
GROUP BY "仕訳伝票番号";

-- 複式簿記チェック関数
CREATE OR REPLACE FUNCTION 複式簿記チェック()
RETURNS TABLE(不整合伝票番号 VARCHAR(20), 差額 DECIMAL) AS $$
BEGIN
  RETURN QUERY
  SELECT "仕訳伝票番号", (借方合計 - 貸方合計) as 差額
  FROM 仕訳残高チェック
  WHERE 借方合計 != 貸方合計;
END;
$$ LANGUAGE plpgsql;
```

### CHECK 制約

```sql
-- 貸借区分は D または C のみ
ALTER TABLE "仕訳貸借明細"
  ADD CONSTRAINT "check_貸借区分"
  CHECK ("仕訳行貸借区分" IN ('D', 'C'));

-- 仕訳金額は 0 以上
ALTER TABLE "仕訳貸借明細"
  ADD CONSTRAINT "check_仕訳金額"
  CHECK ("仕訳金額" >= 0);

-- 赤伝票の場合は赤黒伝票番号が必須
ALTER TABLE "仕訳"
  ADD CONSTRAINT "check_赤伝票_赤黒伝票番号"
  CHECK (
    ("赤伝フラグ" = 0)
    OR
    ("赤伝フラグ" = 1 AND "赤黒伝票番号" IS NOT NULL)
  );
```

---

## インデックス設計

### 主要インデックス

```sql
-- 仕訳系
CREATE INDEX "idx_仕訳_起票日" ON "仕訳"("起票日");
CREATE INDEX "idx_仕訳_部門コード" ON "仕訳"("部門コード");
CREATE INDEX "idx_仕訳貸借明細_勘定科目" ON "仕訳貸借明細"("勘定科目コード");

-- 残高系
CREATE INDEX "idx_日次勘定科目残高_起票日" ON "日次勘定科目残高"("起票日");
CREATE INDEX "idx_日次勘定科目残高_勘定科目" ON "日次勘定科目残高"("勘定科目コード");
CREATE INDEX "idx_月次勘定科目残高_決算期月度" ON "月次勘定科目残高"("決算期", "月度");

-- 勘定科目構成
CREATE INDEX idx_account_structure_path ON "勘定科目構成マスタ" ("勘定科目パス");
CREATE INDEX idx_account_structure_parent ON "勘定科目構成マスタ" ("親科目コード");
```

---

## 参考文献

- [データモデル設計ガイド](../reference/データモデル設計ガイド.md)
- [アーキテクチャ設計ガイド](../reference/アーキテクチャ設計ガイド.md)
- [財務会計システムのケーススタディ（バックエンド編）](../article/backend/chapter00.md)

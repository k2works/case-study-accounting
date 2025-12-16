---
title: chapter04
description:
published: true
date: 2025-12-15T00:00:00.000Z
tags:
editor: markdown
dateCreated: 2025-12-15T00:00:00.000Z
---

# 第4章: データモデル設計の基礎

## 4.1 ER モデリングの原則

### エンティティの識別

エンティティとは、システムで管理すべき「もの」や「こと」を表す概念です。財務会計システムでは、以下のようなエンティティを識別します。

```plantuml
@startuml
title エンティティの分類

package "マスタエンティティ" {
  entity "勘定科目" as account
  entity "勘定科目構成" as account_struct
  entity "課税取引" as tax_trans
  entity "自動仕訳設定" as auto_journal
}

package "トランザクションエンティティ" {
  entity "仕訳" as journal
  entity "仕訳明細" as journal_line
  entity "日次残高" as daily_balance
  entity "月次残高" as monthly_balance
}

package "リソースエンティティ" {
  entity "ユーザー" as user
  entity "監査ログ" as audit
}

@enduml
```

#### エンティティの種類

| 種類 | 説明 | 例 |
|------|------|-----|
| マスタエンティティ | 比較的変更が少ない基本情報 | 勘定科目、勘定科目構成、課税取引 |
| トランザクションエンティティ | 業務活動の記録 | 仕訳、仕訳明細、残高 |
| リソースエンティティ | システム運用に必要な情報 | ユーザー、監査ログ |

#### エンティティ識別のポイント

1. **独立して存在できるか**: 他のエンティティに依存せず、単独で意味を持つか
2. **一意に識別できるか**: 主キーによって各インスタンスを区別できるか
3. **複数のインスタンスを持つか**: 一覧として管理する必要があるか

### リレーションシップの設計

エンティティ間の関係を定義します。

```plantuml
@startuml
title リレーションシップの種類

entity "勘定科目" as account {
  * 勘定科目ID [PK]
  --
  勘定科目コード
  勘定科目名
  勘定科目種別
}

entity "勘定科目構成" as account_struct {
  * 勘定科目構成ID [PK]
  --
  勘定科目コード [FK]
  勘定科目パス
  階層レベル
}

entity "仕訳" as journal {
  * 仕訳ID [PK]
  --
  取引日
  摘要
  ステータス
}

entity "仕訳明細" as journal_line {
  * 仕訳ID [PK][FK]
  * 明細番号 [PK]
  --
  勘定科目ID [FK]
  借方金額
  貸方金額
}

account ||--o| account_struct : "構成"
account ||--o{ journal_line : "使用"
journal ||--|{ journal_line : "含む"

@enduml
```

#### カーディナリティ（多重度）

| 記法 | 意味 | 例 |
|------|------|-----|
| `1:1` | 1対1 | 勘定科目 - 勘定科目構成 |
| `1:N` | 1対多 | 仕訳 - 仕訳明細 |
| `N:M` | 多対多 | 勘定科目 - 仕訳（仕訳明細を介して） |

#### 依存関係の種類

| 種類 | 説明 | 例 |
|------|------|-----|
| 識別依存 | 親の主キーが子の主キーの一部 | 仕訳 - 仕訳明細 |
| 非識別依存 | 親の主キーが子の外部キー | 勘定科目 - 仕訳明細 |

### 正規化と非正規化のトレードオフ

データの冗長性を排除しつつ、パフォーマンスを考慮した設計が必要です。

```plantuml
@startuml
title 正規化のレベル

rectangle "非正規形" as unnorm {
  note "重複データを含む\n更新異常が発生しやすい" as n1
}

rectangle "第1正規形" as 1nf {
  note "繰り返し項目を排除\n各列は原子値" as n2
}

rectangle "第2正規形" as 2nf {
  note "部分関数従属を排除\n主キー全体に依存" as n3
}

rectangle "第3正規形" as 3nf {
  note "推移的関数従属を排除\n非キー列同士の依存なし" as n4
}

unnorm --> 1nf
1nf --> 2nf
2nf --> 3nf

@enduml
```

#### 本システムでの判断

| 観点 | 正規化 | 非正規化 |
|------|--------|----------|
| データ整合性 | 高い | 低い |
| 更新性能 | 高い | 低い |
| 参照性能 | 低い（結合が必要） | 高い |
| ストレージ | 少ない | 多い |

本システムでは、基本的に第3正規形を採用しつつ、以下の場合に非正規化を検討します。

- **勘定科目名の複製**: 仕訳明細に勘定科目名を保持（履歴保持のため）
- **残高の事前計算**: 日次・月次残高テーブルで集計済み残高を保持（集計性能のため）

---

## 4.2 財務会計システムの全体データモデル

### 主要エンティティの洗い出し

本システムの主要エンティティを以下に示します。

```plantuml
@startuml
title 財務会計システム 全体データモデル（概要）

' マスタ系
package "マスタ" {
  entity "勘定科目マスタ" as account
  entity "勘定科目構成マスタ" as account_struct
  entity "課税取引マスタ" as tax_trans
  entity "自動仕訳設定" as auto_journal_setting
}

' 仕訳系
package "仕訳" {
  entity "仕訳ヘッダ" as journal
  entity "仕訳明細" as journal_line
  entity "自動仕訳" as auto_journal
}

' 残高系
package "残高" {
  entity "日次勘定科目残高" as daily_balance
  entity "月次勘定科目残高" as monthly_balance
}

' 元帳系
package "元帳" {
  entity "総勘定元帳" as general_ledger
  entity "補助元帳" as subsidiary_ledger
}

' 財務諸表系
package "財務諸表" {
  entity "残高試算表" as trial_balance
  entity "貸借対照表" as balance_sheet
  entity "損益計算書" as income_statement
}

' リレーション（主要なもののみ）
account ||--o| account_struct
account ||--o{ journal_line
account ||--o{ daily_balance
account ||--o{ monthly_balance

journal ||--|{ journal_line
journal_line }o--|| account

daily_balance }o--|| account
monthly_balance }o--|| account

auto_journal_setting ||--o{ auto_journal

@enduml
```

### 勘定科目の5分類

財務会計システムの基盤となる勘定科目は、以下の5つに分類されます。

```plantuml
@startuml
title 勘定科目の5分類

package "貸借対照表（B/S）勘定" {
  rectangle "資産（Asset）" as asset {
    note "借方で増加\n貸方で減少" as n1
  }
  rectangle "負債（Liability）" as liability {
    note "貸方で増加\n借方で減少" as n2
  }
  rectangle "純資産（Equity）" as equity {
    note "貸方で増加\n借方で減少" as n3
  }
}

package "損益計算書（P/L）勘定" {
  rectangle "収益（Revenue）" as revenue {
    note "貸方で増加\n借方で減少" as n4
  }
  rectangle "費用（Expense）" as expense {
    note "借方で増加\n貸方で減少" as n5
  }
}

asset -[hidden]right- liability
liability -[hidden]right- equity
revenue -[hidden]right- expense

@enduml
```

#### 勘定科目種別と残高計算

| 種別 | 正常残高 | 借方計上 | 貸方計上 |
|------|----------|----------|----------|
| 資産（Asset） | 借方 | 増加 | 減少 |
| 負債（Liability） | 貸方 | 減少 | 増加 |
| 純資産（Equity） | 貸方 | 減少 | 増加 |
| 収益（Revenue） | 貸方 | 減少 | 増加 |
| 費用（Expense） | 借方 | 増加 | 減少 |

### ドメイン境界の設定

システムを以下のドメイン境界で分割しています。

```plantuml
@startuml
title ドメイン境界

rectangle "システム管理" as system {
  (ユーザー)
  (監査ログ)
}

rectangle "マスタ管理" as master {
  (勘定科目)
  (勘定科目構成)
  (課税取引)
}

rectangle "仕訳管理" as journal {
  (仕訳入力)
  (自動仕訳)
  (仕訳承認)
}

rectangle "元帳・残高管理" as ledger {
  (総勘定元帳)
  (補助元帳)
  (残高試算表)
}

rectangle "財務諸表" as statement {
  (貸借対照表)
  (損益計算書)
  (財務分析)
}

master --> journal : 参照
journal --> ledger : 転記
ledger --> statement : 集計

@enduml
```

### データフローの可視化

財務会計プロセスにおけるデータの流れを示します。

```plantuml
@startuml
title 仕訳から財務諸表へのデータフロー

|経理担当者|
start
:取引発生;

|仕訳管理|
:仕訳入力;
:仕訳データ作成;
note right
  借方・貸方の
  金額が一致
end note

:仕訳承認;
:仕訳確定;

|元帳・残高管理|
:総勘定元帳転記;
:日次残高更新;
:月次残高更新;

|財務諸表|
:残高試算表生成;
:貸借対照表生成;
:損益計算書生成;

|経営者|
:財務分析;

stop

@enduml
```

```plantuml
@startuml
title 自動仕訳のデータフロー

|システム|
start
:イベント発生;
note right
  減価償却
  経過勘定振替
  消費税計算
end note

|仕訳管理|
:自動仕訳設定参照;
:仕訳データ自動生成;

if (承認が必要?) then (yes)
  |経理担当者|
  :仕訳確認;
  :承認;
else (no)
  |仕訳管理|
  :自動承認;
endif

:仕訳確定;

|元帳・残高管理|
:元帳転記;
:残高更新;

stop

@enduml
```

### 複式簿記の原理とデータモデル

複式簿記では、すべての取引を借方（デビット）と貸方（クレジット）に記録します。

```plantuml
@startuml
title 複式簿記のデータモデル

entity "仕訳" as journal {
  * 仕訳ID [PK]
  --
  取引日
  摘要
  ステータス
  作成者
  承認者
}

entity "仕訳明細" as journal_line {
  * 仕訳ID [PK][FK]
  * 明細番号 [PK]
  --
  勘定科目ID [FK]
  借方金額
  貸方金額
  摘要
}

entity "勘定科目" as account {
  * 勘定科目ID [PK]
  --
  勘定科目コード
  勘定科目名
  勘定科目種別
}

journal ||--|{ journal_line : "1件の仕訳に\n2件以上の明細"
journal_line }o--|| account : "勘定科目を参照"

note bottom of journal_line
  複式簿記の原則:
  借方金額の合計 = 貸方金額の合計
end note

@enduml
```

#### 仕訳の例

| 取引 | 借方 | 貸方 |
|------|------|------|
| 現金 100万円で商品を仕入 | 仕入 100万円 | 現金 100万円 |
| 売掛金で商品を販売 200万円 | 売掛金 200万円 | 売上 200万円 |
| 給与 50万円を現金で支払 | 給与 50万円 | 現金 50万円 |

---

## 4.3 JIG-ERD によるモデル可視化

### ER 図の自動生成

JIG-ERD は ER 図を自動生成するツールです。

```java
// テストコードで ER 図を生成
@Test
void generateErDiagram() {
    var output = Path.of("build/jig-erd");
    var packageName = "com.example.accounting.infrastructure.datasource";

    JigErd.run(output, packageName);
}
```

#### 生成コマンド

```bash
./gradlew test --tests "*JigErdTest*"
```

### 概要・サマリー・詳細の使い分け

JIG-ERD は3つのレベルの ER 図を生成します。

```plantuml
@startuml
title JIG-ERD の出力レベル

rectangle "概要（Overview）" as overview {
  note "テーブル名のみ表示\n全体構造の把握に有効" as n1
}

rectangle "サマリー（Summary）" as summary {
  note "主キー・外部キーを表示\n主要なリレーションを確認" as n2
}

rectangle "詳細（Detail）" as detail {
  note "全カラムを表示\nテーブル設計の詳細確認" as n3
}

overview --> summary : より詳細に
summary --> detail : より詳細に

@enduml
```

#### 各レベルの用途

| レベル | ファイル名 | 用途 |
|--------|-----------|------|
| 概要 | library-er-overview.svg | 全体構造の把握、新規メンバーへの説明 |
| サマリー | library-er-summary.svg | リレーションの確認、設計レビュー |
| 詳細 | library-er-detail.svg | 実装時の参照、テーブル定義の確認 |

### 設計レビューへの活用

JIG-ERD で生成した ER 図は、以下の場面で活用します。

#### リリースごとのアーカイブ

```
docs/assets/release/
├── v0_1_0/
│   └── jig-erd/
│       ├── library-er-overview.svg
│       ├── library-er-summary.svg
│       └── library-er-detail.svg
├── v0_2_0/
│   └── jig-erd/
│       └── ...
└── v0_11_0/
    └── jig-erd/
        └── ...
```

#### 変更の追跡

バージョン間の ER 図を比較することで、データモデルの変更を視覚的に追跡できます。

```plantuml
@startuml
title データモデルの変更追跡

rectangle "v0.1.0" as v1 {
  (勘定科目)
  (勘定科目構成)
}

rectangle "v0.2.0" as v2 {
  (勘定科目)
  (勘定科目構成)
  (仕訳) #lightgreen
  (仕訳明細) #lightgreen
}

rectangle "v0.3.0" as v3 {
  (勘定科目)
  (勘定科目構成)
  (仕訳)
  (仕訳明細)
  (日次残高) #lightgreen
  (月次残高) #lightgreen
}

rectangle "v0.4.0" as v4 {
  (勘定科目)
  (勘定科目構成)
  (仕訳)
  (仕訳明細)
  (日次残高)
  (月次残高)
  (財務諸表) #lightgreen
}

v1 --> v2 : 仕訳追加
v2 --> v3 : 残高追加
v3 --> v4 : 財務諸表追加

@enduml
```

---

## 4.4 主要テーブル一覧

本システムの主要テーブルを以下に示します。

### マスタ系テーブル

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| 勘定科目マスタ | 勘定科目の基本情報 | 勘定科目ID |
| 勘定科目構成マスタ | 勘定科目の階層構造 | 勘定科目構成ID |
| 課税取引マスタ | 消費税の課税区分 | 課税取引コード |
| 自動仕訳設定 | 自動仕訳のテンプレート | 自動仕訳設定ID |

### トランザクション系テーブル

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| 仕訳 | 仕訳ヘッダ情報 | 仕訳ID |
| 仕訳明細 | 仕訳の借方・貸方明細 | 仕訳ID, 明細番号 |
| 自動仕訳 | 自動生成された仕訳 | 自動仕訳ID |

### 残高系テーブル

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| 日次勘定科目残高 | 日単位の勘定科目残高 | 勘定科目ID, 計上日 |
| 月次勘定科目残高 | 月単位の勘定科目残高 | 勘定科目ID, 会計年月 |

### システム系テーブル

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| ユーザー | システムユーザー情報 | ユーザーID |
| 監査ログ | 操作履歴・変更追跡 | 監査ログID |

---

## 4.5 TDD によるデータモデル設計

### テスト駆動でテーブルを育てる

本書では、TDD の原則をデータベース設計に応用します。

```plantuml
@startuml
title TDD によるデータモデル設計

start

:要求の明確化;
note right
  どのようなデータを
  どのように保存・取得するか
end note

:テストの作成;
note right
  リポジトリのテストを作成
  期待するデータ操作を定義
end note

:マイグレーション作成;
note right
  Flyway でテーブル作成
  必要最小限のカラム
end note

:テスト実行（Red）;
note right
  テストが失敗することを確認
end note

:実装（Green）;
note right
  テストを通す最小限の実装
end note

:リファクタリング;
note right
  テーブル構造の改善
  インデックスの追加
end note

if (次の要求がある?) then (yes)
  :要求の明確化;
else (no)
  stop
endif

@enduml
```

### 段階的なスキーマ進化

各章で以下のようにスキーマを進化させていきます。

| 章 | 追加されるテーブル | 目的 |
|----|-------------------|------|
| 第5章 | 勘定科目マスタ、勘定科目構成マスタ | マスタデータの管理 |
| 第6章 | 仕訳、仕訳明細、自動仕訳 | 取引の記録 |
| 第7章 | 日次残高、月次残高 | 残高の管理 |
| 第8章 | （ビュー）財務諸表 | 財務諸表の生成 |

---

## まとめ

本章では、データモデル設計の基礎について解説しました。

- **エンティティの識別**: マスタ、トランザクション、リソースの3種類に分類
- **リレーションシップ**: カーディナリティと依存関係の設計
- **正規化**: 第3正規形を基本とし、必要に応じて非正規化
- **全体モデル**: マスタ、仕訳、残高、財務諸表のドメイン境界
- **複式簿記**: 借方・貸方のバランスを保証するデータモデル
- **JIG-ERD**: 概要・サマリー・詳細の3レベルで可視化
- **TDD アプローチ**: テスト駆動でデータモデルを段階的に育てる

次章では、マスタデータモデル（勘定科目マスタ、勘定科目構成マスタ）の詳細について解説します。

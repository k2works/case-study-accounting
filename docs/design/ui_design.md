# UI 設計書

## 概要

本ドキュメントは、財務会計システムのユーザーインターフェース設計を定義します。OOUX（Object-Oriented UX）の原則に基づき、ドメインオブジェクトを中心とした画面設計を行います。

## 設計原則

### OOUX（Object-Oriented UX）

1. **オブジェクトの識別**: ユーザーが操作する対象を明確化
2. **関係性の定義**: オブジェクト間の関連を可視化
3. **アクションの設計**: オブジェクトに対する操作を定義
4. **属性の決定**: 各オブジェクトの表示属性を選定

### Container / View パターン

- **Container**: データ取得、状態管理、イベントハンドリング
- **View**: 表示ロジック、Props のみに依存、純粋なコンポーネント

---

## オブジェクトモデル

### 主要オブジェクト一覧

```plantuml
@startuml
title UI オブジェクトモデル

package "マスタ系" {
  object "勘定科目\n(Account)" as account {
    accountCode
    accountName
    bsplType
    elementType
  }

  object "会計期間\n(AccountingPeriod)" as period {
    year
    startDate
    endDate
    isClosed
  }

  object "課税区分\n(TaxType)" as tax {
    taxCode
    taxName
    taxRate
  }

  object "ユーザー\n(User)" as user {
    userId
    userName
    email
    roles
  }
}

package "トランザクション系" {
  object "仕訳\n(JournalEntry)" as journal {
    slipNumber
    journalDate
    description
    status
  }

  object "仕訳明細\n(JournalDetail)" as detail {
    accountCode
    debitAmount
    creditAmount
    description
  }
}

package "残高系" {
  object "日次残高\n(DailyBalance)" as daily {
    date
    accountCode
    balance
  }

  object "月次残高\n(MonthlyBalance)" as monthly {
    yearMonth
    accountCode
    balance
  }

  object "試算表\n(TrialBalance)" as trial {
    accountCode
    debitTotal
    creditTotal
  }
}

package "財務諸表系" {
  object "貸借対照表\n(BalanceSheet)" as bs {
    assets
    liabilities
    equity
  }

  object "損益計算書\n(ProfitLoss)" as pl {
    revenues
    expenses
    netIncome
  }
}

journal "1" *-- "n" detail
account "1" <-- "n" detail
account "1" <-- "n" daily
account "1" <-- "n" monthly
@enduml
```

### オブジェクトと画面パターン

| オブジェクト | Collection View | Single View | 編集モーダル | 検索モーダル |
|-------------|-----------------|-------------|--------------|--------------|
| 勘定科目 | ○ | ○ | ○ | - |
| 会計期間 | ○ | ○ | ○ | - |
| 課税区分 | ○ | ○ | ○ | - |
| ユーザー | ○ | ○ | ○ | - |
| 仕訳 | ○ | ○ | ○ (入力画面) | ○ |
| 日次残高 | ○ | - | - | ○ |
| 月次残高 | ○ | - | - | ○ |
| 試算表 | ○ | - | - | ○ |
| 貸借対照表 | - | ○ | - | ○ |
| 損益計算書 | - | ○ | - | ○ |

---

## 画面構成

### サイトレイアウト

```plantuml
@startuml
title サイトレイアウト構造

rectangle "Site Layout" {
  rectangle "Header" as header {
    rectangle "ロゴ" as logo
    rectangle "期間セレクタ" as period_select
    rectangle "ユーザー情報" as user_info
    rectangle "ログアウト" as logout
  }

  rectangle "Body" as body {
    rectangle "Sidebar" as sidebar {
      rectangle "ナビゲーション" as nav
    }
    rectangle "Main Content" as main {
      rectangle "PageHeader" as page_header
      rectangle "Content Area" as content
    }
  }

  rectangle "Footer" as footer {
    rectangle "コピーライト" as copy
    rectangle "バージョン" as version
  }
}

header -[hidden]down- body
body -[hidden]down- footer
@enduml
```

### ナビゲーション構造

```plantuml
@startuml
title ナビゲーションメニュー

rectangle "ダッシュボード" as dashboard

package "マスタ管理" {
  rectangle "勘定科目" as account_menu
  rectangle "会計期間" as period_menu
  rectangle "課税区分" as tax_menu
  rectangle "ユーザー" as user_menu #yellow
}

package "仕訳管理" {
  rectangle "仕訳入力" as journal_entry
  rectangle "仕訳一覧" as journal_list
  rectangle "仕訳承認" as journal_approval #yellow
}

package "残高照会" {
  rectangle "日次残高" as daily_balance
  rectangle "月次残高" as monthly_balance
  rectangle "試算表" as trial_balance
}

package "財務諸表" {
  rectangle "貸借対照表" as balance_sheet
  rectangle "損益計算書" as profit_loss
  rectangle "キャッシュフロー" as cash_flow
}

package "決算処理" {
  rectangle "月次締め" as monthly_closing #yellow
  rectangle "年次決算" as yearly_closing #yellow
}

package "システム" {
  rectangle "監査ログ" as audit_log
  rectangle "ダウンロード" as download
}

legend right
  | 色 | 意味 |
  | 白 | 認証のみ |
  | 黄 | 特定ロール必須 |
endlegend
@enduml
```

---

## 画面一覧

### 画面 ID 体系

| カテゴリ | ID プレフィックス | 説明 |
|---------|------------------|------|
| 認証 | AUTH | ログイン、ログアウト |
| ダッシュボード | DASH | ダッシュボード |
| マスタ管理 | MSTR | 勘定科目、会計期間、課税区分、ユーザー |
| 仕訳管理 | JRNL | 仕訳入力、一覧、承認 |
| 残高照会 | BLNC | 日次、月次、試算表 |
| 財務諸表 | STMT | BS、PL、CF |
| 決算処理 | CLSE | 月次締め、年次決算 |
| システム | SYS | 監査ログ、ダウンロード |

### 画面一覧表

| 画面 ID | 画面名 | URL | 関連 UC |
|---------|--------|-----|---------|
| AUTH-001 | ログイン | /login | UC001 |
| DASH-001 | ダッシュボード | /dashboard | - |
| MSTR-001 | 勘定科目一覧 | /master/accounts | UC002, UC003, UC004 |
| MSTR-002 | 会計期間一覧 | /master/periods | - |
| MSTR-003 | 課税区分一覧 | /master/tax-types | - |
| MSTR-004 | ユーザー一覧 | /master/users | UC018 |
| JRNL-001 | 仕訳入力 | /journal/entry | UC005 |
| JRNL-002 | 仕訳編集 | /journal/entry/:id | UC006 |
| JRNL-003 | 仕訳一覧 | /journal/list | UC007 |
| JRNL-004 | 仕訳承認 | /journal/approval | UC008 |
| BLNC-001 | 日次残高照会 | /balance/daily | UC009 |
| BLNC-002 | 月次残高照会 | /balance/monthly | UC010 |
| BLNC-003 | 試算表 | /balance/trial | UC011 |
| STMT-001 | 貸借対照表 | /statement/balance-sheet | UC012 |
| STMT-002 | 損益計算書 | /statement/profit-loss | UC013 |
| STMT-003 | キャッシュフロー計算書 | /statement/cash-flow | UC014 |
| CLSE-001 | 月次締め | /closing/monthly | UC015 |
| CLSE-002 | 年次決算 | /closing/year-end | UC016 |
| SYS-001 | 監査ログ | /system/audit-log | UC019 |
| SYS-002 | レポートダウンロード | /system/download | UC017 |

---

## 画面遷移図

### 全体遷移図

```plantuml
@startuml
title 全体画面遷移図

[*] --> ログイン

state "認証" as auth {
  ログイン --> [*] : ログイン成功
  ログイン : ID/パスワード入力
  ログイン : エラー表示
}

[*] --> ダッシュボード : 認証済み

state "メイン画面群" as main {
  ダッシュボード --> マスタ管理
  ダッシュボード --> 仕訳管理
  ダッシュボード --> 残高照会
  ダッシュボード --> 財務諸表
  ダッシュボード --> 決算処理
  ダッシュボード --> システム管理

  state "マスタ管理" as master {
    勘定科目一覧 --> 勘定科目一覧 : CRUD
    会計期間一覧 --> 会計期間一覧 : CRUD
    課税区分一覧 --> 課税区分一覧 : CRUD
    ユーザー一覧 --> ユーザー一覧 : CRUD
  }

  state "仕訳管理" as journal {
    仕訳一覧 --> 仕訳入力 : 新規作成
    仕訳一覧 --> 仕訳編集 : 編集
    仕訳入力 --> 仕訳一覧 : 保存/キャンセル
    仕訳編集 --> 仕訳一覧 : 保存/キャンセル
    仕訳一覧 --> 仕訳承認 : 承認へ
    仕訳承認 --> 仕訳一覧 : 完了
  }

  state "残高照会" as balance {
    日次残高 : 勘定科目別日次残高
    月次残高 : 勘定科目別月次残高
    試算表 : 合計残高試算表
  }

  state "財務諸表" as statement {
    貸借対照表 : B/S 表示
    損益計算書 : P/L 表示
    キャッシュフロー : C/F 表示
  }

  state "決算処理" as closing {
    月次締め : 月次決算処理
    年次決算 : 年次決算処理
  }

  state "システム管理" as system {
    監査ログ : 操作履歴
    ダウンロード : レポート出力
  }
}

main --> ログイン : ログアウト
@enduml
```

### 仕訳管理画面遷移図

```plantuml
@startuml
title 仕訳管理 画面遷移図

[*] --> 仕訳一覧

state "仕訳一覧" as list {
  list : 仕訳データ一覧表示
  list : 検索条件による絞り込み
  list : ステータスフィルタ
}

state "仕訳入力" as entry {
  entry : 伝票日付入力
  entry : 摘要入力
  entry : 仕訳明細入力
  entry : 貸借バランス検証
}

state "仕訳編集" as edit {
  edit : 既存仕訳の編集
  edit : 明細の追加/削除
}

state "仕訳承認" as approval {
  approval : 承認待ち一覧
  approval : 承認/差戻し
}

list --> entry : 新規作成ボタン
list --> edit : 行クリック（下書き）
list --> 仕訳詳細モーダル : 行クリック（承認済み）
list --> 検索モーダル : 検索ボタン
検索モーダル --> list : 検索実行

entry --> list : 保存
entry --> list : キャンセル
entry --> 勘定科目選択モーダル : 科目選択

edit --> list : 更新
edit --> list : キャンセル
edit --> 削除確認モーダル : 削除ボタン
削除確認モーダル --> list : 削除実行

list --> approval : 承認画面へ（マネージャー）
approval --> list : 完了

approval --> 承認確認モーダル : 承認ボタン
approval --> 差戻確認モーダル : 差戻ボタン
承認確認モーダル --> approval : 承認完了
差戻確認モーダル --> approval : 差戻完了
@enduml
```

### マスタ管理画面遷移図

```plantuml
@startuml
title マスタ管理 画面遷移図 (Collection/Single パターン)

[*] --> 勘定科目一覧

state "勘定科目一覧" as collection {
  collection : BS/PL タブ切替
  collection : 階層表示
  collection : 検索機能
}

state "勘定科目詳細" as single {
  single : 属性表示
  single : 関連仕訳表示
}

state "編集モーダル" as edit_modal {
  edit_modal : 新規登録
  edit_modal : 編集
}

state "削除確認モーダル" as delete_modal {
  delete_modal : 削除確認
  delete_modal : 関連チェック
}

collection --> single : 行選択
collection --> edit_modal : 新規作成ボタン

single --> edit_modal : 編集ボタン
single --> delete_modal : 削除ボタン

edit_modal --> collection : 保存完了
edit_modal --> collection : キャンセル

delete_modal --> collection : 削除完了
delete_modal --> single : キャンセル
@enduml
```

---

## 画面モックアップ

### AUTH-001: ログイン画面

```plantuml
@startsalt
title ログイン画面
{
  {^"財務会計システム"
    .
    {
      "メールアドレス" | "user@example.com    "
      "パスワード" | "********            "
    }
    .
    [     ログイン     ]
    .
    <&warning> ログインに失敗しました
  }
}
@endsalt
```

### DASH-001: ダッシュボード

```plantuml
@startsalt
title ダッシュボード
{
  {/ <&home> ダッシュボード | 2024年度 | user@example.com [ログアウト] }
  --
  {
    {
      {^"本日の仕訳"
        | **10** 件
        | 未承認: 3 件
      }
      |
      {^"当月残高"
        | 資産合計: ¥1,234,567
        | 負債合計: ¥567,890
      }
    }
    --
    {^"最近の仕訳"
      {#
        伝票番号 | 日付 | 摘要 | 金額 | 状態
        J-2024-001 | 2024/04/01 | 売上計上 | ¥100,000 | 承認済
        J-2024-002 | 2024/04/02 | 仕入計上 | ¥50,000 | 承認待ち
        J-2024-003 | 2024/04/03 | 経費精算 | ¥10,000 | 下書き
      }
    }
    --
    {^"クイックアクション"
      [ 仕訳入力 ] | [ 試算表 ] | [ 月次締め ]
    }
  }
}
@endsalt
```

### MSTR-001: 勘定科目一覧

```plantuml
@startsalt
title 勘定科目マスタ
{
  {/ <&home> ホーム | マスタ管理 | **勘定科目** }
  --
  { [+ 新規作成] | "検索: " "        " [<&magnifying-glass>] }
  --
  {+
    {/ **貸借対照表** | 損益計算書 }
    --
    {T
      + 資産
      ++ 111 現金
      ++ 112 普通預金
      ++ 121 売掛金
      ++ 131 商品
      + 負債
      ++ 211 買掛金
      ++ 221 短期借入金
      + 純資産
      ++ 311 資本金
    }
  }
  |
  {^"勘定科目詳細"
    {
      コード | **111**
      名称 | **現金**
      略称 | 現金
      カナ | ゲンキン
      BS/PL | 貸借対照表
      貸借区分 | 借方
      要素区分 | 資産
      表示順 | 100
    }
    --
    [ 編集 ] | [ 削除 ]
  }
}
@endsalt
```

### MSTR-001-M1: 勘定科目編集モーダル

```plantuml
@startsalt
title 勘定科目登録/編集
{
  {^"勘定科目登録"
    {
      "勘定科目コード<&asterisk>" | "111       "
      "勘定科目名<&asterisk>" | "現金              "
      "略称" | "現金      "
      "カナ" | "ゲンキン  "
      "BS/PL区分<&asterisk>" | ^貸借対照表(B/S)^
      "貸借区分<&asterisk>" | ^借方^
      "要素区分<&asterisk>" | ^資産^
      "表示順" | "100   "
    }
    --
    { [ キャンセル ] | [   保存   ] }
  }
}
@endsalt
```

### JRNL-001: 仕訳入力画面

```plantuml
@startsalt
title 仕訳入力
{
  {/ <&home> ホーム | 仕訳管理 | **仕訳入力** }
  --
  {^"仕訳ヘッダ"
    {
      "伝票日付<&asterisk>" | "2024/04/01  " [<&calendar>]
      "摘要<&asterisk>" | "売上計上                              "
    }
  }
  --
  {^"仕訳明細"
    {#
      . | 勘定科目 | 補助科目 | 借方金額 | 貸方金額 | 摘要
      1 | [売掛金    <&chevron-bottom>] | [       ] | "100,000   " | "          " | "A社売上      "
      2 | [売上高    <&chevron-bottom>] | [       ] | "          " | "100,000   " | "A社売上      "
      . | [+ 行追加]
    }
    --
    {
      . | . | **合計** | ¥100,000 | ¥100,000 | <&check> 貸借一致
    }
  }
  --
  { [ キャンセル ] | [ 下書き保存 ] | [ 承認申請 ] }
}
@endsalt
```

### JRNL-003: 仕訳一覧画面

```plantuml
@startsalt
title 仕訳一覧
{
  {/ <&home> ホーム | 仕訳管理 | **仕訳一覧** }
  --
  {
    [+ 新規作成] | [<&magnifying-glass> 検索] |
    ^全て^ | ^下書き^ | ^承認待ち^ | ^承認済み^
  }
  --
  {#
    <&check> | 伝票番号 | 伝票日付 | 摘要 | 金額 | 状態 | 操作
    [] | J-2024-001 | 2024/04/01 | 売上計上 | ¥100,000 | 承認済 | [詳細]
    [] | J-2024-002 | 2024/04/02 | 仕入計上 | ¥50,000 | 承認待ち | [詳細]
    [X] | J-2024-003 | 2024/04/03 | 経費精算 | ¥10,000 | 下書き | [編集][削除]
    [] | J-2024-004 | 2024/04/04 | 給与支払 | ¥300,000 | 下書き | [編集][削除]
    [] | J-2024-005 | 2024/04/05 | 家賃支払 | ¥80,000 | 承認済 | [詳細]
  }
  --
  { [< 前へ] | 1 / 10 ページ | [次へ >] }
}
@endsalt
```

### JRNL-003-M1: 仕訳検索モーダル

```plantuml
@startsalt
title 仕訳検索
{
  {^"検索条件"
    {
      {^"期間"
        "開始日" | "2024/04/01  " [<&calendar>]
        "終了日" | "2024/04/30  " [<&calendar>]
      }
      --
      {^"勘定科目"
        [勘定科目を選択...<&chevron-bottom>]
      }
      --
      {^"金額"
        "最小" | "0         " | 〜 | "最大" | "999,999,999"
      }
      --
      {^"摘要"
        "キーワード" | "                    "
      }
      --
      {^"ステータス"
        ^すべて^
      }
    }
    --
    { [条件クリア] | . | [ キャンセル ] | [   検索   ] }
  }
}
@endsalt
```

### JRNL-004: 仕訳承認画面

```plantuml
@startsalt
title 仕訳承認
{
  {/ <&home> ホーム | 仕訳管理 | **仕訳承認** }
  --
  { "承認待ち: **5** 件" | [一括承認] }
  --
  {#
    <&check> | 伝票番号 | 伝票日付 | 摘要 | 金額 | 登録者 | 操作
    [X] | J-2024-002 | 2024/04/02 | 仕入計上 | ¥50,000 | 田中太郎 | [詳細][承認][差戻]
    [X] | J-2024-006 | 2024/04/06 | 備品購入 | ¥25,000 | 鈴木花子 | [詳細][承認][差戻]
    [] | J-2024-007 | 2024/04/07 | 交通費精算 | ¥5,000 | 佐藤次郎 | [詳細][承認][差戻]
  }
}
@endsalt
```

### BLNC-001: 日次残高照会

```plantuml
@startsalt
title 日次残高照会
{
  {/ <&home> ホーム | 残高照会 | **日次残高** }
  --
  {
    "勘定科目" | [現金<&chevron-bottom>] |
    "期間" | "2024/04/01" [<&calendar>] | 〜 | "2024/04/30" [<&calendar>] |
    [検索]
  }
  --
  {^"日次残高推移: 現金"
    {#
      日付 | 借方 | 貸方 | 残高
      2024/04/01 | ¥100,000 | ¥0 | ¥100,000
      2024/04/02 | ¥50,000 | ¥20,000 | ¥130,000
      2024/04/03 | ¥0 | ¥10,000 | ¥120,000
      2024/04/04 | ¥200,000 | ¥0 | ¥320,000
      . | . | . | .
    }
  }
  --
  { [<&data-transfer-download> CSV出力] }
}
@endsalt
```

### BLNC-003: 試算表

```plantuml
@startsalt
title 試算表
{
  {/ <&home> ホーム | 残高照会 | **試算表** }
  --
  {
    "期間" | "2024/04/01" | 〜 | "2024/04/30" |
    ^合計残高試算表^ |
    [表示]
  }
  --
  {^"合計残高試算表"
    {#
      勘定科目 | 借方合計 | 貸方合計 | 借方残高 | 貸方残高
      **【資産】** | . | . | . | .
      現金 | ¥500,000 | ¥200,000 | ¥300,000 | .
      普通預金 | ¥1,000,000 | ¥400,000 | ¥600,000 | .
      売掛金 | ¥300,000 | ¥100,000 | ¥200,000 | .
      **【負債】** | . | . | . | .
      買掛金 | ¥100,000 | ¥250,000 | . | ¥150,000
      **【純資産】** | . | . | . | .
      資本金 | ¥0 | ¥500,000 | . | ¥500,000
      --
      **合計** | ¥1,900,000 | ¥1,450,000 | ¥1,100,000 | ¥650,000
    }
  }
  --
  { [<&data-transfer-download> CSV出力] | [<&print> 印刷] }
}
@endsalt
```

### STMT-001: 貸借対照表

```plantuml
@startsalt
title 貸借対照表
{
  {/ <&home> ホーム | 財務諸表 | **貸借対照表** }
  --
  {
    "基準日" | "2024/04/30" [<&calendar>] | [表示]
  }
  --
  {^"貸借対照表 2024年4月30日現在"
    {#
      **資産の部** | . | **負債の部** | .
      【流動資産】 | . | 【流動負債】 | .
      　現金預金 | ¥900,000 | 　買掛金 | ¥150,000
      　売掛金 | ¥200,000 | 　短期借入金 | ¥100,000
      　商品 | ¥150,000 | 流動負債合計 | ¥250,000
      流動資産合計 | ¥1,250,000 | 【固定負債】 | .
      【固定資産】 | . | 　長期借入金 | ¥200,000
      　建物 | ¥500,000 | 固定負債合計 | ¥200,000
      　備品 | ¥100,000 | **負債合計** | **¥450,000**
      固定資産合計 | ¥600,000 | . | .
      . | . | **純資産の部** | .
      . | . | 　資本金 | ¥500,000
      . | . | 　利益剰余金 | ¥900,000
      . | . | **純資産合計** | **¥1,400,000**
      --
      **資産合計** | **¥1,850,000** | **負債純資産合計** | **¥1,850,000**
    }
  }
  --
  { [<&data-transfer-download> Excel出力] | [<&document> PDF出力] | [<&print> 印刷] }
}
@endsalt
```

### STMT-002: 損益計算書

```plantuml
@startsalt
title 損益計算書
{
  {/ <&home> ホーム | 財務諸表 | **損益計算書** }
  --
  {
    "期間" | "2024/04/01" | 〜 | "2024/04/30" | [表示]
  }
  --
  {^"損益計算書 2024年4月1日〜2024年4月30日"
    {#
      科目 | 金額 | .
      **売上高** | . | **¥1,000,000**
      　商品売上 | ¥800,000 | .
      　サービス売上 | ¥200,000 | .
      **売上原価** | . | **¥400,000**
      　商品仕入 | ¥400,000 | .
      **売上総利益** | . | **¥600,000**
      **販売費及び一般管理費** | . | **¥300,000**
      　給与手当 | ¥200,000 | .
      　地代家賃 | ¥80,000 | .
      　通信費 | ¥20,000 | .
      **営業利益** | . | **¥300,000**
      **営業外収益** | . | **¥10,000**
      　受取利息 | ¥10,000 | .
      **営業外費用** | . | **¥5,000**
      　支払利息 | ¥5,000 | .
      **経常利益** | . | **¥305,000**
      --
      **当期純利益** | . | **¥305,000**
    }
  }
  --
  { [<&data-transfer-download> Excel出力] | [<&document> PDF出力] | [<&print> 印刷] }
}
@endsalt
```

### CLSE-001: 月次締め

```plantuml
@startsalt
title 月次締め
{
  {/ <&home> ホーム | 決算処理 | **月次締め** }
  --
  {^"月次締め処理"
    {
      "対象月" | ^2024年4月^
    }
    --
    {^"締め前チェック"
      {#
        チェック項目 | 状態 | 詳細
        未承認仕訳 | <&check> OK | 0 件
        貸借不一致 | <&check> OK | 0 件
        仮勘定残高 | <&warning> 警告 | ¥10,000
      }
    }
    --
    {
      "現在のステータス: **未締め**"
    }
    --
    { [プレビュー] | [   月次締め実行   ] }
  }
}
@endsalt
```

### SYS-001: 監査ログ

```plantuml
@startsalt
title 監査ログ
{
  {/ <&home> ホーム | システム | **監査ログ** }
  --
  {
    "期間" | "2024/04/01" | 〜 | "2024/04/30" |
    "操作種別" | ^すべて^ |
    "ユーザー" | ^すべて^ |
    [検索]
  }
  --
  {#
    日時 | ユーザー | 操作 | 対象 | 詳細
    2024/04/05 10:30:15 | 田中太郎 | 作成 | 仕訳 | J-2024-010
    2024/04/05 10:25:00 | 鈴木花子 | 承認 | 仕訳 | J-2024-009
    2024/04/05 10:20:30 | 佐藤次郎 | 更新 | 勘定科目 | 111
    2024/04/05 10:15:45 | 田中太郎 | ログイン | - | -
  }
  --
  { [< 前へ] | 1 / 50 ページ | [次へ >] | [<&data-transfer-download> CSV出力] }
}
@endsalt
```

---

## 共通コンポーネント

### 共通モーダル

| コンポーネント | 用途 | サイズ |
|---------------|------|--------|
| BaseModal | 基本モーダル | small/medium/large/full |
| EditModal | 編集モーダル | medium |
| SearchModal | 検索モーダル | large |
| SelectModal | 選択モーダル | medium |
| ConfirmModal | 確認モーダル | small |

### フォームコンポーネント

| コンポーネント | 用途 |
|---------------|------|
| FormField | ラベル付きフォームフィールド |
| DatePicker | 日付選択 |
| DateRangePicker | 期間選択 |
| MoneyInput | 金額入力 |
| AccountSelector | 勘定科目選択 |

### 表示コンポーネント

| コンポーネント | 用途 |
|---------------|------|
| MoneyDisplay | 金額表示（3桁カンマ） |
| DateDisplay | 日付表示 |
| StatusBadge | ステータスバッジ |
| PageHeader | ページヘッダー（パンくず付き） |
| Loading | ローディング表示 |
| ErrorMessage | エラーメッセージ |
| MessageDisplay | 通知メッセージ |

### ボタンコンポーネント

| バリアント | 用途 |
|-----------|------|
| primary | 主要アクション（保存、実行） |
| secondary | 副次アクション（キャンセル） |
| danger | 危険なアクション（削除） |
| warning | 警告アクション（差戻し） |
| text | テキストリンク風 |

---

## アクセシビリティ

### キーボード操作

| 操作 | キー |
|------|------|
| モーダルを閉じる | Escape |
| フォーム送信 | Enter |
| 次のフィールドへ | Tab |
| 前のフィールドへ | Shift + Tab |

### ARIA 対応

- モーダルには `role="dialog"` と `aria-modal="true"` を設定
- フォームフィールドには適切な `label` と `aria-describedby` を設定
- エラーメッセージは `aria-live="polite"` で通知
- ローディング状態は `aria-busy="true"` で通知

### 色覚対応

- 色だけに依存しない情報伝達（アイコン、テキストを併用）
- コントラスト比 4.5:1 以上の確保
- フォーカス時の視覚的フィードバック

---

## レスポンシブ対応

### ブレークポイント

| サイズ | 幅 | 用途 |
|--------|-----|------|
| small | < 768px | スマートフォン |
| medium | 768px - 1024px | タブレット |
| large | > 1024px | デスクトップ |

### 対応方針

- 本システムはデスクトップファーストで設計
- タブレット以上のサイズに最適化
- スマートフォンは閲覧のみ対応（入力は非推奨）

---

## 画面遷移とユースケースの対応

| ユースケース | 開始画面 | 終了画面 |
|-------------|----------|----------|
| UC001: ログイン | AUTH-001 | DASH-001 |
| UC002: 勘定科目登録 | MSTR-001 | MSTR-001 |
| UC003: 勘定科目更新 | MSTR-001 | MSTR-001 |
| UC004: 勘定科目削除 | MSTR-001 | MSTR-001 |
| UC005: 仕訳入力 | JRNL-001 | JRNL-003 |
| UC006: 仕訳修正 | JRNL-002 | JRNL-003 |
| UC007: 仕訳検索 | JRNL-003 | JRNL-003 |
| UC008: 仕訳承認 | JRNL-004 | JRNL-004 |
| UC009: 日次残高照会 | BLNC-001 | BLNC-001 |
| UC010: 月次残高照会 | BLNC-002 | BLNC-002 |
| UC011: 試算表表示 | BLNC-003 | BLNC-003 |
| UC012: 貸借対照表表示 | STMT-001 | STMT-001 |
| UC013: 損益計算書表示 | STMT-002 | STMT-002 |
| UC014: CF計算書表示 | STMT-003 | STMT-003 |
| UC015: 月次締め | CLSE-001 | CLSE-001 |
| UC016: 年次決算 | CLSE-002 | CLSE-002 |
| UC017: レポートDL | SYS-002 | SYS-002 |
| UC018: ユーザー管理 | MSTR-004 | MSTR-004 |
| UC019: 監査ログ | SYS-001 | SYS-001 |

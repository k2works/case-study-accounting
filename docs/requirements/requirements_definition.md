# 要件定義 - 財務会計システム

## システム価値

### システムコンテキスト

```plantuml
@startuml

title システムコンテキスト図 - 財務会計システム

left to right direction

actor 経理担当者
actor 経営者
actor 管理者

agent 外部会計システム
agent 銀行システム

usecase 財務会計システム
note top of 財務会計システム
  企業の財務活動を正確に記録・管理し、
  経営判断に必要な財務情報を提供する。
  複式簿記に基づく仕訳入力から
  財務諸表の自動生成までを支援。
end note

:経理担当者: -- (財務会計システム)
:経営者: -- (財務会計システム)
:管理者: -- (財務会計システム)
(財務会計システム) -- 外部会計システム
(財務会計システム) -- 銀行システム

@enduml
```

### 要求モデル

```plantuml
@startuml

title 要求モデル図 - 財務会計システム

left to right direction

actor 経理担当者
note "日々の取引を正確に記録したい" as keiri_r1
note "仕訳入力の手間を軽減したい" as keiri_r2
note "残高をリアルタイムに確認したい" as keiri_r3
note "消費税計算を自動化したい" as keiri_r4
note as keiri_dr1 #Turquoise
  仕訳入力画面で借方・貸方を
  バランスチェック付きで入力できること
  自動仕訳テンプレートを設定できること
  課税区分に基づく消費税自動計算
end note
:経理担当者: -- keiri_r1
:経理担当者: -- keiri_r2
:経理担当者: -- keiri_r3
:経理担当者: -- keiri_r4
keiri_r1 -- keiri_dr1
keiri_r2 -- keiri_dr1
keiri_r3 -- keiri_dr1
keiri_r4 -- keiri_dr1

actor 経営者
note "財務状況を把握したい" as mgr_r1
note "収益性を分析したい" as mgr_r2
note "資金繰りを確認したい" as mgr_r3
note as mgr_dr1 #Turquoise
  貸借対照表・損益計算書を
  リアルタイムに表示できること
  財務指標の分析機能を提供
  キャッシュフロー計算書の生成
end note
:経営者: -- mgr_r1
:経営者: -- mgr_r2
:経営者: -- mgr_r3
mgr_r1 -- mgr_dr1
mgr_r2 -- mgr_dr1
mgr_r3 -- mgr_dr1

actor 管理者
note "ユーザーを管理したい" as admin_r1
note "操作履歴を確認したい" as admin_r2
note "承認ワークフローを管理したい" as admin_r3
note as admin_dr1 #Turquoise
  ユーザー CRUD 機能
  監査ログの照会機能
  仕訳承認・月次締め権限の管理
end note
:管理者: -- admin_r1
:管理者: -- admin_r2
:管理者: -- admin_r3
admin_r1 -- admin_dr1
admin_r2 -- admin_dr1
admin_r3 -- admin_dr1

@enduml
```

## システム外部環境

### ビジネスコンテキスト

```plantuml
@startuml

title ビジネスコンテキスト図 - 財務会計システム

left to right direction

actor 取引先
actor 税理士

node 企業 {
  rectangle 経理部 {
    actor 経理担当者
    actor 経理マネージャー
  }

  rectangle 経営層 {
    actor 経営者
  }

  rectangle 情報システム部 {
    actor システム管理者
  }

  usecase 日次経理業務
  usecase 月次決算業務
  usecase 年次決算業務
  usecase 財務分析業務

  artifact 仕訳帳
  artifact 総勘定元帳
  artifact 財務諸表
}

node 外部機関 {
  agent 銀行
  agent 税務署
}

:取引先: -- (日次経理業務)
:税理士: -- (年次決算業務)

(日次経理業務) -- :経理担当者:
(月次決算業務) -- :経理マネージャー:
(年次決算業務) -- :経理マネージャー:
(財務分析業務) -- :経営者:

(日次経理業務) -- 仕訳帳
(月次決算業務) -- 総勘定元帳
(年次決算業務) -- 財務諸表

(日次経理業務) -- 銀行
(年次決算業務) -- 税務署

@enduml
```

### ビジネスユースケース

#### 日次経理業務

```plantuml
@startuml

title ビジネスユースケース図 - 日次経理業務

left to right direction

actor 経理担当者
actor 経理マネージャー

agent 経理部

usecase "取引を記録する" as uc_01
usecase "仕訳を入力する" as uc_02
usecase "仕訳を承認する" as uc_03
usecase "日次残高を確認する" as uc_04

artifact 取引証憑
artifact 仕訳伝票
artifact 勘定元帳

:経理担当者: -- (uc_01)
:経理担当者: -- (uc_02)
:経理マネージャー: -- (uc_03)
:経理担当者: -- (uc_04)

(uc_01) -- 経理部
(uc_02) -- 経理部
(uc_03) -- 経理部
(uc_04) -- 経理部

(uc_01) -- 取引証憑
(uc_02) -- 仕訳伝票
(uc_02) -- 取引証憑
(uc_03) -- 仕訳伝票
(uc_04) -- 勘定元帳

@enduml
```

#### 月次決算業務

```plantuml
@startuml

title ビジネスユースケース図 - 月次決算業務

left to right direction

actor 経理担当者
actor 経理マネージャー

agent 経理部

usecase "月次残高を集計する" as uc_01
usecase "試算表を作成する" as uc_02
usecase "月次締めを実行する" as uc_03
usecase "月次レポートを出力する" as uc_04

artifact 月次残高表
artifact 合計残高試算表
artifact 月次報告書

:経理担当者: -- (uc_01)
:経理担当者: -- (uc_02)
:経理マネージャー: -- (uc_03)
:経理マネージャー: -- (uc_04)

(uc_01) -- 経理部
(uc_02) -- 経理部
(uc_03) -- 経理部
(uc_04) -- 経理部

(uc_01) -- 月次残高表
(uc_02) -- 合計残高試算表
(uc_04) -- 月次報告書

@enduml
```

#### 年次決算業務

```plantuml
@startuml

title ビジネスユースケース図 - 年次決算業務

left to right direction

actor 経理マネージャー
actor 経営者
actor 税理士

agent 経理部

usecase "決算整理仕訳を入力する" as uc_01
usecase "年次決算を実行する" as uc_02
usecase "財務諸表を作成する" as uc_03
usecase "財務諸表を承認する" as uc_04

artifact 決算整理仕訳
artifact 貸借対照表
artifact 損益計算書
artifact キャッシュフロー計算書

:経理マネージャー: -- (uc_01)
:経理マネージャー: -- (uc_02)
:経理マネージャー: -- (uc_03)
:経営者: -- (uc_04)
:税理士: -- (uc_03)

(uc_01) -- 経理部
(uc_02) -- 経理部
(uc_03) -- 経理部
(uc_04) -- 経理部

(uc_01) -- 決算整理仕訳
(uc_03) -- 貸借対照表
(uc_03) -- 損益計算書
(uc_03) -- キャッシュフロー計算書

@enduml
```

### 業務フロー

#### 日次経理業務の業務フロー

```plantuml
@startuml

title 業務フロー図 - 日次経理業務

|経理担当者|
partition 取引記録 {
  :取引証憑を収集;
  :取引内容を確認;
  :仕訳を入力;
}

partition 仕訳登録 {
  :借方科目を選択;
  :貸方科目を選択;
  :金額を入力;
  :消費税区分を設定;
  :貸借バランスを確認;
  :仕訳を保存;
}

|経理マネージャー|
partition 承認処理 {
  :承認待ち仕訳を確認;
  split
    :仕訳を承認;
  split again
    -> 差戻しの場合;
    :仕訳を差戻し;
    stop
  end split
  :仕訳を確定;
}

|経理担当者|
partition 残高確認 {
  :日次残高を照会;
  :勘定別明細を確認;
}
stop

@enduml
```

#### 月次決算業務の業務フロー

```plantuml
@startuml

title 業務フロー図 - 月次決算業務

|経理担当者|
start
:未承認仕訳を確認;
:月次残高を集計;
:試算表を作成;

|経理マネージャー|
:試算表を確認;
if (残高に問題あり?) then (yes)
  :修正仕訳を入力;
  :再集計;
else (no)
endif

:月次締めを実行;
:月次レポートを出力;
stop

@enduml
```

### 利用シーン

#### 日次経理業務の利用シーン

```plantuml
@startuml

title 利用シーン図 - 日次経理業務

left to right direction

actor 経理担当者
actor 経理マネージャー

frame "日々の取引記録"
note right of "日々の取引記録"
  毎日発生する売上、仕入、経費などの
  取引を仕訳として記録する場面。
  取引証憑を基に正確な仕訳入力が必要。
  消費税の自動計算により入力効率を向上。
end note

frame "仕訳の承認・確定"
note right of "仕訳の承認・確定"
  経理マネージャーが入力された仕訳を
  チェックし承認する場面。
  不適切な仕訳は差戻し可能。
  承認後は元帳に転記される。
end note

usecase "仕訳入力"
usecase "自動仕訳生成"
usecase "仕訳承認"
usecase "日次残高照会"

:経理担当者: -- "日々の取引記録"
:経理マネージャー: -- "仕訳の承認・確定"
"日々の取引記録" -- (仕訳入力)
"日々の取引記録" -- (自動仕訳生成)
"仕訳の承認・確定" -- (仕訳承認)
"仕訳の承認・確定" -- (日次残高照会)

@enduml
```

#### 財務分析の利用シーン

```plantuml
@startuml

title 利用シーン図 - 財務分析

left to right direction

actor 経営者
actor 経理マネージャー

frame "経営判断のための財務分析"
note right of "経営判断のための財務分析"
  経営者が会社の財務状況を把握し、
  経営判断を行う場面。
  貸借対照表で資産状況を確認し、
  損益計算書で収益性を分析する。
  キャッシュフローで資金繰りを確認。
end note

usecase "貸借対照表表示"
usecase "損益計算書表示"
usecase "キャッシュフロー計算書表示"
usecase "財務指標分析"

:経営者: -- "経営判断のための財務分析"
:経理マネージャー: -- "経営判断のための財務分析"
"経営判断のための財務分析" -- (貸借対照表表示)
"経営判断のための財務分析" -- (損益計算書表示)
"経営判断のための財務分析" -- (キャッシュフロー計算書表示)
"経営判断のための財務分析" -- (財務指標分析)

@enduml
```

### バリエーション・条件

#### 勘定科目種別

| 種別 | 説明 |
|------|------|
| 資産 (Asset) | 会社が所有する財産（現金、売掛金、棚卸資産など） |
| 負債 (Liability) | 会社が負っている債務（買掛金、借入金、未払金など） |
| 純資産 (Equity) | 資産から負債を引いた正味財産（資本金、利益剰余金） |
| 収益 (Revenue) | 事業活動による収入（売上高、受取利息など） |
| 費用 (Expense) | 事業活動による支出（仕入高、給与、家賃など） |

#### 仕訳ステータス

| ステータス | 説明 |
|----------|------|
| 下書き | 入力中の仕訳。編集・削除可能 |
| 承認待ち | 承認を待っている仕訳。経理担当者による編集可能 |
| 承認済み | マネージャーが承認した仕訳。元帳に転記前 |
| 確定 | 元帳に転記された仕訳。編集不可 |
| 差戻し | 承認者により差し戻された仕訳。再編集が必要 |

#### 課税区分

| 区分 | 説明 |
|------|------|
| 課税 | 消費税の対象となる取引 |
| 非課税 | 消費税が非課税となる取引 |
| 不課税 | 消費税の対象外となる取引 |
| 免税 | 輸出取引など消費税が免除される取引 |

#### 会計期間

| 種類 | 説明 |
|------|------|
| 日次 | 日単位での残高管理・照会 |
| 月次 | 月単位での締め処理・報告 |
| 四半期 | 四半期決算報告 |
| 年次 | 年度決算・財務諸表作成 |

## システム境界

### ユースケース複合図

#### マスタ管理

```plantuml
@startuml

title ユースケース複合図 - マスタ管理

left to right direction

actor 経理担当者 as user
actor 管理者 as admin

frame "勘定科目の設定" as f01
usecase "勘定科目管理" as UC1
usecase "勘定科目構成管理" as UC2
boundary "勘定科目画面" as b01
boundary "勘定科目構成画面" as b02
entity "勘定科目" as e01
entity "勘定科目構成" as e02
control "科目コード重複チェック" as c01
control "親子関係整合性チェック" as c02

user -- f01
admin -- f01
f01 -- UC1
f01 -- UC2

b01 -- UC1
UC1 -- e01
UC1 -- c01

b02 -- UC2
UC2 -- e02
UC2 -- c02

frame "税務設定" as f02
usecase "課税マスタ管理" as UC3
usecase "会計期間管理" as UC4
boundary "課税マスタ画面" as b03
boundary "会計期間画面" as b04
entity "課税取引マスタ" as e03
entity "会計期間" as e04
control "税率有効期間チェック" as c03

admin -- f02
f02 -- UC3
f02 -- UC4

b03 -- UC3
UC3 -- e03
UC3 -- c03

b04 -- UC4
UC4 -- e04

@enduml
```

#### 仕訳管理

```plantuml
@startuml

title ユースケース複合図 - 仕訳管理

left to right direction

actor 経理担当者 as user
actor 経理マネージャー as manager

frame "日次仕訳入力" as f01
usecase "仕訳入力" as UC1
usecase "自動仕訳生成" as UC2
usecase "CSV インポート" as UC3
boundary "仕訳入力画面" as b01
boundary "CSV インポート画面" as b02
entity "仕訳" as e01
entity "仕訳明細" as e02
entity "自動仕訳設定" as e03
control "貸借バランスチェック" as c01
control "消費税自動計算" as c02

user -- f01
f01 -- UC1
f01 -- UC2
f01 -- UC3

b01 -- UC1
UC1 -- e01
UC1 -- e02
UC1 -- c01
UC1 -- c02

UC2 -- e03
UC2 -- e01

b02 -- UC3
UC3 -- e01

frame "仕訳承認" as f02
usecase "仕訳一覧表示" as UC4
usecase "仕訳承認" as UC5
usecase "仕訳差戻し" as UC6
boundary "承認待ち一覧画面" as b03
boundary "仕訳詳細画面" as b04
control "承認権限チェック" as c03

manager -- f02
f02 -- UC4
f02 -- UC5
f02 -- UC6

b03 -- UC4
b04 -- UC5
UC5 -- e01
UC5 -- c03
UC6 -- e01

@enduml
```

#### 残高管理

```plantuml
@startuml

title ユースケース複合図 - 残高管理

left to right direction

actor 経理担当者 as user
actor 経理マネージャー as manager

frame "残高照会" as f01
usecase "日次残高照会" as UC1
usecase "月次残高照会" as UC2
usecase "試算表表示" as UC3
boundary "日次残高画面" as b01
boundary "月次残高画面" as b02
boundary "試算表画面" as b03
entity "日次残高" as e01
entity "月次残高" as e02
entity "勘定科目" as e03

user -- f01
manager -- f01
f01 -- UC1
f01 -- UC2
f01 -- UC3

b01 -- UC1
UC1 -- e01
UC1 -- e03

b02 -- UC2
UC2 -- e02
UC2 -- e03

b03 -- UC3
UC3 -- e01
UC3 -- e02
UC3 -- e03

frame "元帳照会" as f02
usecase "総勘定元帳照会" as UC4
usecase "補助元帳照会" as UC5
boundary "総勘定元帳画面" as b04
boundary "補助元帳画面" as b05
entity "総勘定元帳" as e04
entity "補助元帳" as e05

user -- f02
f02 -- UC4
f02 -- UC5

b04 -- UC4
UC4 -- e04

b05 -- UC5
UC5 -- e05

@enduml
```

#### 財務諸表

```plantuml
@startuml

title ユースケース複合図 - 財務諸表

left to right direction

actor 経理マネージャー as manager
actor 経営者 as exec

frame "財務諸表表示" as f01
usecase "貸借対照表表示" as UC1
usecase "損益計算書表示" as UC2
usecase "キャッシュフロー計算書表示" as UC3
boundary "貸借対照表画面" as b01
boundary "損益計算書画面" as b02
boundary "キャッシュフロー計算書画面" as b03
entity "貸借対照表" as e01
entity "損益計算書" as e02
entity "キャッシュフロー計算書" as e03
control "勘定式/報告式切替" as c01
control "期間比較表示" as c02

manager -- f01
exec -- f01
f01 -- UC1
f01 -- UC2
f01 -- UC3

b01 -- UC1
UC1 -- e01
UC1 -- c01
UC1 -- c02

b02 -- UC2
UC2 -- e02
UC2 -- c02

b03 -- UC3
UC3 -- e03

frame "帳票出力" as f02
usecase "PDF 出力" as UC4
usecase "Excel 出力" as UC5
usecase "CSV 出力" as UC6
boundary "ダウンロード画面" as b04
interface "非同期ダウンロード" as i01

manager -- f02
f02 -- UC4
f02 -- UC5
f02 -- UC6

b04 -- UC4
b04 -- UC5
b04 -- UC6
UC4 -- i01
UC5 -- i01
UC6 -- i01

@enduml
```

#### 決算処理

```plantuml
@startuml

title ユースケース複合図 - 決算処理

left to right direction

actor 経理マネージャー as manager

frame "月次決算" as f01
usecase "月次締め実行" as UC1
usecase "月次締め解除" as UC2
boundary "月次締め画面" as b01
entity "会計期間" as e01
entity "月次残高" as e02
control "未承認仕訳チェック" as c01
control "締め済み期間チェック" as c02

manager -- f01
f01 -- UC1
f01 -- UC2

b01 -- UC1
b01 -- UC2
UC1 -- e01
UC1 -- e02
UC1 -- c01
UC2 -- c02

frame "年次決算" as f02
usecase "決算整理仕訳入力" as UC3
usecase "年次決算実行" as UC4
usecase "繰越処理" as UC5
boundary "決算処理画面" as b02
entity "仕訳" as e03
entity "繰越残高" as e04
control "決算整理仕訳チェック" as c03

manager -- f02
f02 -- UC3
f02 -- UC4
f02 -- UC5

b02 -- UC3
b02 -- UC4
UC3 -- e03
UC4 -- e01
UC5 -- e04
UC4 -- c03

@enduml
```

#### 認証・ユーザー管理

```plantuml
@startuml

title ユースケース複合図 - 認証・ユーザー管理

left to right direction

actor ユーザー as user
actor 管理者 as admin

frame "認証" as f01
usecase "ログイン" as UC1
usecase "ログアウト" as UC2
usecase "パスワード変更" as UC3
boundary "ログイン画面" as b01
entity "ユーザー" as e01
control "認証チェック" as c01
interface "JWT トークン発行" as i01

user -- f01
f01 -- UC1
f01 -- UC2
f01 -- UC3

b01 -- UC1
UC1 -- e01
UC1 -- c01
UC1 -- i01

frame "ユーザー管理" as f02
usecase "ユーザー一覧表示" as UC4
usecase "ユーザー登録" as UC5
usecase "ユーザー更新" as UC6
usecase "ユーザー削除" as UC7
boundary "ユーザー管理画面" as b02
entity "ロール" as e02
control "権限チェック" as c02

admin -- f02
f02 -- UC4
f02 -- UC5
f02 -- UC6
f02 -- UC7

b02 -- UC4
b02 -- UC5
UC5 -- e01
UC5 -- e02
UC5 -- c02
UC6 -- e01
UC7 -- e01

@enduml
```

#### 監査・履歴

```plantuml
@startuml

title ユースケース複合図 - 監査・履歴

left to right direction

actor 管理者 as admin

frame "監査ログ照会" as f01
usecase "操作履歴照会" as UC1
usecase "仕訳変更履歴照会" as UC2
usecase "ログイン履歴照会" as UC3
boundary "監査ログ画面" as b01
entity "操作履歴" as e01
entity "仕訳変更履歴" as e02
entity "ログイン履歴" as e03
control "期間指定検索" as c01

admin -- f01
f01 -- UC1
f01 -- UC2
f01 -- UC3

b01 -- UC1
b01 -- UC2
b01 -- UC3
UC1 -- e01
UC1 -- c01
UC2 -- e02
UC3 -- e03

@enduml
```

## システム

### 情報モデル

```plantuml
@startuml

title 情報モデル図 - 財務会計システム

left to right direction

' マスタ系
entity 勘定科目
entity 勘定科目構成
entity 課税取引マスタ
entity 自動仕訳設定
entity 会計期間

' トランザクション系
entity 仕訳
entity 仕訳明細
entity 総勘定元帳
entity 補助元帳

' 残高系
entity 日次残高
entity 月次残高
entity 繰越残高

' 財務諸表系
entity 貸借対照表
entity 損益計算書
entity キャッシュフロー計算書

' ユーザー系
entity ユーザー
entity ロール

' 監査系
entity 操作履歴
entity 仕訳変更履歴
entity ログイン履歴

' マスタ関連
勘定科目 -- 勘定科目構成
勘定科目 -- 課税取引マスタ
勘定科目 -- 自動仕訳設定

' トランザクション関連
仕訳 -- 仕訳明細
仕訳明細 -- 勘定科目
仕訳 -- 会計期間
仕訳 -- 総勘定元帳
総勘定元帳 -- 補助元帳

' 残高関連
勘定科目 -- 日次残高
勘定科目 -- 月次残高
会計期間 -- 日次残高
会計期間 -- 月次残高
会計期間 -- 繰越残高

' 財務諸表関連
月次残高 -- 貸借対照表
月次残高 -- 損益計算書
月次残高 -- キャッシュフロー計算書

' ユーザー関連
ユーザー -- ロール
ユーザー -- 仕訳

' 監査関連
ユーザー -- 操作履歴
仕訳 -- 仕訳変更履歴
ユーザー -- ログイン履歴

@enduml
```

### 状態モデル

#### 仕訳の状態遷移

```plantuml
@startuml
title 仕訳の状態遷移図

[*] --> 下書き : 仕訳入力開始

state 下書き {
  [*] --> 入力中
  入力中 --> 入力中 : 編集
}

下書き --> 承認待ち : 承認申請
承認待ち --> 下書き : 取り下げ
承認待ち --> 承認済み : 承認
承認待ち --> 差戻し : 差戻し

差戻し --> 下書き : 再編集
差戻し --> 取消 : 取消

承認済み --> 確定 : 元帳転記
確定 --> 取消 : 取消仕訳作成

下書き --> 削除 : 削除
削除 --> [*]
取消 --> [*]
確定 --> [*]

@enduml
```

#### 会計期間の状態遷移

```plantuml
@startuml
title 会計期間の状態遷移図

[*] --> オープン : 期間開始

オープン --> 仮締め : 月次仮締め
仮締め --> オープン : 仮締め解除
仮締め --> 締め済み : 月次締め確定

締め済み --> 仮締め : 締め解除（特別権限）

state 締め済み {
  [*] --> 通常締め
  通常締め --> 決算締め : 年次決算実行
}

締め済み --> [*] : 翌期繰越完了

@enduml
```

#### ユーザーの状態遷移

```plantuml
@startuml
title ユーザーの状態遷移図

[*] --> 仮登録 : ユーザー作成

仮登録 --> 有効 : 初回ログイン完了
有効 --> ロック : ログイン失敗超過
ロック --> 有効 : ロック解除
有効 --> 無効 : 無効化
無効 --> 有効 : 再有効化
無効 --> 削除 : 完全削除

削除 --> [*]

@enduml
```

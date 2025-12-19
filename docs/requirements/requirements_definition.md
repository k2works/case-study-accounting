# 要件定義 - 財務会計システム

## システム価値

### システムコンテキスト

```plantuml
@startuml

title システムコンテキスト図 - 財務会計システム

left to right direction

actor 経理担当者
actor 経理責任者
actor 経営者
actor システム管理者

agent 銀行システム
agent 税務申告システム
agent 外部監査システム

usecase 財務会計システム
note top of 財務会計システム
  企業の財務活動を正確に記録・管理し、
  財務諸表の生成と経営判断を支援する。
  複式簿記に基づく仕訳管理から
  貸借対照表・損益計算書の作成まで
  一元的に管理する。
end note

:経理担当者: -- (財務会計システム)
:経理責任者: -- (財務会計システム)
:経営者: -- (財務会計システム)
:システム管理者: -- (財務会計システム)
(財務会計システム) -- 銀行システム
(財務会計システム) -- 税務申告システム
(財務会計システム) -- 外部監査システム

@enduml
```

### 要求モデル

```plantuml
@startuml

title 要求モデル図 - 財務会計システム

left to right direction

actor 経理担当者
note "日々の取引を正確に記録したい" as r1_1
note "仕訳入力を効率化したい" as r1_2
note "元帳・残高を照会したい" as r1_3
note as dr1 #Turquoise
  仕訳入力画面で借方・貸方を入力し、
  貸借一致を自動検証できること。
  定型取引は自動仕訳で効率化。
end note
:経理担当者: -- r1_1
:経理担当者: -- r1_2
:経理担当者: -- r1_3
r1_1 -- dr1
r1_2 -- dr1
r1_3 -- dr1

actor 経理責任者
note "仕訳の承認・確定を管理したい" as r2_1
note "勘定科目を体系的に管理したい" as r2_2
note "月次・年次の締め処理を行いたい" as r2_3
note as dr2 #Turquoise
  仕訳承認ワークフローで
  適切な承認プロセスを実現。
  勘定科目マスタで階層構造を管理。
end note
:経理責任者: -- r2_1
:経理責任者: -- r2_2
:経理責任者: -- r2_3
r2_1 -- dr2
r2_2 -- dr2
r2_3 -- dr2

actor 経営者
note "財務状況を把握したい" as r3_1
note "経営判断に必要な指標を見たい" as r3_2
note "財務諸表を確認したい" as r3_3
note as dr3 #Turquoise
  貸借対照表・損益計算書を
  いつでも参照可能。
  財務分析機能で各種指標を算出。
end note
:経営者: -- r3_1
:経営者: -- r3_2
:経営者: -- r3_3
r3_1 -- dr3
r3_2 -- dr3
r3_3 -- dr3

actor システム管理者
note "ユーザーとアクセス権限を管理したい" as r4_1
note "監査ログを確認したい" as r4_2
note "システムの安定稼働を維持したい" as r4_3
note as dr4 #Turquoise
  ロールベースのアクセス制御で
  権限を適切に管理。
  全操作の監査ログを保持。
end note
:システム管理者: -- r4_1
:システム管理者: -- r4_2
:システム管理者: -- r4_3
r4_1 -- dr4
r4_2 -- dr4
r4_3 -- dr4

@enduml
```

## システム外部環境

### ビジネスコンテキスト

```plantuml
@startuml

title ビジネスコンテキスト図 - 財務会計システム

left to right direction

actor 取引先
actor 株主
actor 税務署

node 企業 {
  rectangle 経理部門 {
    actor 経理担当者
    actor 経理責任者
  }

  rectangle 経営層 {
    actor 経営者
  }

  rectangle 情報システム部門 {
    actor システム管理者
  }

  usecase 日常経理業務
  usecase 月次決算業務
  usecase 年次決算業務
  usecase 財務分析業務

  artifact 仕訳帳
  artifact 総勘定元帳
  artifact 財務諸表
}

node 外部機関 {
  agent 銀行システム
  agent 税務申告システム
  agent 外部監査システム
}

:取引先: -- (日常経理業務)
:株主: -- (財務諸表)

(日常経理業務) -- :経理担当者:
(月次決算業務) -- :経理責任者:
(年次決算業務) -- :経理責任者:
(財務分析業務) -- :経営者:

(日常経理業務) -- 仕訳帳
(月次決算業務) -- 総勘定元帳
(年次決算業務) -- 財務諸表

(日常経理業務) -- 銀行システム
(年次決算業務) -- 税務申告システム
(年次決算業務) -- 外部監査システム

@enduml
```

### ビジネスユースケース

#### 日常経理業務

```plantuml
@startuml

title ビジネスユースケース図 - 日常経理業務

left to right direction

actor 経理担当者
actor 経理責任者

agent 経理部門

usecase "取引の仕訳入力" as buc_01
usecase "自動仕訳の生成" as buc_02
usecase "仕訳の承認" as buc_03
usecase "元帳への転記" as buc_04
usecase "残高の確認" as buc_05

artifact 証憑書類 as af_01
artifact 仕訳帳 as af_02
artifact 総勘定元帳 as af_03

:経理担当者: -- (buc_01)
:経理担当者: -- (buc_02)
:経理担当者: -- (buc_05)
:経理責任者: -- (buc_03)
:経理責任者: -- (buc_04)

(buc_01) -- 経理部門
(buc_02) -- 経理部門
(buc_03) -- 経理部門
(buc_04) -- 経理部門
(buc_05) -- 経理部門

(buc_01) -- af_01
(buc_01) -- af_02
(buc_02) -- af_02
(buc_04) -- af_03

@enduml
```

#### 決算業務

```plantuml
@startuml

title ビジネスユースケース図 - 決算業務

left to right direction

actor 経理責任者
actor 経営者

agent 経理部門

usecase "月次締め処理" as buc_01
usecase "試算表の作成" as buc_02
usecase "決算整理仕訳" as buc_03
usecase "財務諸表の作成" as buc_04
usecase "財務分析" as buc_05

artifact 試算表 as af_01
artifact 貸借対照表 as af_02
artifact 損益計算書 as af_03

:経理責任者: -- (buc_01)
:経理責任者: -- (buc_02)
:経理責任者: -- (buc_03)
:経理責任者: -- (buc_04)
:経営者: -- (buc_05)

(buc_01) -- 経理部門
(buc_02) -- 経理部門
(buc_03) -- 経理部門
(buc_04) -- 経理部門
(buc_05) -- 経理部門

(buc_02) -- af_01
(buc_04) -- af_02
(buc_04) -- af_03

@enduml
```

### 業務フロー

#### 仕訳入力から元帳転記までの業務フロー

```plantuml
@startuml

title 業務フロー図 - 仕訳入力から元帳転記

|経理担当者|
start
partition 取引発生 {
  :取引内容を確認;
  :証憑書類を準備;
}

partition 仕訳入力 {
  :仕訳日付を入力;
  :借方科目・金額を入力;
  :貸方科目・金額を入力;
  :摘要を入力;
  if (貸借一致?) then (yes)
    :仕訳を保存;
  else (no)
    :エラー表示;
    stop
  endif
}

|経理責任者|
partition 仕訳承認 {
  :仕訳内容を確認;
  if (承認可能?) then (yes)
    :仕訳を承認;
  else (no)
    :差し戻し;
    stop
  endif
}

partition 仕訳確定 {
  :仕訳を確定;
  :元帳に転記;
}

|経理担当者|
partition 残高確認 {
  :総勘定元帳を照会;
  :残高を確認;
}
stop

@enduml
```

#### 月次決算業務フロー

```plantuml
@startuml

title 業務フロー図 - 月次決算

|経理責任者|
start
partition 月次準備 {
  :当月仕訳の確認;
  :未処理仕訳の確認;
}

partition 月次締め {
  :決算整理仕訳の入力;
  :月次締め処理;
  if (貸借一致?) then (yes)
    :締め処理完了;
  else (no)
    :差異調整;
    stop
  endif
}

partition 試算表作成 {
  :合計残高試算表を生成;
  :勘定科目別残高を確認;
}

|経営者|
partition 財務報告 {
  :試算表を確認;
  :財務状況を把握;
}
stop

@enduml
```

### 利用シーン

#### 日常経理業務の利用シーン

```plantuml
@startuml

title 利用シーン図 - 日常経理業務

left to right direction

actor 経理担当者
actor 経理責任者

frame "経費精算シーン"
note right of "経費精算シーン"
  従業員の経費精算を処理する場面。
  領収書をもとに仕訳を入力し、
  経費科目と金額を記録する。
end note

frame "売上計上シーン"
note right of "売上計上シーン"
  商品・サービスの売上を計上する場面。
  売掛金と売上高の仕訳を入力。
end note

frame "支払処理シーン"
note right of "支払処理シーン"
  仕入先への支払いを処理する場面。
  買掛金の消込と現金/預金の減少を記録。
end note

usecase "仕訳入力"
usecase "自動仕訳生成"
usecase "仕訳承認"
usecase "元帳照会"

:経理担当者: -- "経費精算シーン"
:経理担当者: -- "売上計上シーン"
:経理担当者: -- "支払処理シーン"
"経費精算シーン" -- (仕訳入力)
"売上計上シーン" -- (仕訳入力)
"売上計上シーン" -- (自動仕訳生成)
"支払処理シーン" -- (仕訳入力)

:経理責任者: -- "経費精算シーン"
"経費精算シーン" -- (仕訳承認)

@enduml
```

#### 決算業務の利用シーン

```plantuml
@startuml

title 利用シーン図 - 決算業務

left to right direction

actor 経理責任者
actor 経営者

frame "月次決算シーン"
note right of "月次決算シーン"
  月末に財務状況を確認する場面。
  試算表を生成し、残高の妥当性を検証。
  必要に応じて決算整理仕訳を入力。
end note

frame "年次決算シーン"
note right of "年次決算シーン"
  期末に財務諸表を作成する場面。
  貸借対照表と損益計算書を生成。
  監査対応の準備を行う。
end note

frame "経営分析シーン"
note right of "経営分析シーン"
  経営判断のために財務指標を確認する場面。
  ROE、流動比率などの指標を算出。
end note

usecase "月次締め"
usecase "試算表生成"
usecase "財務諸表作成"
usecase "財務分析"

:経理責任者: -- "月次決算シーン"
:経理責任者: -- "年次決算シーン"
"月次決算シーン" -- (月次締め)
"月次決算シーン" -- (試算表生成)
"年次決算シーン" -- (財務諸表作成)

:経営者: -- "経営分析シーン"
"経営分析シーン" -- (財務分析)

@enduml
```

### バリエーション・条件

#### 勘定科目種別

| 種別 | 説明 |
|------|------|
| 資産 (ASSET) | 会社が所有する財産（現金、売掛金、商品、建物など） |
| 負債 (LIABILITY) | 会社が負っている債務（買掛金、借入金、未払金など） |
| 純資産 (EQUITY) | 資産から負債を引いた正味財産（資本金、利益剰余金など） |
| 収益 (REVENUE) | 事業活動による収入（売上高、受取利息など） |
| 費用 (EXPENSE) | 事業活動による支出（仕入高、給与、家賃など） |

#### 仕訳ステータス

| ステータス | 説明 |
|------------|------|
| 下書き (DRAFT) | 入力中の仕訳。編集・削除可能 |
| 承認待ち (PENDING_APPROVAL) | 承認待ちの仕訳。経理責任者の承認を待っている |
| 承認済み (APPROVED) | 承認された仕訳。確定処理を待っている |
| 確定 (CONFIRMED) | 確定済みの仕訳。編集・削除不可 |
| 取消 (CANCELLED) | 取り消された仕訳 |

#### ユーザーロール

| ロール | 説明 |
|--------|------|
| 管理者 (ADMIN) | システム全体の管理権限。ユーザー管理、監査ログ参照 |
| 経理責任者 (MANAGER) | 仕訳承認・確定、マスタ管理、財務諸表作成 |
| 経理担当者 (ACCOUNTANT) | 仕訳入力、元帳照会、残高確認 |
| 閲覧者 (VIEWER) | 財務諸表の参照のみ |

## システム境界

### ユースケース複合図

#### マスタ管理

```plantuml
@startuml

title ユースケース複合図 - マスタ管理

left to right direction

actor 経理責任者 as user

frame "マスタ管理シーン" as f01
usecase "勘定科目管理" as UC1
usecase "勘定科目構成管理" as UC2
usecase "自動仕訳設定" as UC3
boundary "勘定科目画面" as b01
boundary "勘定科目構成画面" as b02
boundary "自動仕訳設定画面" as b03
entity "勘定科目" as e01
entity "勘定科目構成" as e02
entity "自動仕訳設定" as e03
control "科目コード一意制約" as c01
control "親子関係整合性" as c02

user -- f01
f01 -- UC1
f01 -- UC2
f01 -- UC3

b01 -- UC1
UC1 -- e01
UC1 -- c01

b02 -- UC2
UC2 -- e01
UC2 -- e02
UC2 -- c02

b03 -- UC3
UC3 -- e01
UC3 -- e03

@enduml
```

#### 仕訳管理

```plantuml
@startuml

title ユースケース複合図 - 仕訳管理

left to right direction

actor 経理担当者 as user
actor 経理責任者 as manager

frame "仕訳入力シーン" as f01
usecase "仕訳入力" as UC1
usecase "自動仕訳生成" as UC2
boundary "仕訳入力画面" as b01
boundary "自動仕訳画面" as b02
entity "仕訳" as e01
entity "仕訳明細" as e02
entity "自動仕訳設定" as e03
control "貸借一致" as c01
control "科目存在チェック" as c02

frame "仕訳承認シーン" as f02
usecase "仕訳承認" as UC3
usecase "仕訳確定" as UC4
boundary "仕訳承認画面" as b03
control "承認権限チェック" as c03
interface "仕訳確定イベント" as i01

user -- f01
f01 -- UC1
f01 -- UC2

b01 -- UC1
UC1 -- e01
UC1 -- e02
UC1 -- c01
UC1 -- c02

b02 -- UC2
UC2 -- e03
UC2 -- e01

manager -- f02
f02 -- UC3
f02 -- UC4

b03 -- UC3
UC3 -- e01
UC3 -- c03

UC4 -- e01
UC4 -- i01

@enduml
```

#### 元帳・残高管理

```plantuml
@startuml

title ユースケース複合図 - 元帳・残高管理

left to right direction

actor 経理担当者 as user
actor 経理責任者 as manager

frame "元帳照会シーン" as f01
usecase "総勘定元帳照会" as UC1
usecase "補助元帳照会" as UC2
boundary "総勘定元帳画面" as b01
boundary "補助元帳画面" as b02
entity "元帳" as e01
entity "元帳明細" as e02

frame "試算表シーン" as f02
usecase "残高試算表" as UC3
boundary "試算表画面" as b03
entity "試算表" as e03
entity "試算表明細" as e04
control "貸借一致検証" as c01

user -- f01
f01 -- UC1
f01 -- UC2

b01 -- UC1
UC1 -- e01
UC1 -- e02

b02 -- UC2
UC2 -- e01
UC2 -- e02

manager -- f02
f02 -- UC3

b03 -- UC3
UC3 -- e03
UC3 -- e04
UC3 -- c01

@enduml
```

#### 財務諸表

```plantuml
@startuml

title ユースケース複合図 - 財務諸表

left to right direction

actor 経理責任者 as manager
actor 経営者 as executive

frame "財務諸表シーン" as f01
usecase "貸借対照表" as UC1
usecase "損益計算書" as UC2
usecase "財務分析" as UC3
boundary "貸借対照表画面" as b01
boundary "損益計算書画面" as b02
boundary "財務分析画面" as b03
entity "貸借対照表" as e01
entity "損益計算書" as e02
entity "財務指標" as e03
control "資産=負債+純資産" as c01
control "収益-費用=利益" as c02

manager -- f01
f01 -- UC1
f01 -- UC2

b01 -- UC1
UC1 -- e01
UC1 -- c01

b02 -- UC2
UC2 -- e02
UC2 -- c02

executive -- f01
f01 -- UC3
b03 -- UC3
UC3 -- e03

@enduml
```

## システム

### 情報モデル

```plantuml
@startuml

title 情報モデル図 - 財務会計システム

left to right direction

' ユーザー・認証関連
entity ユーザー
entity ロール

' マスタ関連
entity 勘定科目
entity 勘定科目構成
entity 自動仕訳設定
entity 会計期間
entity 課税区分

' 仕訳関連
entity 仕訳
entity 仕訳明細

' 元帳関連
entity 総勘定元帳
entity 元帳明細
entity 補助元帳

' 残高関連
entity 日次残高
entity 月次残高
entity 試算表
entity 試算表明細

' 財務諸表関連
entity 貸借対照表
entity 損益計算書
entity 財務指標

' 監査関連
entity 監査ログ
entity 操作履歴

' 関連付け
ユーザー -- ロール
ユーザー -- 仕訳

勘定科目 -- 勘定科目構成
勘定科目 -- 自動仕訳設定

仕訳 -- 仕訳明細
仕訳明細 -- 勘定科目
仕訳明細 -- 課税区分

仕訳 -- 総勘定元帳
総勘定元帳 -- 元帳明細
総勘定元帳 -- 勘定科目

勘定科目 -- 日次残高
勘定科目 -- 月次残高

試算表 -- 試算表明細
試算表明細 -- 勘定科目

貸借対照表 -- 勘定科目
損益計算書 -- 勘定科目

ユーザー -- 監査ログ
ユーザー -- 操作履歴

@enduml
```

### 状態モデル

#### 仕訳の状態遷移

```plantuml
@startuml
title 仕訳の状態遷移図

[*] --> 下書き : 仕訳入力

state 下書き {
  [*] --> 入力中
  入力中 --> 入力中 : 編集
}

下書き --> 承認待ち : 承認申請
承認待ち --> 下書き : 差し戻し
承認待ち --> 承認済み : 承認

承認済み --> 確定 : 確定処理
確定 --> [*]

下書き --> 取消 : 削除
承認待ち --> 取消 : 取り消し
取消 --> [*]

@enduml
```

#### 会計期間の状態遷移

```plantuml
@startuml
title 会計期間の状態遷移図

[*] --> オープン : 期間開始

state オープン {
  [*] --> 営業中
  営業中 --> 営業中 : 仕訳入力
}

オープン --> 月次締め中 : 月次締め開始
月次締め中 --> オープン : 月次締め取消
月次締め中 --> 月次締め完了 : 月次締め確定

月次締め完了 --> 年次締め中 : 年次締め開始
年次締め中 --> 月次締め完了 : 年次締め取消
年次締め中 --> クローズ : 年次締め確定

クローズ --> [*]

@enduml
```

---

## 機能要件サマリ

| カテゴリ | 機能 | 概要 |
|---------|------|------|
| 認証・ユーザー管理 | ログイン/ログアウト | JWT トークンによる認証 |
| | ユーザー管理 | ユーザーの CRUD、ロール割当 |
| マスタ管理 | 勘定科目管理 | 勘定科目コード、名称、種別の管理 |
| | 勘定科目構成管理 | 科目の階層構造（親子関係）の管理 |
| | 自動仕訳設定 | 定型仕訳のテンプレート管理 |
| 仕訳管理 | 仕訳入力 | 借方・貸方の明細入力、貸借一致検証 |
| | 自動仕訳生成 | 設定に基づく仕訳の自動作成 |
| | 仕訳承認 | 上長による承認ワークフロー |
| | 仕訳確定 | 確定済み仕訳のロック |
| 元帳・残高管理 | 総勘定元帳照会 | 勘定科目別の取引履歴と残高 |
| | 補助元帳照会 | 取引先別、部門別等の詳細管理 |
| | 残高試算表 | 全勘定科目の残高一覧と貸借検証 |
| 財務諸表 | 貸借対照表 | 資産・負債・純資産の状況 |
| | 損益計算書 | 収益・費用と当期純利益 |
| | 財務分析 | ROE、流動比率等の財務指標 |
| システム管理 | 監査ログ | 操作履歴の記録・参照 |
| | データダウンロード | CSV/Excel/PDF 出力 |

## 非機能要件サマリ

| カテゴリ | 要件 | 詳細 |
|---------|------|------|
| 可用性 | 稼働率 | 業務時間中は安定して稼働する |
| 性能 | 応答時間 | 一覧表示は3秒以内に応答する |
| セキュリティ | 認証・認可 | JWT 認証、ロールベースアクセス制御 |
| 保守性 | 変更容易性 | 機能追加・変更が容易な構造とする |
| 監査性 | ログ保持 | すべての操作について監査ログを保持する |
| データ整合性 | 貸借一致 | 仕訳の貸借は常に一致する |

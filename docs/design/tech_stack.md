# 技術スタック選定

## 概要

本ドキュメントは、財務会計システムの技術スタック選定について、選定理由、比較検討、採用技術の詳細を記載します。

---

## 選定方針

### 基本原則

| 原則 | 説明 |
|------|------|
| 型安全性 | コンパイル時のエラー検出で堅牢なコードを実現 |
| 保守性 | 長期運用を見据えた保守しやすいコードベース |
| 実績 | エンタープライズシステムでの豊富な実績 |
| エコシステム | 充実したライブラリ・ツール群 |
| テスタビリティ | テスト駆動開発を支援する環境 |

### ソフトウェア開発の三種の神器

```plantuml
@startmindmap
* ソフトウェア開発の三種の神器
** バージョン管理
*** Git
*** GitHub
** テスティング
*** JUnit 5
*** Vitest
*** Testcontainers
** 自動化
*** Gradle
*** npm
*** GitHub Actions
@endmindmap
```

---

## バックエンド技術スタック

### 概要

| カテゴリ | 技術 | バージョン | 用途 |
|---------|------|-----------|------|
| 言語 | Java | 25 | プログラミング言語 |
| フレームワーク | Spring Boot | 4.0.0 | Web フレームワーク |
| O/R マッパー | MyBatis | 3.0.4 | データベースアクセス |
| データベース | PostgreSQL | 16 | 本番データベース |
| マイグレーション | Flyway | 10.x | スキーマバージョン管理 |
| ビルド | Gradle | 9.2.1 | ビルドツール |
| 関数型 | Vavr | 0.10.4 | 関数型プログラミング |
| 認証 | JJWT | 0.12.6 | JWT トークン処理 |
| API ドキュメント | springdoc-openapi | 2.8.8 | OpenAPI / Swagger UI |
| ユーティリティ | Lombok | - | ボイラープレート削減 |
| テスト | JUnit 5 | 5.11.x | テストフレームワーク |
| テスト | Testcontainers | 1.20.4 | テスト用 DB コンテナ |
| テスト | AssertJ | 3.26.x | アサーションライブラリ |
| アーキテクチャテスト | ArchUnit | 1.3.0 | アーキテクチャ検証 |
| ドキュメント生成 | JIG | 2025.11.1 | アーキテクチャ可視化 |
| ER 図生成 | JIG-ERD | 0.2.1 | ER 図自動生成 |
| 静的解析 | Checkstyle | 10.20.2 | コーディング規約チェック |
| 静的解析 | PMD | 7.16.0 | バグパターン検出 |
| 静的解析 | SpotBugs | 4.9.8 | 潜在バグ検出 |
| カバレッジ | JaCoCo | 0.8.14 | テストカバレッジ計測 |

### Java 25 選定理由

```plantuml
@startmindmap
* Java 25
** 長所
*** 型安全性
*** 豊富なエコシステム
*** エンタープライズ実績
*** 優れた IDE サポート
*** 充実したテストフレームワーク
** 新機能
*** パターンマッチング
*** Record クラス
*** Sealed クラス
*** 仮想スレッド（Virtual Threads）
@endmindmap
```

| 観点 | Java の利点 |
|------|-------------|
| 型安全性 | コンパイル時のエラー検出で堅牢なコードを実現 |
| エコシステム | Spring Boot、MyBatis など成熟したフレームワーク群 |
| 保守性 | 静的型付けにより、大規模コードベースでもリファクタリングが安全 |
| テスト | JUnit 5、AssertJ、Testcontainers など充実したテストツール |
| 最新機能 | Record、Sealed クラス、パターンマッチングなど生産性向上 |

### Spring Boot 4.0 選定理由

```plantuml
@startuml
title Spring Boot アーキテクチャ

package "Spring Boot Application" {
  [Spring MVC] --> [Controller]
  [Controller] --> [Service]
  [Service] --> [Repository]

  [Spring Security] --> [Controller]
  [MyBatis] --> [Repository]
  [Spring Validation] --> [Controller]
}

database "Database" {
  [Tables]
}

[Repository] --> [Tables]
@enduml
```

| 特徴 | 説明 |
|------|------|
| 自動設定 | 設定より規約（Convention over Configuration）で開発効率向上 |
| 組み込みサーバー | Tomcat が組み込まれ、単独で実行可能な JAR を生成 |
| スターター | 依存関係をスターターで一括管理 |
| Actuator | ヘルスチェック、メトリクスなど運用機能を標準提供 |
| DevTools | ホットリロードによる開発効率向上 |
| セキュリティ | Spring Security による認証・認可の標準サポート |
| Java 25 対応 | 最新 Java バージョンへの対応 |

### MyBatis vs JPA 比較

```plantuml
@startuml
title O/R マッパーの比較

rectangle "JPA (Hibernate)" {
  (自動 DDL 生成)
  (JPQL)
  (遅延ロード)
  (キャッシュ)
}

rectangle "MyBatis" {
  (SQL 直接記述)
  (動的 SQL)
  (XML マッピング)
  (柔軟なマッピング)
}
@enduml
```

#### 比較表

| 観点 | JPA (Hibernate) | MyBatis |
|------|-----------------|---------|
| SQL 制御 | 自動生成（カスタマイズ困難） | 直接記述（完全制御可能） |
| 学習コスト | 高い（JPQL、N+1 問題など） | 低い（SQL 知識で十分） |
| パフォーマンス | チューニング困難 | SQL レベルで最適化可能 |
| デバッグ | 発行 SQL の特定が困難 | SQL が明確 |
| 既存 DB 対応 | レガシースキーマに不向き | 柔軟に対応可能 |

#### MyBatis 採用理由

| 観点 | MyBatis の利点 |
|------|----------------|
| SQL 制御 | 複雑な SQL を直接記述でき、パフォーマンスチューニングが容易 |
| 学習コスト | SQL の知識がそのまま活用でき、学習曲線が緩やか |
| デバッグ | 実行される SQL が明確で、問題の特定が容易 |
| 柔軟性 | 動的 SQL により、条件に応じた柔軟なクエリが記述可能 |
| 既存 DB 対応 | 既存データベースへの適用が容易 |
| 財務会計要件 | 複雑な集計クエリ、財務諸表生成に適している |

### PostgreSQL 選定理由

| 観点 | PostgreSQL の利点 |
|------|-------------------|
| 信頼性 | ACID 準拠、トランザクション安全性 |
| 機能性 | JSON、配列、ENUM 型など豊富なデータ型 |
| 拡張性 | プラグイン、カスタム関数のサポート |
| コスト | オープンソース、ライセンス費用なし |
| 実績 | エンタープライズシステムでの豊富な実績 |

### Testcontainers 選定理由

```plantuml
@startuml
title Testcontainers の動作

[*] --> テスト開始
テスト開始 --> コンテナ起動 : PostgreSQL コンテナを起動
コンテナ起動 --> マイグレーション実行 : Flyway でスキーマ作成
マイグレーション実行 --> テスト実行
テスト実行 --> コンテナ停止 : テスト終了後に自動削除
コンテナ停止 --> [*]
@enduml
```

| メリット | 説明 |
|---------|------|
| 完全な分離 | 各テスト実行時に新しいコンテナを起動、他のテストとの干渉を防止 |
| 自動クリーンアップ | テスト終了時にコンテナが自動的に削除 |
| 本番同等環境 | 実際の PostgreSQL コンテナを使用、インメモリ DB では検出できない問題も発見 |
| CI/CD 対応 | Docker があればどこでもテストが実行可能 |

### Vavr 選定理由

関数型プログラミングを Java で実現するために Vavr を採用しました。

```plantuml
@startmindmap
* Vavr
** 不変コレクション
*** List
*** Set
*** Map
** 制御構造
*** Option (null 安全)
*** Either (エラー処理)
*** Try (例外処理)
*** Validation (複数エラー蓄積)
** データ構造
*** Tuple (複数値)
*** Lazy (遅延評価)
** パターンマッチング
*** Match API
@endmindmap
```

| 機能 | 説明 | 用途 |
|------|------|------|
| `Option<T>` | null 安全なコンテナ | null チェックの代替 |
| `Either<L, R>` | 2つの型のいずれかを保持 | エラーハンドリング |
| `Try<T>` | 例外を値として扱う | 例外処理の関数化 |
| `Validation<E, T>` | 複数エラーの蓄積 | バリデーション |
| `Tuple2-8` | 不変タプル | 複数値の返却 |
| `List`, `Set`, `Map` | 不変コレクション | スレッドセーフなデータ構造 |

#### Vavr のメリット

| メリット | 説明 |
|---------|------|
| null 安全性 | Option による null 参照エラーの防止 |
| 明示的なエラー処理 | Either/Try による例外の値としての扱い |
| 不変性 | 不変コレクションによるスレッドセーフな設計 |
| 関数合成 | 高階関数による宣言的なコード |

### JIG 選定理由

アーキテクチャの可視化と設計ドキュメント自動生成のために JIG を採用しました。

```plantuml
@startuml
title JIG によるドキュメント生成

[*] --> ソースコード解析
ソースコード解析 --> クラス図生成
ソースコード解析 --> パッケージ依存関係図
ソースコード解析 --> ユースケース図
ソースコード解析 --> ビジネスルール一覧
クラス図生成 --> ドキュメント出力
パッケージ依存関係図 --> ドキュメント出力
ユースケース図 --> ドキュメント出力
ビジネスルール一覧 --> ドキュメント出力
ドキュメント出力 --> [*]
@enduml
```

| 機能 | 説明 |
|------|------|
| パッケージ関連図 | パッケージ間の依存関係を可視化 |
| クラス関連図 | ドメインモデルの関連を可視化 |
| ビジネスルール一覧 | ビジネスロジックを一覧化 |
| ユースケース図 | アプリケーション層のユースケースを可視化 |

#### JIG-ERD 選定理由

MyBatis のマッパーからデータベーススキーマを解析し、ER 図を自動生成します。

| メリット | 説明 |
|---------|------|
| 自動生成 | コードから ER 図を自動生成 |
| 常に最新 | コード変更時に再生成で同期 |
| 保守性 | ドキュメントの手動メンテナンス不要 |

### springdoc-openapi 選定理由

API ドキュメントの自動生成と対話的なテスト環境として springdoc-openapi を採用しました。

```plantuml
@startuml
title springdoc-openapi による API ドキュメント生成

[*] --> Controller解析
Controller解析 : @RestController, @RequestMapping
Controller解析 --> アノテーション解析
アノテーション解析 : @Operation, @ApiResponse, @Schema
アノテーション解析 --> OpenAPI仕様生成
OpenAPI仕様生成 : openapi.yaml / openapi.json
OpenAPI仕様生成 --> SwaggerUI表示
SwaggerUI表示 : ブラウザでAPI確認・テスト
SwaggerUI表示 --> [*]
@enduml
```

| 機能 | 説明 |
|------|------|
| OpenAPI 仕様生成 | Controller から OpenAPI 3.0 仕様を自動生成 |
| Swagger UI | ブラウザベースの API ドキュメント・テストツール |
| JWT 認証対応 | Bearer トークン認証をサポート |
| スキーマ定義 | DTO から JSON スキーマを自動生成 |

#### springdoc-openapi のメリット

| メリット | 説明 |
|---------|------|
| コード同期 | Controller の変更が即座にドキュメントに反映 |
| 対話的テスト | Swagger UI から直接 API を実行・テスト可能 |
| フロントエンド連携 | OpenAPI 仕様から Orval で TypeScript コードを生成 |
| チーム連携 | API 仕様を共有し、フロントエンド開発を並行実施 |

#### アクセス URL

| URL | 説明 |
|-----|------|
| `/swagger-ui.html` | Swagger UI（API ドキュメント・テスト画面） |
| `/v3/api-docs` | OpenAPI 仕様（JSON 形式） |
| `/api-docs.yaml` | OpenAPI 仕様（YAML 形式） |

---

## フロントエンド技術スタック

### 概要

| カテゴリ | 技術 | バージョン | 用途 |
|---------|------|-----------|------|
| 言語 | TypeScript | 5.5.x | 型安全な JavaScript |
| UI ライブラリ | React | 18.3.x | コンポーネントベース UI |
| ルーティング | React Router | 6.x | SPA ルーティング |
| ビルドツール | Vite | 5.4.x | 高速ビルド・HMR |
| サーバー状態管理 | TanStack Query | 5.x | キャッシュ・再取得 |
| HTTP クライアント | Axios | 1.7.x | HTTP リクエスト |
| コード生成 | Orval | 7.x | OpenAPI → TypeScript |
| 金額計算 | decimal.js | 10.x | 精度保証された数値計算 |
| 日付処理 | dayjs | 1.11.x | 日付フォーマット・計算 |
| 単体テスト | Vitest | 2.x | テストフレームワーク |
| コンポーネントテスト | React Testing Library | 16.x | DOM テスト |
| API モック | MSW | 2.x | リクエストモック |
| E2E テスト | Cypress | 14.x | 統合テスト |

### React + TypeScript 選定理由

```plantuml
@startuml
title 技術選定の判断基準

rectangle "要件" {
  card "型安全性" as r1
  card "保守性" as r2
  card "開発効率" as r3
  card "エコシステム" as r4
}

rectangle "選定技術" {
  card "TypeScript" as t1
  card "React" as t2
  card "Vite" as t3
  card "TanStack Query" as t4
}

r1 --> t1 : コンパイル時型チェック
r2 --> t1 : 明確な型定義
r2 --> t2 : コンポーネント設計
r3 --> t2 : 宣言的 UI
r3 --> t3 : 高速 HMR
r4 --> t2 : 豊富なライブラリ
r4 --> t4 : サーバー状態管理
@enduml
```

| 要件 | 説明 |
|------|------|
| 型安全性 | 金額計算、API 連携での型不整合防止 |
| 保守性 | 長期運用を見据えたコードベース |
| 開発効率 | コンポーネントの再利用性 |
| エコシステム | 豊富なライブラリ・ツール |

### Vite vs Create React App 比較

| 比較項目 | CRA (webpack) | Vite |
|---------|---------------|------|
| 開発サーバー起動 | 30秒〜1分 | 1秒以下 |
| HMR（ホットリロード） | 数秒 | 即時 |
| ビルド速度 | 遅い | 高速（esbuild） |
| 設定の複雑さ | 隠蔽（eject 必要） | シンプル |
| TypeScript サポート | 追加設定必要 | ネイティブ対応 |

#### Vite 採用理由

- **高速開発サーバー**: ES Module ベースでバンドル不要
- **即座の HMR**: 変更が即座に反映
- **シンプルな設定**: vite.config.ts で完結
- **TypeScript ネイティブ対応**: 追加設定不要

### TanStack Query 選定理由

```plantuml
@startuml
title 状態管理戦略

rectangle "サーバー状態\n(Server State)" as server {
  (API からのデータ)
  (キャッシュ)
  (ローディング状態)
  (エラー状態)
}

rectangle "クライアント状態\n(Client State)" as client {
  (UI 状態)
  (フォーム入力)
  (モーダル開閉)
  (選択状態)
}

rectangle "グローバル状態\n(Global State)" as global {
  (認証情報)
  (テーマ設定)
  (アプリ設定)
}

cloud "TanStack Query" as tanstack
cloud "useState/useReducer" as useState
cloud "Context API" as context

server --> tanstack : 管理
client --> useState : 管理
global --> context : 管理
@enduml
```

| 状態の種類 | 管理方法 | 例 |
|-----------|---------|-----|
| サーバー状態 | TanStack Query | API からの勘定科目データ、仕訳データ |
| クライアント状態 | useState / useReducer | モーダル開閉、フォーム入力値 |
| グローバル状態 | Context API | 認証情報、ユーザー設定 |

#### TanStack Query のメリット

| 機能 | 説明 |
|------|------|
| 自動キャッシュ | 同一データの重複リクエストを防止 |
| バックグラウンド再取得 | データの鮮度を自動維持 |
| 楽観的更新 | ユーザー体験の向上 |
| エラーリトライ | 一時的なエラーからの自動復旧 |
| 宣言的な API | シンプルで直感的なインターフェース |

### Orval 選定理由

```plantuml
@startuml
title API 連携フロー

participant "Spring Boot" as backend
participant "springdoc-openapi" as springdoc
database "openapi.yaml" as spec
participant "Orval" as orval
participant "React App" as frontend

== バックエンド開発時 ==
backend -> springdoc : Controller + DTO
springdoc -> spec : 自動生成

== フロントエンド開発時 ==
frontend -> spec : ダウンロード
frontend -> orval : npm run api:generate
orval -> frontend : API Client\n型定義\nReact Query Hooks
@enduml
```

| 生成物 | 説明 |
|--------|------|
| API クライアント | Axios ベースの HTTP クライアント |
| 型定義 | TypeScript インターフェース |
| React Query Hooks | useQuery / useMutation ラッパー |

#### Orval のメリット

| メリット | 説明 |
|---------|------|
| 型安全性 | バックエンド API と完全に同期した型定義 |
| 自動生成 | 手動でのクライアントコード記述不要 |
| 保守性 | API 変更時は再生成するだけ |
| 開発効率 | TanStack Query Hooks も自動生成 |

### decimal.js 選定理由

財務会計システムでは金額計算の精度が重要です。JavaScript の浮動小数点演算では精度問題が発生するため、decimal.js を採用しました。

```javascript
// JavaScript の浮動小数点問題
0.1 + 0.2 // 0.30000000000000004

// decimal.js による精度保証
new Decimal('0.1').plus('0.2') // 0.3
```

| 観点 | decimal.js の利点 |
|------|-------------------|
| 精度 | 任意精度の十進演算 |
| 財務計算 | 金額計算での丸め誤差なし |
| API 互換 | JavaScript Number と互換性のある API |

---

## インフラストラクチャ技術スタック

### 概要

| カテゴリ | 技術 | 用途 |
|---------|------|------|
| コンテナ | Docker | コンテナ化 |
| 開発環境 | Docker Compose | ローカル開発環境 |
| CI/CD | GitHub Actions | 継続的インテグレーション・デプロイ |
| デモ環境 | Heroku Container | デモ環境ホスティング |
| デモ DB | H2 Database | デモ環境データベース（インメモリ） |
| 開発 DB | PostgreSQL 16 | 開発・テスト用データベース |
| DB 管理 | Adminer | データベース管理ツール |

### Docker / Docker Compose 選定理由

```plantuml
@startuml
title 開発環境構成

rectangle "Docker Compose" as dc {
  node "postgres" as postgres {
    [PostgreSQL 16]
  }

  node "adminer" as adminer {
    [Adminer]
  }

  node "backend" as backend {
    [Spring Boot]
  }

  node "frontend" as frontend {
    [Vite Dev Server]
  }
}

postgres --> adminer
backend --> postgres
frontend --> backend : proxy
@enduml
```

| メリット | 説明 |
|---------|------|
| 環境統一 | 開発者間で同一環境を共有 |
| 再現性 | 環境構築の再現性を保証 |
| 分離性 | ホスト環境を汚染しない |
| CI/CD 対応 | GitHub Actions との親和性 |

### GitHub Actions 選定理由

```plantuml
@startuml
title CI/CD パイプライン

|開発者|
start
:コードをプッシュ;

|GitHub Actions|
:コードチェックアウト;

fork
  :Backend CI;
  :JDK セットアップ;
  :Gradle ビルド;
  :テスト実行;
  :静的解析;
fork again
  :Frontend CI;
  :Node.js セットアップ;
  :npm install;
  :Lint チェック;
  :テスト実行;
  :ビルド;
end fork

:アーティファクト保存;

if (main ブランチ?) then (yes)
  :デプロイ;
endif

stop
@enduml
```

| メリット | 説明 |
|---------|------|
| GitHub 統合 | リポジトリとシームレスに統合 |
| 無料枠 | パブリックリポジトリは無制限 |
| 豊富なアクション | 公式・コミュニティアクションが豊富 |
| マトリックスビルド | 複数環境での並列テスト |

---

## 技術スタック一覧

### バックエンド

```plantuml
@startmindmap
* バックエンド
** 言語・フレームワーク
*** Java 25
*** Spring Boot 4.0.0
*** Spring Security
*** Spring Validation
*** Lombok
** データアクセス
*** MyBatis 3.0.4
*** Flyway 10.x
*** PostgreSQL 16
** 関数型・ユーティリティ
*** Vavr 0.10.4
*** JJWT 0.12.6
** API ドキュメント
*** springdoc-openapi 2.8.8
*** Swagger UI
** テスト
*** JUnit 5
*** Testcontainers 1.20.4
*** AssertJ
*** ArchUnit 1.3.0
** 品質管理
*** Checkstyle 10.20.2
*** PMD 7.16.0
*** SpotBugs 4.9.8
*** JaCoCo 0.8.14
** ドキュメント生成
*** JIG 2025.11.1
*** JIG-ERD 0.2.1
@endmindmap
```

### フロントエンド

```plantuml
@startmindmap
* フロントエンド
** 言語・フレームワーク
*** TypeScript 5.5.x
*** React 18.3.x
*** React Router 6.x
** ビルド
*** Vite 5.4.x
*** ESLint
*** Prettier
** API 連携
*** TanStack Query 5.x
*** Axios 1.7.x
*** Orval 7.x
** ユーティリティ
*** decimal.js
*** dayjs
** テスト
*** Vitest 2.x
*** React Testing Library
*** MSW 2.x
*** Cypress 14.x
@endmindmap
```

### インフラストラクチャ

```plantuml
@startmindmap
* インフラストラクチャ
** コンテナ
*** Docker
*** Docker Compose
** CI/CD
*** GitHub Actions
** デプロイ
*** Heroku Container
** データベース
*** PostgreSQL 16（開発）
*** H2 Database（デモ）
** 監視
*** Spring Boot Actuator
*** Heroku Logplex
@endmindmap
```

---

## バージョン管理ポリシー

### バージョン固定方針

| 対象 | 方針 |
|------|------|
| メジャーバージョン | 明示的に指定（例：Java 25） |
| マイナーバージョン | 互換性のある範囲で最新（例：3.2.x） |
| パッチバージョン | セキュリティ修正は即時適用 |

### 依存関係更新ルール

1. **セキュリティ脆弱性**: 即時更新
2. **バグ修正**: スプリント内で更新
3. **機能追加**: 次期バージョンで検討
4. **メジャーアップグレード**: 計画的に実施

---

## 選定プロセス

### 評価基準

| 基準 | 重み | 説明 |
|------|------|------|
| 型安全性 | 高 | 財務システムとしてのデータ整合性 |
| 実績・安定性 | 高 | エンタープライズでの採用実績 |
| 保守性 | 高 | 長期運用での保守コスト |
| エコシステム | 中 | ライブラリ・ツールの充実度 |
| 学習コスト | 中 | チームの習熟度、採用難易度 |
| パフォーマンス | 中 | 応答時間、スループット |
| コスト | 低 | ライセンス、運用コスト |

### 技術選定フロー

```plantuml
@startuml
title 技術選定フロー

[*] --> 要件定義
要件定義 : 機能要件・非機能要件の明確化

要件定義 --> 候補選定
候補選定 : 候補技術のリストアップ

候補選定 --> 評価
評価 : PoC 実施、比較検討

評価 --> 選定
選定 : ADR 作成、承認

選定 --> 導入
導入 : 環境構築、チーム教育

導入 --> [*]
@enduml
```

---

## まとめ

本システムの技術スタックは、以下の観点から選定しました。

### 選定ポイント

| 観点 | 選定内容 |
|------|----------|
| 型安全性 | Java 25 + TypeScript による静的型付け |
| 生産性 | Spring Boot 4.0 + React + Vite による開発効率 |
| 関数型アプローチ | Vavr による null 安全性と明示的なエラー処理 |
| テスタビリティ | Testcontainers + Vitest による充実したテスト環境 |
| 保守性 | ヘキサゴナルアーキテクチャ + Container/View パターン |
| API 連携 | OpenAPI + Orval による型安全な連携 |
| 財務計算 | decimal.js による精度保証 |
| ドキュメント | JIG + JIG-ERD によるアーキテクチャ可視化 |
| 品質管理 | Checkstyle + PMD + SpotBugs による静的解析 |

### 今後の検討事項

| 項目 | 内容 |
|------|------|
| GraphQL | 複雑なクエリ要件への対応 |
| マイクロサービス | スケーラビリティ向上時の検討 |
| Kubernetes | 本番環境でのコンテナオーケストレーション |
| 監視・可観測性 | Prometheus + Grafana の導入 |

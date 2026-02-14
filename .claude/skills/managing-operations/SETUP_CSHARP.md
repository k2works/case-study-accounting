# C# WPF 開発環境の統合セットアップ

ドキュメントベースの包括的 C# WPF + Clean Architecture 開発環境構築手順。

## 参照ドキュメント

- `@docs/design/tech_stack.md`: C# WPF 技術スタック選定理由と詳細仕様
- `@docs/design/architecture_backend.md`: Clean Architecture + MagicOnion サーバー設計詳細
- `@docs/design/architecture_frontend.md`: WPF MVVM アーキテクチャとプレゼンテーション層設計詳細

## 初期設定プロセス

### 対話式プロジェクト設定

- プロジェクト名の確認（例: AdventureWorks.PurchasingSystem）
- ソリューション名の確認（例: AdventureWorks）
- 作成場所の確認（デフォルト: `apps/` 配下）
- データベース接続文字列の確認

### 注意点

- apps/app のような構成にしてはいけない
- apps/{solution-name} のような構成すること
- tech_stack.md のディレクトリ構成詳細にしたがうこと
- ディレクトリだけの場合もコミットしたいので `.gitkeep` を入れる
- Java の `src` や `tests` のようなディレクトリ構成にしないこと

## 構築される環境の詳細

### 基盤技術 (@docs/design/tech_stack.md 準拠)

- .NET 8.0 + C# 12
- WPF + CommunityToolkit.Mvvm 8.x
- MagicOnion 5.x (gRPC ベース RPC)
- Dapper 2.x (Micro ORM)
- Kamishibai 3.x (ナビゲーション)

### アーキテクチャ (@docs/design/architecture_backend.md + @docs/design/architecture_frontend.md 準拠)

- Clean Architecture（依存関係逆転）
- Domain-Driven Design（ドメインモデル中心）
- MVVM パターン（WPF 標準）
- レイヤー分離: Domain → Application → Infrastructure → Presentation
- MagicOnion サーバーによる gRPC 通信

### データベース環境

- 開発・テスト: SQL Server LocalDB
- 本番: SQL Server + Dapper
- マイグレーション: DbUp 5.x
- 接続プール: SqlConnection + HikariCP 相当

### 品質管理ツール

- テスト: NUnit 3 + FluentAssertions + Moq + Codeer.Friendly
- 静的解析: SonarQube + StyleCop + FxCop
- カバレッジ: OpenCover (Domain: 95%, Application: 85% 目標)
- UI テスト: Codeer.Friendly.Windows.Wpf
- E2E テスト: Page Object Pattern

### 開発支援機能

- Serilog (構造化ログ)
- Docker Compose (開発環境)
- MkDocs (ドキュメントサイト)
- PlantUML (アーキテクチャ図)

## 連携シナリオ

```bash
# ドキュメント準拠の C# WPF 開発環境構築（対話式）
cat docs/design/tech_stack.md
cat docs/design/architecture_backend.md
cat docs/design/architecture_frontend.md
# → ソリューション名入力: "AdventureWorks"
# → プロジェクト名確認: "AdventureWorks.PurchasingSystem" (自動生成)
# → 作成場所確認: "apps/wpfapp/" (Enter でデフォルト採用)
# → 接続文字列確認: "Server=(localdb)\\mssqllocaldb;..." (デフォルト採用)
dotnet build
```

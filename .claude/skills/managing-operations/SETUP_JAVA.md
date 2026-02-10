# Java 開発環境の統合セットアップ

ドキュメントベースの包括的 Java 開発環境構築手順。

## 参照ドキュメント

- `docs/reference/Javaアプリケーション環境構築ガイド.md`: 基本セットアップ手順とベストプラクティス
- `@docs/design/tech_stack.md`: 技術スタック選定理由と詳細仕様
- `@docs/design/architecture_backend.md`: ヘキサゴナルアーキテクチャ設計詳細

## 初期設定プロセス

### 対話式プロジェクト設定

- プロジェクト名の確認（例: meeting-room-system）
- パッケージ名の確認（例: com.example.{project-name}）
- グループ ID・アーティファクト ID の設定確認
- 作成場所の確認（デフォルト: `apps/backend/` 配下）

### 注意点

- `docs/reference/Javaアプリケーション環境構築ガイド.md` の Gradle プロジェクトの初期化部分は参考にしない
- apps/backend/app のような構成にしてはいけない
- apps/backend/{project-name} のような構成すること
- ディレクトリだけの場合もコミットしたいので `.gitkeep` を入れる

## 構築される環境の詳細

### 基盤技術 (@docs/design/tech_stack.md 準拠)

- Java 21 LTS + Spring Boot 3.3.2
- Spring Security 6.3.1 (JWT Bearer 認証)
- Spring Data JPA 3.3.2
- Gradle 8.5 + Gradle Wrapper

### アーキテクチャ (@docs/design/architecture_backend.md 準拠)

- ヘキサゴナルアーキテクチャ（ポートとアダプターパターン）
- ドメインモデルパターン
- レイヤー分離: Domain → Application → Infrastructure
- ディレクトリ構造: `src/main/java/{domain,application,infrastructure,shared}/`

### データベース環境

- 開発・テスト: H2 Database 2.2.224 (In-Memory)
- 本番: PostgreSQL 15 + HikariCP 5.0.1
- マイグレーション: Flyway 9.22.3

### 品質管理ツール

- テスト: JUnit 5 + AssertJ + Mockito + Testcontainers + ArchUnit
- 静的解析: SonarQube + Checkstyle 10.12.3 + SpotBugs + PMD
- カバレッジ: JaCoCo (80% 目標)
- セキュリティ: OWASP Dependency Check 8.4.0
- 循環複雑度制限: 7 以下

### 開発支援機能

- Spring Boot DevTools (ホットリロード)
- Spring Boot Actuator (監視・メトリクス)
- 環境プロファイル分離 (dev/test/prod)
- H2 Console (開発時データベース可視化)

## 連携シナリオ

```bash
# ドキュメント準拠の Java 開発環境構築（対話式）
cat docs/reference/Javaアプリケーション環境構築ガイド.md
cat docs/design/tech_stack.md
cat docs/design/architecture_backend.md
# → プロジェクト名入力: "meeting-room-system"
# → 作成場所確認: "apps/backend/" (Enter でデフォルト採用)
# → パッケージ名確認: "com.example.meetingroomsystem" (自動生成)
./gradlew build
```

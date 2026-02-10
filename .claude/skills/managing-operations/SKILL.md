---
name: managing-operations
description: アプリケーションの運用・構築・配置を統括管理。環境構築、デプロイ、監視、バックアップ、日次運用タスクの実行。運用管理やインフラ整備時に使用。
---

# 運用管理ガイド

アプリケーションの運用・構築・配置を統括的に管理し、本番環境への安全かつ効率的なデプロイを実現します。

## Instructions

### 1. オプション

- なし : 運用全体のステータス確認（デフォルト）
- `--setup [環境名]` : 環境構築の初期設定とインフラ準備
  - `--setup Java` : Java 開発環境の構築と Spring Boot プロジェクトセットアップ
  - `--setup FrontEnd` : TypeScript/React 開発環境の構築と Vite プロジェクトセットアップ
  - `--setup C#WPF` : C# WPF 開発環境の構築と Clean Architecture プロジェクトセットアップ
- `--build` : アプリケーションのビルドとパッケージング
- `--deploy <環境>` : 指定環境へのデプロイ実行
- `--status <環境>` : 特定環境の動作状況確認
- `--rollback <環境>` : 指定環境での前バージョンへのロールバック
- `--logs <サービス>` : 特定サービスのログ確認
- `--health` : システム全体のヘルスチェック実行
- `--backup` : データベース・設定ファイルのバックアップ作成
- `--restore <バックアップ>` : 指定バックアップからの復元
- `--daily` : 日次運用タスクの自動実行（日誌生成と概要編集）

### 2. 基本例

```bash
# 運用全体の状況確認
# 「現在の環境構築・デプロイ・運用状況を包括的にレポート」

# Java 開発環境の構築
# --setup Java
# 「プロジェクト名と作成場所を対話式で確認後、Java 開発環境の統合セットアップ」

# TypeScript/React 開発環境の構築
# --setup FrontEnd
# 「プロジェクト名と作成場所を対話式で確認後、TypeScript/React 開発環境の統合セットアップ」

# C# WPF 開発環境の構築
# --setup C#WPF
# 「プロジェクト名と作成場所を対話式で確認後、C# WPF + Clean Architecture 開発環境の統合セットアップ」

# プロダクション環境へのデプロイ
# --deploy production
# 「本番環境への安全なデプロイ実行とヘルスチェック」

# システム全体のヘルスチェック
# --health
# 「全環境のサービス稼働状況・リソース使用量・アラート確認」

# 日次運用タスクの実行
# --daily
# 「日誌の自動生成と概要編集を実行」
```

### 3. 環境構築の詳細

各環境構築オプションの詳細は以下の参照ファイルを確認してください:

- **Java**: @.claude/skills/managing-operations/SETUP_JAVA.md
- **TypeScript/React**: @.claude/skills/managing-operations/SETUP_FRONTEND.md
- **C# WPF**: @.claude/skills/managing-operations/SETUP_CSHARP.md

### 4. デプロイ・運用監視の詳細

デプロイメント管理、運用監視、バックアップの詳細:

- @.claude/skills/managing-operations/DEPLOY.md

### 5. 環境構築とインフラ管理

```bash
# 完全な環境構築
# --setup
```

**実行される構築作業**:

- **Docker 環境**: コンテナ・ネットワーク・ボリュームの構築
- **CI/CD パイプライン**: GitHub Actions・Jenkins 等の設定
- **データベース**: PostgreSQL・Redis 等のセットアップ
- **監視システム**: ログ収集・メトリクス監視・アラート設定
- **セキュリティ**: SSL 証明書・認証基盤・ファイアウォール設定

### 6. ビルドとパッケージング

複数言語・フレームワークに対応した統合ビルドシステム:

**対応技術スタック**:

- **Java**: Maven・Gradle による Spring Boot アプリケーション
- **Node.js**: npm・yarn による React・Vue.js アプリケーション
- **Python**: pip・poetry による Django・FastAPI アプリケーション
- **.NET**: dotnet による ASP.NET Core アプリケーション

### 7. 日次運用タスク

プロジェクトの日常的な運用作業を自動化し、開発履歴の記録と管理を支援:

```bash
# 日次運用タスクの自動実行
# --daily
```

**実行される作業**:

1. `npm run journal` コマンドの実行
2. Git コミット履歴から日付別の作業記録を自動生成
3. `docs/journal/YYYYMMDD.md` 形式でファイル作成
4. `mkdocs.yml` に自動登録
5. 技術的な作業内容を簡潔に要約

### 8. 出力例

```
運用管理ダッシュボード
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Overall Status: 全環境正常稼働中

Environment Status:
├─ Development: 稼働中 (backend: http://localhost:5150)
├─ Staging: 稼働中 (v1.2.1)
├─ Production: 稼働中 (v1.2.0, 99.99% アップタイム)
└─ Monitoring: 正常 (アラートなし)

Infrastructure Health:
├─ Database: PostgreSQL 正常 (CPU: 15%, Memory: 45%)
├─ API Gateway: 正常 (平均レスポンス: 120ms)
└─ CDN: キャッシュ効率: 95%
```

### 9. 注意事項

- **前提条件**: Docker・Git・適切な権限設定が必要
- **制限事項**: 本番環境への直接アクセスは制限される場合あり
- **推奨事項**: 本番デプロイ前に必ずステージング環境での動作確認を実施

### 10. ベストプラクティス

1. **段階的デプロイ**: Development → Staging → Production の順序を厳守
2. **自動化優先**: 手作業を最小限に抑え、スクリプトと CI/CD で自動化
3. **監視ファースト**: デプロイ前後の監視設定とアラート確認を徹底
4. **バックアップ確保**: 重要な変更前には必ずバックアップを作成
5. **ロールバック準備**: 緊急時の迅速なロールバック手順を事前準備
6. **セキュリティ重視**: 認証・認可・暗号化・ログ監査を常に実施

### 関連スキル

- `tracking-progress` : 開発進捗と運用準備状況の確認
- `planning-releases` : リリース計画と運用スケジュールの管理
- `git-commit` : デプロイに適したコミット作成

# 会計システムのケーススタディ

## 概要

会計システムの設計・開発に関するケーススタディプロジェクトです。

### 目的

- 会計システムの要件定義から実装までの開発プロセスを学ぶ
- ドメイン駆動設計（DDD）とテスト駆動開発（TDD）の実践
- クリーンアーキテクチャに基づくシステム設計の習得

### 前提

| ソフトウェア | バージョン | 備考                     |
| :----------- | :--------- | :----------------------- |
| Node.js      | 24.x       | ドキュメントビルド用     |
| Docker       | latest     | 開発環境コンテナ         |
| Python       | 3.x        | MkDocs 実行用            |

## プロジェクト構成

```
case-study-accounting/
├── apps/                    # アプリケーションコード
├── docs/                    # ドキュメント（MkDocs）
│   ├── adr/                 #   アーキテクチャ決定記録
│   ├── assets/              #   静的ファイル
│   ├── design/              #   設計ドキュメント
│   ├── development/         #   開発ドキュメント
│   ├── operation/           #   運用ドキュメント
│   ├── reference/           #   参照ガイド
│   ├── requirements/        #   要件定義
│   ├── template/            #   テンプレート
│   └── wiki/                #   Wiki
├── scripts/                 # ユーティリティスクリプト
├── .claude/                 # Claude Code 設定
├── .devcontainer/           # Dev Container 設定
├── .github/                 # GitHub Actions ワークフロー
├── CLAUDE.md                # AI Agent 実行ガイドライン
├── Dockerfile               # Docker イメージ定義
├── docker-compose.yml       # Docker Compose 設定
├── gulpfile.js              # Gulp タスク定義
├── mkdocs.yml               # MkDocs 設定
└── package.json             # npm パッケージ設定
```

## 目次

- [Quick Start](#quick-start)
- [構築](#構築)
- [配置](#配置)
- [運用](#運用)
- [開発](#開発)
- [ドキュメント](#ドキュメント)

## 詳細

### Quick Start

```bash
npm install
npm start
```

### Docker Compose でアプリケーション起動

バックエンドとフロントエンドを Docker Compose で起動できます。

```bash
# バックエンド・フロントエンドを起動（PostgreSQL も自動起動）
docker compose up -d backend frontend

# ログ確認
docker compose logs -f backend frontend

# 停止
docker compose down
```

#### サービス一覧

| サービス | ポート | 説明 |
|----------|--------|------|
| frontend | 3001 | React SPA (nginx) |
| backend | 8081 | Spring Boot API |
| postgres | 5432 | PostgreSQL データベース |
| adminer | 8888 | データベース管理ツール |
| mkdocs | 8000 | ドキュメントサーバー |
| sonarqube | 9000 | コード品質分析 |

#### アクセス URL

- フロントエンド: http://localhost:3001
- バックエンド API: http://localhost:8081
- Adminer: http://localhost:8888
- MkDocs: http://localhost:8000
- SonarQube: http://localhost:9000

#### 環境変数

| 変数 | デフォルト | 説明 |
|------|----------|------|
| `BACKEND_PORT` | 8081 | バックエンドの公開ポート |
| `FRONTEND_PORT` | 3001 | フロントエンドの公開ポート |
| `POSTGRES_PORT` | 5432 | PostgreSQL の公開ポート |
| `SPRING_PROFILES_ACTIVE` | dev | Spring プロファイル |

### 構築

**[⬆ back to top](#目次)**

### 配置

#### GitHub Pages セットアップ

1. **GitHub リポジトリの Settings を開く**
    - リポジトリページで `Settings` タブをクリック

2. **Pages 設定を開く**
    - 左サイドバーの `Pages` をクリック

3. **Source を設定**
    - `Source` で `Deploy from a branch` を選択
    - `Branch` で `gh-pages` を選択し、フォルダは `/ (root)` を選択
    - `Save` をクリック

4. **初回デプロイ**
    - main ブランチにプッシュすると GitHub Actions が自動実行
    - Actions タブでデプロイ状況を確認

**[⬆ back to top](#目次)**

### 運用

#### ドキュメントの編集

1. ローカル環境でMkDocsサーバーを起動
   ```
   docker-compose up mkdocs
   ```
   または、Gulpタスクを使用:
   ```
   npm run docs:serve
   ```

2. ブラウザで http://localhost:8000 にアクセスして編集結果をプレビュー

3. `docs/`ディレクトリ内のMarkdownファイルを編集

4. 変更をコミットしてプッシュ
   ```
   git add .
   git commit -m "ドキュメントの更新"
   git push
   ```

#### Gulpタスクの使用

プロジェクトには以下のGulpタスクが用意されています：

##### MkDocsタスク

- MkDocsサーバーの起動:
  ```
  npm run docs:serve
  ```
  または
  ```
  npx gulp mkdocs:serve
  ```

- MkDocsサーバーの停止:
  ```
  npm run docs:stop
  ```
  または
  ```
  npx gulp mkdocs:stop
  ```

- MkDocsドキュメントのビルド:
  ```
  npm run docs:build
  ```
  または
  ```
  npx gulp mkdocs:build
  ```

##### 作業履歴（ジャーナル）タスク

- すべてのコミット日付の作業履歴を生成:
  ```
  npm run journal
  ```
  または
  ```
  npx gulp journal:generate
  ```

- 特定の日付の作業履歴を生成:
  ```
  npx gulp journal:generate:date --date=YYYY-MM-DD
  ```
  (例: `npx gulp journal:generate:date --date=2023-04-01`)

生成された作業履歴は `docs/journal/` ディレクトリに保存され、各ファイルには指定された日付のコミット情報が含まれます。

##### SonarQube タスク

- SonarQube サービスの起動:
  ```
  npx gulp sonar:start
  ```

- SonarQube サービスの停止:
  ```
  npx gulp sonar:stop
  ```

- SonarQube ダッシュボードを開く:
  ```
  npx gulp sonar:open
  ```

- バックエンドの解析実行:
  ```
  npx gulp sonar:analyze:backend
  ```

- フロントエンドの解析実行:
  ```
  npx gulp sonar:analyze:frontend
  ```

- 全プロジェクトの解析実行:
  ```
  npx gulp sonar:analyze
  ```

- サービス状態確認:
  ```
  npx gulp sonar:status
  ```

##### SchemaSpy タスク

- ER 図の生成:
  ```
  npx gulp schemaspy:generate
  ```

- 生成した ER 図をブラウザで開く:
  ```
  npx gulp schemaspy:open
  ```

- ER 図の生成と表示（一連の流れ）:
  ```
  npx gulp schemaspy
  ```

- 出力ディレクトリのクリーンアップ:
  ```
  npx gulp schemaspy:clean
  ```

- ER 図の再生成（クリーン後に生成）:
  ```
  npx gulp schemaspy:regenerate
  ```

生成された ER 図は `docs/assets/schemaspy-output/` ディレクトリに保存されます。

##### Heroku デプロイタスク

- バックエンドのデプロイ:
  ```
  npx gulp deploy:backend
  ```

- フロントエンドのデプロイ:
  ```
  npx gulp deploy:frontend
  ```

- 全アプリケーションの一括デプロイ:
  ```
  npx gulp deploy:all
  ```

- デプロイ状態の確認:
  ```
  npx gulp deploy:status
  ```

- バックエンドのログ表示:
  ```
  npx gulp deploy:backend:logs
  ```

- フロントエンドのログ表示:
  ```
  npx gulp deploy:frontend:logs
  ```

- アプリケーションをブラウザで開く:
  ```
  npx gulp deploy:open
  ```

個別のステップを実行する場合:
- `deploy:login` - Heroku Container Registry にログイン
- `deploy:backend:build` - バックエンドの Docker イメージをビルド
- `deploy:backend:push` - バックエンドのイメージをプッシュ
- `deploy:backend:release` - バックエンドをリリース
- `deploy:frontend:build` - フロントエンドの Docker イメージをビルド
- `deploy:frontend:push` - フロントエンドのイメージをプッシュ
- `deploy:frontend:release` - フロントエンドをリリース

#### GitHub Container Registry

このプロジェクトでは、GitHub Container Registry（GHCR）を使用して開発コンテナイメージを管理しています。

##### 自動ビルド・プッシュ

タグをプッシュすると、GitHub Actions が自動的にコンテナイメージをビルドし、GHCR にプッシュします。

```bash
# タグを作成してプッシュ
git tag 0.0.1
git push origin 0.0.1
```

##### イメージの取得・実行

GHCR からイメージを取得して実行するには：

```bash
# イメージをプル
docker pull ghcr.io/k2works/case-study-accounting:latest

# または特定バージョン
docker pull ghcr.io/k2works/case-study-accounting:0.0.1

# コンテナを実行
docker run -it -v $(pwd):/srv ghcr.io/k2works/case-study-accounting:latest
```

認証が必要な場合は、以下のコマンドでログインします：

```bash
# GitHub Personal Access Token でログイン
echo $GITHUB_TOKEN | docker login ghcr.io -u <username> --password-stdin
```

##### 権限設定

- リポジトリの Settings → Actions → General で `Read and write permissions` を設定
- `GITHUB_TOKEN` に `packages: write` 権限が付与されています

##### Dev Container の使用

VS Code で Dev Container を使用する場合：

1. VS Code で「Dev Containers: Reopen in Container」を実行
2. または「Dev Containers: Rebuild and Reopen in Container」で再ビルド

**[⬆ back to top](#目次)**

### 開発

開発手法については [CLAUDE.md](CLAUDE.md) および [docs/reference/開発ガイド.md](docs/reference/開発ガイド.md) を参照してください。

#### 開発の基本方針

- **TDD（テスト駆動開発）**: Red → Green → Refactor のサイクルに従う
- **構造変更と動作変更の分離**: コミットは単一の論理的作業単位で
- **品質保証**: すべてのテストがパスしてからコミット

**[⬆ back to top](#目次)**

### ドキュメント

プロジェクトのドキュメントは `docs/` ディレクトリで管理されています。

#### ドキュメント構成

| ディレクトリ   | 内容                               |
| :------------- | :--------------------------------- |
| `requirements` | 要件定義、ユースケース、ユーザーストーリー |
| `design`       | アーキテクチャ設計、データモデル設計       |
| `development`  | 開発ガイド、コーディング規約             |
| `operation`    | 運用手順、監視設定                     |
| `adr`          | アーキテクチャ決定記録（ADR）           |
| `reference`    | 参照ガイド、ベストプラクティス          |
| `template`     | ドキュメントテンプレート               |

詳細は [docs/index.md](docs/index.md) を参照してください。

**[⬆ back to top](#目次)**

## 参照

- [開発ガイド](docs/reference/開発ガイド.md)
- [よいソフトウェアとは](docs/reference/よいソフトウェアとは.md)
- [エクストリームプログラミング](docs/reference/エクストリームプログラミング.md)

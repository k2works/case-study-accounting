# 開発コンテナ構築手順書

## 概要

本ドキュメントは、会計システムの開発環境で使用する Docker コンテナの構築・運用手順書です。

## コンテナ一覧

| サービス名 | イメージ | ポート | 用途 |
|-----------|---------|--------|------|
| postgres | postgres:16-alpine | 5432 | アプリケーション用データベース |
| adminer | adminer:latest | 8888 | データベース管理 GUI |
| schemaspy | schemaspy/schemaspy:latest | - | データベーススキーマ可視化 |
| mkdocs | python:3.11-slim | 8000 | ドキュメントサーバー |
| plantuml | plantuml/plantuml-server:jetty | - | PlantUML レンダリング |
| sonarqube | sonarqube:community | 9000 | コード品質分析 |
| sonarqube-db | postgres:15 | - | SonarQube 用データベース |

## 前提条件

以下のツールがインストールされていること:

- Docker 24.x 以上
- Docker Compose v2 以上

## ディレクトリ構成

```
project-root/
├── docker-compose.yml            # Docker Compose 設定
├── docker/
│   ├── postgres/
│   │   └── init/
│   │       └── 01-init.sql       # PostgreSQL 初期化スクリプト
│   └── schemaspy/
│       └── Dockerfile            # SchemaSpy カスタムイメージ
├── docs/
│   └── Dockerfile                # MkDocs イメージ
├── schemaspy-output/             # SchemaSpy 出力先
└── site/                         # MkDocs ビルド出力先
```

## 環境変数

### PostgreSQL

| 変数名 | デフォルト値 | 説明 |
|--------|-------------|------|
| `POSTGRES_USER` | postgres | データベースユーザー |
| `POSTGRES_PASSWORD` | postgres | データベースパスワード |
| `POSTGRES_DB` | accounting | データベース名 |
| `POSTGRES_PORT` | 5432 | ホスト側ポート |

### Adminer

| 変数名 | デフォルト値 | 説明 |
|--------|-------------|------|
| `ADMINER_PORT` | 8888 | ホスト側ポート |
| `DB_TYPE` | postgres | デフォルト接続先 |

### SonarQube

SonarQube は専用の PostgreSQL データベース（sonarqube-db）を使用します。

| 設定 | 値 |
|------|-----|
| データベース名 | sonar |
| ユーザー名 | sonar |
| パスワード | sonar |

## 起動・停止

### 全サービス起動

```bash
docker compose up -d
```

### 全サービス停止

```bash
docker compose down
```

### 特定サービスのみ起動

```bash
# データベース関連のみ
docker compose up -d postgres adminer

# SonarQube のみ
docker compose up -d sonarqube sonarqube-db

# ドキュメントサーバーのみ
docker compose up -d mkdocs plantuml
```

### ログ確認

```bash
# 全サービス
docker compose logs -f

# 特定サービス
docker compose logs -f postgres
```

### サービス状態確認

```bash
docker compose ps
```

## 各サービス詳細

### PostgreSQL（アプリケーション用）

アプリケーションで使用するメインのデータベースです。

**接続情報:**

| 項目 | 値 |
|------|-----|
| ホスト | localhost |
| ポート | 5432 |
| データベース | accounting |
| ユーザー | postgres |
| パスワード | postgres |

**JDBC URL:**
```
jdbc:postgresql://localhost:5432/accounting
```

**初期化スクリプト:**

`docker/postgres/init/` 配下の SQL ファイルがコンテナ起動時に自動実行されます。

```sql
-- docker/postgres/init/01-init.sql
SET timezone = 'Asia/Tokyo';
```

**データ永続化:**

`postgres_data` ボリュームにデータが保存されます。

```bash
# ボリューム確認
docker volume ls | grep postgres_data

# データ完全削除（注意）
docker compose down -v
```

### Adminer

Web ベースのデータベース管理ツールです。

**アクセス URL:** http://localhost:8888

**ログイン情報:**

| 項目 | 値 |
|------|-----|
| システム | PostgreSQL |
| サーバー | postgres |
| ユーザー名 | postgres |
| パスワード | postgres |
| データベース | accounting |

### SchemaSpy

データベーススキーマをドキュメント化するツールです。

**実行方法:**

```bash
# PostgreSQL が起動していることを確認
docker compose up -d postgres

# SchemaSpy を実行（ワンショット）
docker compose run --rm schemaspy
```

**出力先:** `./schemaspy-output/`

**結果確認:**
```bash
# ブラウザで開く（Windows）
start schemaspy-output/index.html

# ブラウザで開く（Mac）
open schemaspy-output/index.html
```

**カスタム Dockerfile:**

日本語フォント対応のため、カスタムイメージを使用しています。

```dockerfile
# docker/schemaspy/Dockerfile
FROM schemaspy/schemaspy:latest

USER root
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    fonts-noto-cjk \
    fonts-noto-cjk-extra \
    fontconfig && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN fc-cache -f -v

ENV LANG=ja_JP.UTF-8 \
    LC_ALL=ja_JP.UTF-8

USER java
```

### MkDocs

プロジェクトドキュメントを提供するサーバーです。

**アクセス URL:** http://localhost:8000

**特徴:**
- ライブリロード対応
- PlantUML サーバー連携
- Material テーマ

**ローカル編集:**

`docs/` ディレクトリのファイルを編集すると、自動的にブラウザに反映されます。

**Dockerfile:**

```dockerfile
# docs/Dockerfile
FROM python:3.11-slim

WORKDIR /docs

RUN pip install --no-cache-dir \
    mkdocs \
    mkdocs-material \
    pymdown-extensions \
    plantuml-markdown

COPY mkdocs.yml /docs/

EXPOSE 8000

CMD ["mkdocs", "serve", "--dev-addr=0.0.0.0:8000"]
```

### PlantUML

MkDocs 内の PlantUML 図をレンダリングするサーバーです。

**内部 URL:** http://plantuml:8080/plantuml

MkDocs から自動的に参照されるため、直接アクセスする必要はありません。

### SonarQube

コード品質分析ツールです。

**アクセス URL:** http://localhost:9000

**初期ログイン:**

| 項目 | 値 |
|------|-----|
| ユーザー名 | admin |
| パスワード | admin |

初回ログイン後、パスワード変更を求められます。

**トークン生成:**

1. http://localhost:9000 にログイン
2. My Account > Security > Generate Tokens
3. トークン名を入力し「Generate」をクリック
4. 生成されたトークンをコピー

**バックエンド解析:**
```bash
cd apps/backend
./gradlew sonar -Dsonar.token=<your-token>
```

**フロントエンド解析:**
```bash
cd apps/frontend
# .env.local に SONAR_TOKEN を設定
npm run sonar
```

詳細は [SonarQube セットアップ](sonarqube_setup.md) を参照してください。

## ボリューム管理

### ボリューム一覧

| ボリューム名 | 用途 |
|-------------|------|
| `postgres_data` | PostgreSQL データ |
| `sonarqube_data` | SonarQube データ |
| `sonarqube_extensions` | SonarQube プラグイン |
| `sonarqube_logs` | SonarQube ログ |
| `sonarqube_db_data` | SonarQube DB データ |

### ボリューム操作

```bash
# ボリューム一覧
docker volume ls

# 特定ボリュームの詳細
docker volume inspect case-study-accounting_postgres_data

# 未使用ボリュームの削除
docker volume prune

# 全ボリューム削除（データ消失注意）
docker compose down -v
```

## ネットワーク

### ネットワーク構成

```
db_network (bridge)
├── postgres
├── adminer
└── schemaspy
```

PostgreSQL 関連サービスは `db_network` で接続されています。

### ネットワーク確認

```bash
# ネットワーク一覧
docker network ls

# ネットワーク詳細
docker network inspect case-study-accounting_db_network
```

## トラブルシューティング

### PostgreSQL が起動しない

```bash
# ログ確認
docker compose logs postgres

# ヘルスチェック状態確認
docker inspect accounting-postgres | grep -A 10 Health

# ボリューム削除して再起動
docker compose down -v
docker compose up -d postgres
```

### SonarQube が起動しない

Elasticsearch のメモリ設定が原因の場合があります。

**Linux/Mac:**
```bash
sudo sysctl -w vm.max_map_count=262144
```

**Windows (WSL2):**
```bash
wsl -d docker-desktop
sysctl -w vm.max_map_count=262144
```

### Adminer でログインできない

PostgreSQL コンテナ名を確認してください。

- サーバー名には `postgres`（コンテナ名）を指定
- `localhost` ではアクセスできません

### SchemaSpy でエラーが発生

PostgreSQL が healthy 状態であることを確認してください。

```bash
# ヘルスチェック確認
docker compose ps postgres

# 手動で接続確認
docker compose exec postgres psql -U postgres -d accounting -c "SELECT 1"
```

### ポート競合

既存のサービスとポートが競合している場合、環境変数で変更できます。

```bash
# .env ファイルで設定
POSTGRES_PORT=15432
ADMINER_PORT=18888
```

または直接指定:

```bash
POSTGRES_PORT=15432 docker compose up -d postgres
```

## 開発ワークフロー

### 日常的な開発

```bash
# 1. データベースと管理ツール起動
docker compose up -d postgres adminer

# 2. バックエンド開発
cd apps/backend
./gradlew bootRun

# 3. フロントエンド開発
cd apps/frontend
npm run dev
```

### ドキュメント作成

```bash
# MkDocs サーバー起動
docker compose up -d mkdocs plantuml

# ブラウザで確認
open http://localhost:8000
```

### コード品質チェック

```bash
# SonarQube 起動
docker compose up -d sonarqube sonarqube-db

# 起動完了まで待機（初回は数分かかる）
# http://localhost:9000 にアクセスして確認

# バックエンド解析
cd apps/backend
./gradlew sonar -Dsonar.token=<token>

# フロントエンド解析
cd apps/frontend
npm run sonar
```

### データベーススキーマ確認

```bash
# SchemaSpy 実行
docker compose run --rm schemaspy

# 結果確認
open schemaspy-output/index.html
```

## 参考資料

- [Docker Compose 公式ドキュメント](https://docs.docker.com/compose/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
- [Adminer Docker Hub](https://hub.docker.com/_/adminer)
- [SchemaSpy](https://schemaspy.org/)
- [MkDocs](https://www.mkdocs.org/)
- [PlantUML Server](https://plantuml.com/server)
- [SonarQube](https://www.sonarsource.com/products/sonarqube/)

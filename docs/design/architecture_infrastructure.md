# インフラストラクチャアーキテクチャ設計

## 概要

本ドキュメントは、財務会計システムのインフラストラクチャアーキテクチャを定義します。
開発環境から本番環境まで、一貫した構成管理とデプロイメント戦略を提供します。

## システム構成

### 全体構成図

```plantuml
@startuml
title システム全体構成

cloud "Internet" as internet

rectangle "Load Balancer" as lb {
  (HTTPS終端)
  (ルーティング)
}

rectangle "Application Tier" as app {
  node "Frontend" as frontend {
    [React SPA]
    [Nginx]
  }

  node "Backend" as backend {
    [Spring Boot]
    [JVM]
  }
}

rectangle "Data Tier" as data {
  database "PostgreSQL" as db {
    [Primary]
  }
}

rectangle "Support Services" as support {
  [Adminer]
  [Prometheus]
  [Grafana]
}

internet --> lb
lb --> frontend
lb --> backend
backend --> db
support --> db
support --> backend
@enduml
```

---

## 開発環境

### Docker Compose 構成

```plantuml
@startuml
title 開発環境構成

rectangle "Docker Compose" as dc {
  node "postgres" as postgres {
    [PostgreSQL 16]
    (port: 5432)
  }

  node "mysql" as mysql {
    [MySQL 8.0]
    (port: 3306)
    (profile: mysql)
  }

  node "adminer" as adminer {
    [Adminer]
    (port: 8080)
  }

  node "backend" as backend {
    [Spring Boot]
    (port: 8081)
  }

  node "frontend" as frontend {
    [Vite Dev Server]
    (port: 3000)
  }
}

postgres --> adminer
mysql --> adminer
backend --> postgres
backend ..> mysql : optional
frontend --> backend : proxy
@enduml
```

### docker-compose.yml

```yaml
services:
  # PostgreSQL データベース
  postgres:
    image: postgres:16-alpine
    container_name: accounting-postgres
    restart: unless-stopped
    environment:
      POSTGRES_USER: ${POSTGRES_USER:-postgres}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-postgres}
      POSTGRES_DB: ${POSTGRES_DB:-accounting_system}
      TZ: 'Asia/Tokyo'
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-postgres}"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - accounting-network

  # MySQL データベース（代替オプション）
  mysql:
    image: mysql:8.0
    container_name: accounting-mysql
    restart: unless-stopped
    profiles:
      - mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-accounting_system}
      MYSQL_USER: ${MYSQL_USER:-user}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-password}
      TZ: 'Asia/Tokyo'
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - accounting-network

  # データベース管理ツール
  adminer:
    image: adminer:latest
    container_name: accounting-adminer
    restart: unless-stopped
    ports:
      - "${ADMINER_PORT:-8080}:8080"
    environment:
      ADMINER_DEFAULT_SERVER: postgres
    networks:
      - accounting-network
    depends_on:
      - postgres

  # バックエンドアプリケーション
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile.dev
    container_name: accounting-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/accounting_system
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    ports:
      - "${BACKEND_PORT:-8081}:8080"
    volumes:
      - ./backend:/app
      - gradle_cache:/home/gradle/.gradle
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - accounting-network

  # フロントエンドアプリケーション
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.dev
    container_name: accounting-frontend
    restart: unless-stopped
    environment:
      VITE_API_BASE_URL: http://backend:8080/api
    ports:
      - "${FRONTEND_PORT:-3000}:3000"
    volumes:
      - ./frontend:/app
      - /app/node_modules
    depends_on:
      - backend
    networks:
      - accounting-network

volumes:
  postgres_data:
  mysql_data:
  gradle_cache:

networks:
  accounting-network:
    driver: bridge
```

### 環境変数設定

**.env**:

```bash
# PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=accounting_system
POSTGRES_PORT=5432

# MySQL（代替オプション）
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=accounting_system
MYSQL_USER=user
MYSQL_PASSWORD=password
MYSQL_PORT=3306

# Application Ports
BACKEND_PORT=8081
FRONTEND_PORT=3000
ADMINER_PORT=8080

# Application Settings
SPRING_PROFILES_ACTIVE=dev
```

---

## データベース設計

### Flyway マイグレーション管理

```plantuml
@startuml
title Flyway マイグレーションフロー

[*] --> アプリ起動
アプリ起動 --> バージョンチェック
バージョンチェック --> マイグレーション実行 : 未適用あり
バージョンチェック --> 完了 : 全て適用済み
マイグレーション実行 --> バージョン記録
バージョン記録 --> バージョンチェック
完了 --> [*]

note right of バージョンチェック
  flyway_schema_history
  テーブルで管理
end note
@enduml
```

### マイグレーションファイル構成

```
src/main/resources/db/migration/
├── V001__create_account_master.sql
├── V002__create_account_structure.sql
├── V003__create_journal_tables.sql
├── V004__create_balance_tables.sql
├── V005__create_user_tables.sql
├── V006__create_audit_log.sql
└── V007__insert_initial_data.sql
```

### マイグレーションファイル例

```sql
-- V001__create_account_master.sql

-- 勘定科目種別の enum 型
DO $$ BEGIN
    CREATE TYPE account_type AS ENUM ('資産', '負債', '純資産', '収益', '費用');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 勘定科目マスタ
CREATE TABLE IF NOT EXISTS account (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_code VARCHAR(20) UNIQUE NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_abbr VARCHAR(50),
    account_kana VARCHAR(100),
    account_type account_type NOT NULL,
    bs_pl_type CHAR(1) NOT NULL CHECK (bs_pl_type IN ('B', 'P')),
    debit_credit_type CHAR(1) NOT NULL CHECK (debit_credit_type IN ('借', '貸')),
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス
CREATE INDEX idx_account_code ON account(account_code);
CREATE INDEX idx_account_type ON account(account_type);
CREATE INDEX idx_account_active ON account(is_active);

-- テーブルコメント
COMMENT ON TABLE account IS '勘定科目マスタ';
COMMENT ON COLUMN account.account_id IS '勘定科目ID（主キー）';
COMMENT ON COLUMN account.account_code IS '勘定科目コード';
COMMENT ON COLUMN account.account_name IS '勘定科目名';
COMMENT ON COLUMN account.account_type IS '勘定科目種別（資産/負債/純資産/収益/費用）';
COMMENT ON COLUMN account.bs_pl_type IS 'BS/PL区分（B:貸借対照表、P:損益計算書）';
COMMENT ON COLUMN account.debit_credit_type IS '貸借区分（借:借方、貸:貸方）';
COMMENT ON COLUMN account.version IS '楽観的ロック用バージョン';
```

---

## CI/CD パイプライン

### GitHub Actions ワークフロー

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
  :カバレッジレポート;
fork again
  :Frontend CI;
  :Node.js セットアップ;
  :依存関係インストール;
  :API クライアント生成;
  :Lint チェック;
  :テスト実行;
  :ビルド;
end fork

:アーティファクト保存;

if (main ブランチ?) then (yes)
  :Docker イメージビルド;
  :レジストリにプッシュ;
  :デプロイ;
else (no)
endif

stop
@enduml
```

### バックエンド CI

```yaml
# .github/workflows/backend-ci.yml
name: Backend CI

on:
  push:
    branches: [main, develop]
    paths:
      - 'backend/**'
  pull_request:
    branches: [main]
    paths:
      - 'backend/**'

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: test_db
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v3

      - name: Build with Gradle
        working-directory: ./backend
        run: ./gradlew build -x test

      - name: Run tests
        working-directory: ./backend
        run: ./gradlew test
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/test_db
          SPRING_DATASOURCE_USERNAME: postgres
          SPRING_DATASOURCE_PASSWORD: postgres

      - name: Run static analysis
        working-directory: ./backend
        run: ./gradlew check

      - name: Generate coverage report
        working-directory: ./backend
        run: ./gradlew jacocoTestReport

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: backend-coverage-report
          path: backend/build/reports/jacoco/test/html
```

### フロントエンド CI

```yaml
# .github/workflows/frontend-ci.yml
name: Frontend CI

on:
  push:
    branches: [main, develop]
    paths:
      - 'frontend/**'
      - 'openapi.yaml'
  pull_request:
    branches: [main]
    paths:
      - 'frontend/**'
      - 'openapi.yaml'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        working-directory: ./frontend
        run: npm ci

      - name: Generate API client
        working-directory: ./frontend
        run: npm run api:generate

      - name: Lint check
        working-directory: ./frontend
        run: npm run lint

      - name: Type check
        working-directory: ./frontend
        run: npm run type-check

      - name: Run tests
        working-directory: ./frontend
        run: npm run test:coverage

      - name: Build
        working-directory: ./frontend
        run: npm run build

      - name: Upload build artifact
        uses: actions/upload-artifact@v4
        with:
          name: frontend-build
          path: frontend/dist
```

---

## 本番環境構成（デモ用）

### Heroku Container 構成

本環境はデモ用途で、フロントエンドとバックエンドを別々の Heroku アプリとしてデプロイします。
バックエンドは H2 インメモリデータベースを使用し、Dyno 再起動時にデータはリセットされます。

```plantuml
@startuml
title デモ環境構成（Heroku Container）

cloud "Internet" as internet

cloud "Heroku" as heroku {
  rectangle "Frontend App" as frontendApp {
    node "case-study-accounting-frontend" as frontendDyno {
      [nginx]
      [React SPA]
    }
  }

  rectangle "Backend App" as backendApp {
    node "case-study-accounting-backend" as backendDyno {
      [Spring Boot]
      database "H2 In-Memory" as h2 {
        [デモデータ]
      }
    }
  }
}

internet --> frontendDyno : HTTPS
frontendDyno --> backendDyno : API リクエスト
backendDyno --> h2

note bottom of h2
  Dyno 再起動で
  データはリセット
end note
@enduml
```

### デプロイ対象

| アプリケーション | Heroku アプリ名 | URL |
|------------------|-----------------|-----|
| フロントエンド | case-study-accounting-frontend | https://case-study-accounting-frontend-2cb4e7e16f2f.herokuapp.com |
| バックエンド | case-study-accounting-backend | https://case-study-accounting-backend-8d23bb5e8bbe.herokuapp.com |

### デモ環境の特徴

| 項目 | 内容 |
|------|------|
| フロントエンド | React SPA + nginx |
| バックエンド | Spring Boot + H2 |
| データベース | H2 インメモリ |
| データ永続性 | なし（再起動でリセット） |
| 初期データ | 起動時に自動投入 |
| 用途 | デモ、プレゼンテーション、機能確認 |
| Dyno タイプ | Eco または Basic |

### Dockerfile（バックエンド）

```dockerfile
# apps/backend/Dockerfile
FROM gradle:ubi10 AS builder

WORKDIR /app
COPY ./ ./
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]
```

### Dockerfile（フロントエンド）

```dockerfile
# apps/frontend/Dockerfile
FROM node:22-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf.template
COPY start.sh /start.sh
RUN sed -i 's/\r$//' /start.sh && chmod +x /start.sh

EXPOSE 80

CMD ["/bin/sh", "/start.sh"]
```

### GitHub Actions デプロイ

main ブランチへの push 時、または手動実行で自動デプロイされます。

| ワークフロー | ファイル | トリガー |
|--------------|----------|----------|
| Backend Deploy | `.github/workflows/backend-deploy.yml` | `apps/backend/**` 変更時 |
| Frontend Deploy | `.github/workflows/frontend-deploy.yml` | `apps/frontend/**` 変更時 |

#### 必要な GitHub Secrets

| シークレット名 | 内容 |
|----------------|------|
| `HEROKU_API_KEY` | Heroku API キー |

### 手動デプロイ（Gulp タスク）

```bash
# バックエンドのみ
npx gulp deploy:backend

# フロントエンドのみ
npx gulp deploy:frontend

# 両方
npx gulp deploy:all

# デプロイ状態確認
npx gulp deploy:status

# ログ確認
npx gulp deploy:backend:logs
npx gulp deploy:frontend:logs

# ブラウザで開く
npx gulp deploy:open
```

### 手動デプロイ（Docker コマンド）

```bash
# Heroku Container Registry にログイン
heroku container:login

# バックエンド
docker build -t registry.heroku.com/case-study-accounting-backend/web ./apps/backend
docker push registry.heroku.com/case-study-accounting-backend/web
heroku container:release web -a case-study-accounting-backend

# フロントエンド
docker build -t registry.heroku.com/case-study-accounting-frontend/web ./apps/frontend
docker push registry.heroku.com/case-study-accounting-frontend/web
heroku container:release web -a case-study-accounting-frontend
```

### 環境変数（Config Vars）

#### バックエンド

```bash
heroku config:set SPRING_PROFILES_ACTIVE=demo -a case-study-accounting-backend
heroku config:set JWT_SECRET=$(openssl rand -base64 32) -a case-study-accounting-backend
```

#### フロントエンド

```bash
heroku config:set API_URL=https://case-study-accounting-backend-8d23bb5e8bbe.herokuapp.com/api -a case-study-accounting-frontend
```

### ログ確認

```bash
# バックエンド
heroku logs --tail -a case-study-accounting-backend

# フロントエンド
heroku logs --tail -a case-study-accounting-frontend
```

### Dyno 再起動（データリセット）

```bash
heroku restart -a case-study-accounting-backend
```

### デモ環境の制約

| 制約 | 説明 |
|------|------|
| データ非永続 | Dyno 再起動でデータはリセット |
| シングル Dyno | 複数 Dyno でのデータ共有不可 |
| 同時アクセス | 大量アクセスには非対応 |
| 本番利用不可 | あくまでデモ・検証用途 |

### 関連ドキュメント

- [バックエンドデモ環境](../operation/backend_demo_env.md)
- [フロントエンドデモ環境](../operation/frontend_demo_env.md)
- [デモ環境デプロイ手順](../operation/deploy_demo.md)

---

## 監視・ログ（デモ用）

### シンプルなログ構成

デモ環境では Heroku 標準のログ機能のみを使用します。

```plantuml
@startuml
title デモ環境ログ構成

rectangle "Application" as app {
  [Dyno (web)]
  [stdout/stderr]
}

rectangle "Heroku Platform" as heroku {
  [Logplex]
}

rectangle "確認方法" as view {
  [heroku logs --tail]
  [Heroku Dashboard]
}

app --> [Logplex] : ログ出力
[Logplex] --> view
@enduml
```

### ログ確認コマンド

```bash
# リアルタイムログ
heroku logs --tail --app accounting-demo

# 直近のログ（100行）
heroku logs -n 100 --app accounting-demo

# アプリケーションログのみ
heroku logs --source app --app accounting-demo
```

### Spring Boot Actuator（デモ用）

```yaml
# application-demo.yml に追加
management:
  endpoints:
    web:
      exposure:
        include: health,info
      base-path: /actuator
  endpoint:
    health:
      show-details: always
```

```bash
# ヘルスチェック
curl https://accounting-demo.herokuapp.com/actuator/health
```

---

## セキュリティ

### セキュリティ対策

```plantuml
@startuml
title セキュリティレイヤー

rectangle "ネットワーク層" as network {
  (HTTPS/TLS)
  (WAF)
  (DDoS 対策)
}

rectangle "アプリケーション層" as app {
  (JWT 認証)
  (RBAC 認可)
  (入力バリデーション)
  (CSRF 対策)
  (XSS 対策)
}

rectangle "データ層" as data {
  (暗号化（at rest）)
  (暗号化（in transit）)
  (最小権限原則)
  (監査ログ)
}

network --> app
app --> data
@enduml
```

### セキュリティチェックリスト

| カテゴリ | 対策 | 実装 |
|---------|------|------|
| 認証 | JWT トークン | Spring Security |
| 認可 | ロールベースアクセス制御 | @PreAuthorize |
| 通信 | HTTPS 強制 | TLS 1.3 |
| 入力検証 | バリデーション | Bean Validation |
| SQL インジェクション | パラメータバインド | MyBatis |
| XSS | 出力エスケープ | React 自動エスケープ |
| CSRF | トークン検証 | Spring Security |
| 監査 | 操作ログ | AOP + DB |

---

## バックアップ・リカバリ（デモ用）

### デモ環境のデータ管理

デモ環境では H2 インメモリデータベースを使用するため、バックアップは不要です。

| 項目 | 内容 |
|------|------|
| バックアップ | 不要（インメモリ DB） |
| データリセット | Dyno 再起動で自動リセット |
| 初期データ復元 | 起動時に自動投入 |

### データリセット手順

```bash
# Dyno 再起動でデータをリセット
heroku restart --app accounting-demo

# または Heroku Dashboard から Restart Dyno
```

### 注意事項

- デモ中に入力したデータは Dyno 再起動で消失します
- 重要なデータは別途保存してください
- 本番運用にはこの構成を使用しないでください

---

## 技術スタック

| 技術 | 用途 |
|------|------|
| Docker | コンテナ化 |
| Docker Compose | ローカル開発環境 |
| PostgreSQL 16 | 開発・テスト用データベース |
| H2 Database | デモ環境データベース（インメモリ） |
| Flyway | DB マイグレーション（開発環境） |
| GitHub Actions | CI/CD |
| Heroku Container | デモ環境ホスティング |
| Heroku Logplex | ログ管理 |

---

## 運用チェックリスト

### デプロイ前チェック

- [ ] すべてのテストがパス
- [ ] 静的解析の警告がゼロ
- [ ] セキュリティスキャンに問題なし
- [ ] マイグレーションファイルの確認
- [ ] 環境変数の設定確認
- [ ] ロールバック手順の準備

### デプロイ後チェック

- [ ] ヘルスチェック OK
- [ ] ログにエラーなし
- [ ] 主要機能の動作確認
- [ ] メトリクスの正常性確認
- [ ] パフォーマンスの確認

# フロントエンドデモ環境構築手順書

## 概要

本ドキュメントは、財務会計システムのフロントエンドデモ環境について説明します。
デモ環境は React アプリケーションを nginx でホストし、バックエンド API にプロキシする構成です。

## デモ環境の特徴

| 項目 | 内容 |
|------|------|
| フレームワーク | React 18 + TypeScript |
| ビルドツール | Vite |
| Web サーバー | nginx (Alpine) |
| API 接続 | バックエンドへのリバースプロキシ |
| 用途 | デモ、プレゼンテーション、機能確認 |

## 技術スタック

- **Node.js**: 22 (ビルド時)
- **React**: 18.3
- **TypeScript**: 5.5
- **Vite**: 5.4
- **nginx**: Alpine
- **状態管理**: TanStack Query

## ローカル実行

### 前提条件

- Node.js 22 以上
- npm 10.x 以上

### 起動方法

```bash
cd apps/frontend
npm install
npm run dev
```

デフォルトで http://localhost:3000 で起動します。

### 本番ビルド

```bash
npm run build
npm run preview
```

## 設定ファイル

### 環境変数

#### .env.production

```
# API ベース URL（本番）- OpenAPI spec のパスに /api が含まれているため空
VITE_API_BASE_URL=

# アプリケーション名
VITE_APP_NAME=財務会計システム

# 開発モード
VITE_DEV_MODE=false

# MSW 有効化
VITE_ENABLE_MSW=false
```

### Dockerfile

`apps/frontend/Dockerfile` に以下の内容が配置されています:

```dockerfile
# ビルドステージ
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# 実行ステージ
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf.template
COPY start.sh /start.sh
RUN sed -i 's/\r$//' /start.sh && chmod +x /start.sh
EXPOSE 80
CMD ["/bin/sh", "/start.sh"]
```

### nginx.conf

nginx 設定ファイルでは以下を行います:

- SPA のルーティング対応（すべてのパスを index.html にフォールバック）
- API リクエストをバックエンドにプロキシ
- 静的ファイルのキャッシュ設定
- ヘルスチェックエンドポイント

```nginx
server {
    listen ${PORT};
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # SPA 用: すべてのルートを index.html にフォールバック
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API プロキシ
    location /api/ {
        proxy_pass ${API_URL}/;
        ...
    }

    # ヘルスチェック
    location /health {
        return 200 'OK';
    }
}
```

### start.sh

起動スクリプトで環境変数を nginx 設定に注入します:

```bash
#!/bin/sh
: "${PORT:=80}"
: "${API_URL:=http://localhost:8080/api}"
envsubst '${PORT} ${API_URL}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf
exec nginx -g 'daemon off;'
```

## Heroku デプロイ

### 前提条件

- Heroku CLI インストール済み
- Heroku アカウント作成済み
- Docker Desktop インストール済み
- バックエンドがデプロイ済み

### デプロイ手順（Docker コマンド使用・推奨）

#### 1. Heroku アプリ作成

```bash
heroku create アプリ名
```

#### 2. スタックを container に設定

```bash
heroku stack:set container -a アプリ名
```

#### 3. 環境変数設定

```bash
heroku config:set API_URL=https://バックエンドアプリ名.herokuapp.com/api -a アプリ名
```

#### 4. Heroku Container Registry にログイン

```bash
heroku container:login
```

#### 5. Docker ビルド

```bash
docker build -t registry.heroku.com/アプリ名/web apps/frontend
```

#### 6. プッシュ

```bash
docker push registry.heroku.com/アプリ名/web
```

#### 7. リリース

```bash
heroku container:release web -a アプリ名
```

#### 8. 動作確認

```bash
# ログ確認
heroku logs --tail -a アプリ名

# ヘルスチェック
curl https://アプリ名.herokuapp.com/health
```

## 環境変数一覧

| 変数名 | 説明 | 例 |
|--------|------|-----|
| `PORT` | nginx がリッスンするポート（Heroku が自動設定） | 自動 |
| `API_URL` | バックエンド API の URL | `https://backend.herokuapp.com/api` |

## トラブルシューティング

### start.sh: not found エラー

Windows で作成したシェルスクリプトの改行コード（CRLF）が原因です。
Dockerfile で `sed -i 's/\r$//' /start.sh` を実行して解決します。

### API 接続エラー

1. `API_URL` 環境変数が正しく設定されているか確認
2. バックエンドが起動しているか確認
3. CORS 設定を確認

### 画面が真っ白になる

1. ブラウザの開発者ツールでコンソールエラーを確認
2. ネットワークタブで API リクエストの状態を確認
3. nginx のエラーログを確認: `heroku logs --tail -a アプリ名`

### SPA ルーティングが動作しない

nginx 設定で `try_files $uri $uri/ /index.html;` が正しく設定されているか確認してください。

## 制約事項

| 制約 | 説明 |
|------|------|
| バックエンド依存 | API 機能はバックエンドに依存 |
| 環境変数 | ビルド時の環境変数は固定（ランタイムでは変更不可） |

## 関連ドキュメント

- [フロントエンド構築手順書](frontend_setup.md) - 開発環境のセットアップ
- [バックエンドデモ環境](backend_demo_env.md) - バックエンドデモ環境
- [フロントエンドアーキテクチャ](../design/architecture_frontend.md) - アーキテクチャ設計

# Frontend

財務会計システムのフロントエンドアプリケーション。

## 技術スタック

- React 18
- TypeScript
- Vite
- TanStack Query
- React Router
- Vitest + Testing Library

## セットアップ

```bash
npm install
```

## 開発サーバー起動

```bash
npm run dev
```

## 環境変数

Vite では `VITE_` プレフィックスを持つ環境変数のみがクライアントサイドで利用可能です。

### 環境変数ファイル

| ファイル | 用途 |
|---------|------|
| `.env.development` | 開発環境のデフォルト値 |
| `.env.production` | 本番環境のデフォルト値 |
| `.env.test` | テスト環境のデフォルト値 |
| `.env.local` | ローカル環境固有の設定（Git 管理外） |

### 変数一覧

| 変数名 | 必須 | デフォルト | 説明 |
|--------|------|-----------|------|
| `VITE_API_BASE_URL` | Yes | - | バックエンド API のベース URL。開発環境では `http://localhost:8080`、本番環境では空文字（同一オリジン） |
| `VITE_APP_NAME` | Yes | `財務会計システム` | アプリケーション名。ヘッダーに表示される |
| `VITE_DEV_MODE` | Yes | `false` | 開発モードフラグ。`true` でデバッグ機能が有効になる |
| `VITE_ENABLE_MSW` | Yes | `false` | Mock Service Worker の有効化。`true` で API モックが有効になる |
| `VITE_DEMO_MODE` | No | - | デモモードフラグ。`true` でデモ用認証情報が自動入力される |
| `VITE_DEMO_USERNAME` | No | - | デモ用ユーザー名。ログインフォームに自動入力される |
| `VITE_DEMO_PASSWORD` | No | - | デモ用パスワード。ログインフォームに自動入力される |

### 環境別設定例

#### 開発環境 (.env.development)

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=財務会計システム
VITE_DEV_MODE=true
VITE_ENABLE_MSW=false
VITE_DEMO_USERNAME=admin
VITE_DEMO_PASSWORD=Password123!
```

#### 本番環境 (.env.production)

```env
VITE_API_BASE_URL=
VITE_APP_NAME=財務会計システム
VITE_DEV_MODE=false
VITE_ENABLE_MSW=false
```

#### ローカル環境 (.env.local)

`.env.local` ファイルは Git 管理外です。ローカル固有の設定が必要な場合に使用します。

```env
# SonarQube 設定（オプション）
SONAR_HOST_URL=http://localhost:9000
SONAR_TOKEN=your-token-here

# 環境変数のオーバーライド
VITE_API_BASE_URL=http://192.168.1.100:8080
```

### 変数の使用方法

コード内で環境変数にアクセスする方法:

```typescript
// 直接アクセス
const apiUrl = import.meta.env.VITE_API_BASE_URL;

// config.ts 経由（推奨）
import { config } from './config';
console.log(config.apiBaseUrl);
console.log(config.appName);
```

### セキュリティ注意事項

- `VITE_` プレフィックスを持つ変数はビルド時にバンドルに含まれ、クライアントサイドで公開されます
- パスワードや API キーなどの機密情報は本番環境では絶対に設定しないでください
- `VITE_DEMO_USERNAME` / `VITE_DEMO_PASSWORD` は開発・デモ環境専用です

## Docker ビルド

### 通常ビルド

```bash
docker build -t frontend .
```

### デモモードでのビルド

デモ環境向けにビルドする場合、`--build-arg` でデモ認証情報を渡します。

```bash
docker build \
  --build-arg VITE_DEMO_MODE=true \
  --build-arg VITE_DEMO_USERNAME=admin \
  --build-arg VITE_DEMO_PASSWORD=Password123! \
  -t frontend .
```

デモモードが有効な場合、ログイン画面にユーザー名とパスワードが自動入力されます。

### Heroku へのデプロイ

プロジェクトルートから gulp タスクを使用します。

```bash
# フロントエンドのみデプロイ（デモモード有効）
gulp deploy:frontend

# 全アプリケーションをデプロイ
gulp deploy:all
```

デプロイ時のデモ認証情報は環境変数で変更できます。

```bash
VITE_DEMO_USERNAME=myuser VITE_DEMO_PASSWORD=mypassword gulp deploy:frontend
```

## スクリプト

### 開発

| コマンド | 説明 |
|---------|------|
| `npm run dev` | 開発サーバー起動 |
| `npm run dev:e2e` | E2E テスト用開発サーバー起動（MSW 有効） |
| `npm run build` | 本番ビルド |
| `npm run preview` | ビルド結果のプレビュー |
| `npm run setup` | 初期セットアップ（install + checkAndFix） |

### テスト

| コマンド | 説明 |
|---------|------|
| `npm test` | テスト実行（watch モード） |
| `npm run test:run` | テスト実行（一度のみ） |
| `npm run test:ui` | テスト UI 起動 |
| `npm run test:coverage` | カバレッジ付きテスト |

### E2E テスト

| コマンド | 説明 |
|---------|------|
| `npm run cypress` | Cypress をインタラクティブモードで起動 |
| `npm run cypress:run` | Cypress をヘッドレスモードで実行 |
| `npm run e2e` | E2E テスト実行（cypress:run のエイリアス） |
| `npm run e2e:open` | E2E テストをインタラクティブモードで起動 |

### コード品質

| コマンド | 説明 |
|---------|------|
| `npm run lint` | ESLint 実行 |
| `npm run lint:fix` | ESLint 自動修正 |
| `npm run lint:report` | ESLint レポート出力（JSON） |
| `npm run format` | Prettier でフォーマット |
| `npm run format:check` | Prettier フォーマットチェック |
| `npm run depcruise` | 依存関係チェック |
| `npm run check` | 全チェック実行（Gulp） |
| `npm run checkAndFix` | 全チェック＆自動修正（Gulp） |

### API クライアント

| コマンド | 説明 |
|---------|------|
| `npm run api:generate` | OpenAPI から API クライアント生成 |
| `npm run api:fetch` | バックエンドから OpenAPI スキーマを取得して生成 |

### SonarQube

| コマンド | 説明 |
|---------|------|
| `npm run sonar` | SonarQube 分析実行（テスト + lint + スキャン） |
| `npm run sonar:ci` | CI 用 SonarQube 分析（スキャンのみ） |

### Gulp タスク

| コマンド | 説明 |
|---------|------|
| `npm run gulp` | Gulp タスク実行 |
| `npm run watch` | ファイル監視タスク |
| `npm run guard` | ガードタスク |

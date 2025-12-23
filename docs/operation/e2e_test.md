# E2E テスト実行手順書

## 概要

本ドキュメントは、会計システムのフロントエンド E2E（End-to-End）テストの実行方法を説明します。

E2E テストは Cypress を使用し、MSW（Mock Service Worker）によるモック API を使用することで、バックエンドサーバーなしでテストを実行できます。

## 技術スタック

| カテゴリ | 技術 | 用途 |
|---------|------|------|
| E2E テスト | Cypress | ブラウザ自動テスト |
| API モック | MSW | Service Worker によるリクエスト傍受 |
| 環境変数 | cross-env | クロスプラットフォーム対応 |

## 前提条件

- Node.js 20 以上
- npm 10 以上
- フロントエンドの依存関係がインストール済み（`npm install`）

## ディレクトリ構成

```
apps/frontend/
├── cypress/
│   ├── e2e/                    # E2E テストファイル
│   │   ├── auth/
│   │   │   └── login.cy.ts     # 認証テスト
│   │   └── sample.cy.ts        # サンプルテスト
│   ├── support/
│   │   ├── commands.ts         # カスタムコマンド
│   │   └── e2e.ts              # E2E サポート設定
│   └── tsconfig.json           # Cypress 用 TypeScript 設定
├── public/
│   └── mockServiceWorker.js    # MSW Service Worker（自動生成）
├── src/
│   └── mocks/
│       ├── browser.ts          # MSW ブラウザ設定
│       └── handlers.ts         # モックハンドラー定義
└── cypress.config.ts           # Cypress 設定
```

## 実行方法

### 1. MSW 有効モードで開発サーバーを起動

```bash
cd apps/frontend
npm run dev:e2e
```

このコマンドは `VITE_ENABLE_MSW=true` 環境変数を設定して Vite を起動します。MSW が有効になり、API リクエストがモックされます。

### 2. E2E テストを実行

#### ヘッドレスモード（CI 向け）

```bash
npm run e2e
# または
npm run cypress:run
```

#### インタラクティブモード（開発向け）

```bash
npm run e2e:open
# または
npm run cypress
```

### 3. ワンコマンド実行（開発サーバー + テスト）

開発サーバーとテストを別々のターミナルで実行する場合：

**ターミナル 1:**
```bash
npm run dev:e2e
```

**ターミナル 2:**
```bash
npm run e2e
```

## MSW モックハンドラー

### 認証モック

`src/mocks/handlers.ts` に定義されたモックハンドラー：

| ユーザー名 | パスワード | 結果 |
|-----------|-----------|------|
| `admin` | `Password123!` | ADMIN ロールでログイン成功 |
| `user` | `Password123!` | USER ロールでログイン成功 |
| `locked` | 任意 | アカウントロックエラー |
| `inactive` | 任意 | アカウント無効化エラー |
| その他 | 任意 | 認証エラー |

### モックトークン

MSW は有効な JWT 形式のトークンを返します。これにより、ページリロード後も認証状態が維持されます。

```typescript
// トークン生成関数
const createMockJwt = (sub: string, expiresInSeconds = 3600): string => {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(
    JSON.stringify({
      sub,
      exp: Math.floor(Date.now() / 1000) + expiresInSeconds,
      iat: Math.floor(Date.now() / 1000),
    })
  );
  const signature = 'mock-signature';
  return `${header}.${payload}.${signature}`;
};
```

## テストケース

### 認証テスト（`cypress/e2e/auth/login.cy.ts`）

#### US-AUTH-001: ログイン

| テストケース | 説明 |
|-------------|------|
| 正しい認証情報でログインできる | admin/Password123! でログイン成功 |
| 間違った認証情報でログインするとエラーが表示される | 不正なパスワードでエラー表示 |
| ユーザー名が空の場合はバリデーションエラーが表示される | 必須チェック |
| パスワードが空の場合はバリデーションエラーが表示される | 必須チェック |
| パスワードが8文字未満の場合はバリデーションエラーが表示される | 長さチェック |
| 未認証状態で保護されたページにアクセスするとログインページにリダイレクトされる | ルートガード |
| ログイン成功後、JWT トークンがローカルストレージに保存される | トークン永続化 |
| 一般ユーザーでログインできる | user/Password123! でログイン成功 |

#### US-AUTH-002: ログアウト

| テストケース | 説明 |
|-------------|------|
| ログアウトボタンをクリックするとログアウトできる | ログアウト動作 |
| ログアウト後、ローカルストレージから認証情報がクリアされる | トークン削除 |
| ログアウト後、保護されたページにアクセスするとログインページにリダイレクトされる | セッション終了 |

#### 認証状態の維持

| テストケース | 説明 |
|-------------|------|
| ログイン後、ページをリロードしても認証状態が維持される | セッション永続化 |
| ログイン済みの状態でログインページにアクセスするとダッシュボードにリダイレクトされる | 認証済みリダイレクト |

## カスタムコマンド

`cypress/support/commands.ts` に定義されたカスタムコマンド：

```typescript
// ログイン
cy.login(username, password)

// ログアウト
cy.logout()

// 認証情報クリア
cy.clearAuth()
```

## トラブルシューティング

### MSW Service Worker が登録できない

```
[MSW] Failed to register the Service Worker
```

**解決方法:**
```bash
npx msw init public/ --save
```

### Windows で環境変数が認識されない

```
'VITE_ENABLE_MSW' は、内部コマンドまたは外部コマンドとして認識されていません
```

**解決方法:**
`cross-env` パッケージを使用する（`package.json` で設定済み）

### テストがタイムアウトする

- 開発サーバーが起動しているか確認
- `baseUrl` が正しいポートを指しているか確認

```bash
# デフォルトポート確認
curl http://localhost:3000
```

## CI/CD 統合

GitHub Actions での実行例：

```yaml
- name: Install dependencies
  run: npm ci
  working-directory: apps/frontend

- name: Start dev server
  run: npm run dev:e2e &
  working-directory: apps/frontend

- name: Wait for server
  run: npx wait-on http://localhost:3000

- name: Run E2E tests
  run: npm run e2e
  working-directory: apps/frontend
```

## 関連ドキュメント

- [フロントエンド構築手順書](frontend_setup.md)
- [フロントエンド開発ガイド](frontend_dev.md)
- [テスト戦略](../design/test_strategy.md)

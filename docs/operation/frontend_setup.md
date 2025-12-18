# フロントエンド構築手順書

## 概要

本ドキュメントは、会計システムのフロントエンド環境を構築するための手順書です。

## 技術スタック

| カテゴリ | 技術 | バージョン |
|---------|------|-----------|
| 言語 | TypeScript | ~5.5.0 |
| フレームワーク | React | ^18.3.0 |
| ビルドツール | Vite | ^5.4.0 |
| 状態管理 | TanStack Query | ^5.0.0 |
| ルーティング | React Router | ^6.26.0 |
| HTTP クライアント | Axios | ^1.7.0 |
| 日付処理 | Day.js | ^1.11.0 |
| 数値処理 | Decimal.js | ^10.4.0 |
| バリデーション | Zod | ^4.2.1 |
| 静的解析 | ESLint | ^9.0.0 |
| 静的解析 | eslint-plugin-sonarjs | ^3.0.5 |
| 循環参照検知 | dependency-cruiser | ^17.3.4 |
| フォーマッタ | Prettier | ^3.0.0 |
| 単体テスト | Vitest | ^2.0.0 |
| テストライブラリ | Testing Library | ^16.0.0 |
| API モック | MSW | ^2.0.0 |
| E2E テスト | Cypress | ^14.0.0 |
| カバレッジ | @vitest/coverage-v8 | ^2.0.0 |
| タスクランナー | Gulp | ^5.0.1 |
| API 生成 | Orval | ^7.0.0 |
| コード品質 | SonarQube Scanner | ^4.3.2 |

## 前提条件

以下のツールがインストールされていること:

- Node.js 20 以上
- npm 10 以上
- Git

## ディレクトリ構成

```
apps/frontend/
├── package.json              # パッケージ設定
├── tsconfig.json             # TypeScript 設定
├── tsconfig.node.json        # Node.js 用 TypeScript 設定
├── vite.config.ts            # Vite 設定
├── eslint.config.js          # ESLint 設定
├── .prettierrc               # Prettier 設定
├── .prettierignore           # Prettier 除外設定
├── .dependency-cruiser.cjs   # 循環参照検知設定
├── gulpfile.js               # Gulp タスク定義
├── .env.development          # 開発環境変数
├── .env.production           # 本番環境変数
├── index.html                # HTML エントリポイント
├── public/                   # 静的ファイル
├── cypress/                  # E2E テスト
│   ├── e2e/
│   └── support/
└── src/
    ├── main.tsx              # アプリケーションエントリポイント
    ├── App.tsx               # ルートコンポーネント
    ├── App.test.tsx          # App テスト
    ├── config.ts             # 設定
    ├── queryClient.ts        # TanStack Query 設定
    ├── vite-env.d.ts         # Vite 型定義
    ├── api/                  # API クライアント（Orval 生成）
    │   ├── generated/
    │   └── model/
    ├── components/           # 共通コンポーネント
    ├── features/             # 機能別モジュール
    ├── hooks/                # カスタムフック
    │   └── useAuth.ts
    ├── mocks/                # MSW モック
    │   ├── browser.ts
    │   ├── handlers.ts
    │   └── server.ts
    ├── pages/                # ページコンポーネント
    ├── providers/            # Context プロバイダー
    │   └── AuthProvider.tsx
    ├── styles/               # スタイル
    │   └── global.css
    ├── test/                 # テスト設定
    │   └── setup.ts
    ├── types/                # 型定義
    └── utils/                # ユーティリティ
```

## セットアップ手順

### 1. 依存関係のインストール

```bash
cd apps/frontend
npm install
```

### 2. 初期チェックの実行

```bash
npm run checkAndFix
```

### 3. 開発サーバーの起動

```bash
npm run dev
```

ブラウザで http://localhost:3000 にアクセス

### 4. 動作確認

```bash
# ビルド
npm run build

# テスト
npm run test:run

# Lint
npm run lint

# 循環参照チェック
npm run depcruise
```

## npm スクリプト一覧

### 基本コマンド

| コマンド | 説明 |
|---------|------|
| `npm run dev` | 開発サーバー起動 |
| `npm run build` | 本番ビルド |
| `npm run preview` | ビルド結果プレビュー |

### テスト

| コマンド | 説明 |
|---------|------|
| `npm run test` | テスト実行（ウォッチモード） |
| `npm run test:run` | テスト実行（1回） |
| `npm run test:ui` | テスト UI 起動 |
| `npm run test:coverage` | カバレッジ付きテスト |

### 品質チェック

| コマンド | 説明 |
|---------|------|
| `npm run lint` | ESLint 実行 |
| `npm run lint:fix` | ESLint 自動修正 |
| `npm run format` | Prettier フォーマット |
| `npm run format:check` | Prettier チェック |
| `npm run depcruise` | 循環参照チェック |

### E2E テスト

| コマンド | 説明 |
|---------|------|
| `npm run cypress` | Cypress GUI 起動 |
| `npm run cypress:run` | Cypress ヘッドレス実行 |

### API 生成

| コマンド | 説明 |
|---------|------|
| `npm run api:generate` | OpenAPI から API クライアント生成 |
| `npm run api:fetch` | OpenAPI 取得 + 生成 |

### タスクランナー（Gulp）

| コマンド | 説明 |
|---------|------|
| `npm run gulp` | Gulp タスク実行 |
| `npm run watch` | ファイル監視（テストのみ） |
| `npm run guard` | ファイル監視（lint + format + test） |
| `npm run check` | 全体チェック（修正なし） |
| `npm run checkAndFix` | 全体チェック（自動修正付き） |
| `npm run setup` | 初期セットアップ |

## Gulp タスク一覧

```bash
npx gulp --tasks
```

| タスク | 説明 |
|--------|------|
| `test` | テスト実行 |
| `coverage` | カバレッジ付きテスト |
| `lint` | ESLint 実行 |
| `lintFix` | ESLint 自動修正 |
| `format` | Prettier フォーマット |
| `formatCheck` | Prettier チェック |
| `depcruise` | 循環参照チェック |
| `build` | ビルド |
| `dev` | 開発サーバー |
| `check` | 全体チェック（修正なし） |
| `checkAndFix` | 全体チェック（自動修正付き） |
| `guard` | ファイル監視（自動 lint + format + test） |
| `watchTask` | ファイル監視（テストのみ） |
| `default` | checkAndFix + guard |

## 設定ファイル

### vite.config.ts

```typescript
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/api/generated/',
        'src/api/model/',
        'src/test/',
      ],
    },
  },
});
```

### tsconfig.json の主要設定

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "react-jsx",
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  }
}
```

## 静的解析設定

### ESLint (9.x + sonarjs)

主要ルール:
- 循環的複雑度: 最大 7
- 認知的複雑度: 最大 15（sonarjs/cognitive-complexity）
- `no-console`: warn（warn, error は許可）
- `no-debugger`: error
- `no-var`: error
- `prefer-const`: error

設定ファイル: `eslint.config.js`

```javascript
import sonarjs from 'eslint-plugin-sonarjs';

export default tseslint.config(
  // ...
  {
    plugins: {
      sonarjs,
    },
    rules: {
      // 循環的複雑度の制限 - 7を超える場合はエラー
      complexity: ['error', { max: 7 }],
      // 認知的複雑度の制限 - 15を超える場合はエラー
      'sonarjs/cognitive-complexity': ['error', 15],
      // ...
    },
  }
);
```

### dependency-cruiser

循環参照と未使用ファイルを検知します。

設定ファイル: `.dependency-cruiser.cjs`

主要ルール:
- `no-circular`: 循環参照を禁止（error）
- `no-orphans`: 未使用ファイルを警告（warn）
- `not-to-deprecated`: 非推奨モジュールへの依存を警告（warn）
- `no-non-package-json`: package.json 未記載パッケージへの依存を禁止（error）

実行:
```bash
npm run depcruise
```

依存関係の可視化（Graphviz が必要）:
```bash
npx depcruise src --include-only "^src" --output-type dot | dot -T svg > dependency-graph.svg
```

### Prettier

設定ファイル: `.prettierrc`

```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100
}
```

## テスト

### テスト構成

| 種類 | 対象 | ツール |
|------|------|--------|
| 単体テスト | コンポーネント、フック | Vitest, Testing Library |
| 統合テスト | API 連携 | Vitest, MSW |
| E2E テスト | ユーザーフロー | Cypress |

### Vitest 設定

```typescript
// vite.config.ts
test: {
  globals: true,
  environment: 'jsdom',
  setupFiles: ['./src/test/setup.ts'],
  include: ['src/**/*.{test,spec}.{ts,tsx}'],
}
```

### MSW（Mock Service Worker）

API モックの設定:

```typescript
// src/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('/api/auth/login', async ({ request }) => {
    const { username, password } = await request.json();
    if (username === 'admin' && password === 'password') {
      return HttpResponse.json({
        accessToken: 'mock-access-token',
        user: { id: 1, username: 'admin', role: 'ADMIN' },
      });
    }
    return new HttpResponse(null, { status: 401 });
  }),
];
```

### Cypress E2E テスト

```typescript
// cypress/e2e/sample.cy.ts
describe('App', () => {
  it('トップページにアクセスできる', () => {
    cy.visit('/');
    cy.contains('財務会計システム');
  });
});
```

## 開発ワークフロー

### Guard モード（推奨）

ファイル変更時に自動で lint、format、test を実行:

```bash
npm run guard
```

### TDD サイクル

1. テストを書く（Red）
2. テストをパスする最小限のコードを書く（Green）
3. リファクタリング（Refactor）

```bash
# テストウォッチモードで開発
npm run test
```

### コミット前チェック

```bash
npm run checkAndFix
```

## API クライアント生成

バックエンドの OpenAPI 仕様から TypeScript クライアントを自動生成:

```bash
# バックエンドが起動している状態で
npm run api:fetch
```

生成先:
- `src/api/generated/` - API クライアント
- `src/api/model/` - 型定義

## 環境変数

### .env.development

```
VITE_API_BASE_URL=http://localhost:8080
VITE_ENABLE_MSW=true
```

### .env.production

```
VITE_API_BASE_URL=/api
VITE_ENABLE_MSW=false
```

アプリケーション内での使用:

```typescript
// src/config.ts
export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || '/api',
  enableMsw: import.meta.env.VITE_ENABLE_MSW === 'true',
};
```

## トラブルシューティング

### Node.js バージョンエラー

**解決方法:**
Node.js 20 以上がインストールされていることを確認:
```bash
node -v
# v20.0.0 以上であること
```

### ESLint エラー

**sonarjs プラグインエラー:**
```bash
npm install -D eslint-plugin-sonarjs
```

### 循環参照エラー

dependency-cruiser で循環参照が検出された場合:

```bash
npm run depcruise
# error no-circular: src/module-a.ts → src/module-b.ts → src/module-a.ts
```

**解決方法:**
1. 依存性逆転の原則を適用
2. 共通モジュールを抽出
3. レイヤーアーキテクチャを導入

### Gulp エラー

```bash
# タスク一覧確認
npx gulp --tasks

# 個別タスク実行
npx gulp test
```

### ビルドエラー

```bash
# キャッシュクリア
rm -rf node_modules/.vite
npm run build
```

### テストエラー

```bash
# テスト環境リセット
rm -rf node_modules
npm install
npm run test:run
```

## SonarQube 連携

### 概要

SonarQube を使用してコード品質を継続的に監視します。

### 設定ファイル

`sonar-project.properties`:

```properties
sonar.projectKey=accounting-frontend
sonar.projectName=Accounting Frontend
sonar.sources=src
sonar.tests=src
sonar.test.inclusions=**/*.test.ts,**/*.test.tsx
sonar.exclusions=**/node_modules/**,**/dist/**,**/coverage/**
sonar.typescript.lcov.reportPaths=coverage/lcov.info
sonar.eslint.reportPaths=eslint-report.json
```

### 環境変数の設定

`.env.local.example` をコピーして `.env.local` を作成し、SonarQube の設定を行います。

```bash
cp .env.local.example .env.local
```

`.env.local`:
```properties
SONAR_HOST_URL=http://localhost:9000
SONAR_TOKEN=your-token-here
```

### 解析の実行

```bash
# SonarQube サーバーが起動していること

# 解析実行（カバレッジ + ESLint レポート生成 + SonarQube 送信）
npm run sonar
```

### npm スクリプト

| コマンド | 説明 |
|---------|------|
| `npm run sonar` | カバレッジ + ESLint レポート生成 + SonarQube 解析 |
| `npm run sonar:ci` | CI 用（レポート生成済みの場合） |
| `npm run lint:report` | ESLint レポートを JSON 形式で出力 |

### 結果の確認

http://localhost:9000/dashboard?id=accounting-frontend

## 参考資料

- [Vite 公式ドキュメント](https://vitejs.dev/)
- [React 公式ドキュメント](https://react.dev/)
- [TanStack Query ドキュメント](https://tanstack.com/query/latest)
- [Vitest ドキュメント](https://vitest.dev/)
- [Testing Library ドキュメント](https://testing-library.com/)
- [MSW ドキュメント](https://mswjs.io/)
- [Cypress ドキュメント](https://docs.cypress.io/)
- [ESLint ドキュメント](https://eslint.org/)
- [eslint-plugin-sonarjs](https://github.com/SonarSource/eslint-plugin-sonarjs)
- [dependency-cruiser](https://github.com/sverweij/dependency-cruiser)
- [Gulp ドキュメント](https://gulpjs.com/)
- [SonarScanner ドキュメント](https://docs.sonarsource.com/sonarqube-server/analyzing-source-code/scanners/sonarscanner/)

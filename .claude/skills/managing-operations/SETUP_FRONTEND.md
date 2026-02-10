# TypeScript/React 開発環境の統合セットアップ

ドキュメントベースの包括的 TypeScript/React 開発環境構築手順。

## 参照ドキュメント

- `docs/reference/TypeScriptアプリケーション環境構築ガイド.md`: TDD 基盤セットアップ手順と開発規律
- `@docs/design/tech_stack.md`: フロントエンド技術スタック選定理由と詳細仕様
- `@docs/design/architecture_frontend.md`: SPA アーキテクチャとコンポーネント設計詳細

## 初期設定プロセス

### 対話式プロジェクト設定

- プロジェクト名の確認（例: meeting-room-reservation-ui）
- パッケージ名の確認（例: @mrs/frontend）
- 作成場所の確認（デフォルト: `apps/frontend/` 配下）
- 開発ポートの確認（デフォルト: 3000）

### 注意点

- `docs/reference/TypeScriptアプリケーション環境構築ガイド.md` の Vite プロジェクトの初期化部分は参考にしない
- apps/frontend/app のような構成にしてはいけない
- apps/frontend/{project-name} のような構成すること
- ディレクトリだけの場合もコミットしたいので `.gitkeep` を入れる

## 構築される環境の詳細

### 基盤技術 (@docs/design/tech_stack.md 準拠)

- Node.js 20 LTS + TypeScript 5.4.0
- React 18.3.0 + React DOM 18.3.0
- Vite 5.2.0 (高速ビルドツール)
- Zustand 4.5.2 (クライアント状態管理)

### アーキテクチャ (@docs/design/architecture_frontend.md 準拠)

- SPA アーキテクチャ（Single Page Application）
- Container/Presentational パターン
- Custom Hooks による状態ロジック分離
- ディレクトリ構造: 11 フォルダ構成 (`components/,pages/,hooks/,services/,stores/,types/,utils/,constants/,assets/,styles/,__tests__/`)

### UI/UX 技術

- Tailwind CSS 3.4.3 (ユーティリティファースト CSS)
- React Hook Form 7.51.4 (フォーム管理)
- React Router 6.23.1 (ルーティング)
- Lucide React 0.378.0 (アイコンライブラリ)

### データ管理

- TanStack React Query 5.40.1 (サーバー状態管理)
- Axios 1.7.2 (HTTP クライアント)
- React Hook Form + Zod 3.23.8 (バリデーション)

### 品質管理ツール

- テスト: Vitest + @testing-library/react + @testing-library/jest-dom
- 静的解析: ESLint 8.57.0 + @typescript-eslint
- フォーマッター: Prettier 3.2.5
- カバレッジ: @vitest/coverage-v8 (80% 目標)
- 循環複雑度制限: 7 以下

### 開発支援機能

- Vite HMR (ホットモジュールリロード)
- TypeScript 厳格設定 (strict: true)
- ESLint + Prettier 統合
- Gulp Guard 機能（自動テスト・リント・フォーマット）
- TDD サイクル支援（Red-Green-Refactor）

## 連携シナリオ

```bash
# ドキュメント準拠の TypeScript/React 開発環境構築（対話式）
cat docs/reference/TypeScriptアプリケーション環境構築ガイド.md
cat docs/design/tech_stack.md
cat docs/design/architecture_frontend.md
# → プロジェクト名入力: "meeting-room-reservation-ui"
# → 作成場所確認: "apps/frontend/" (Enter でデフォルト採用)
# → パッケージ名確認: "@mrs/frontend" (自動生成)
# → 開発ポート確認: "3000" (Enter でデフォルト採用)
npm run dev
```

# AGENTS.md

AI コーディングエージェント向けのプロジェクトガイドラインです。

> **重要**: 詳細なガイドラインは `CLAUDE.md` を参照してください。

## プロジェクト概要

会計システムのケーススタディ - フルスタック Web アプリケーション

- **フロントエンド**: React + TypeScript + Vite (`apps/frontend/`)
- **バックエンド**: Spring Boot + Java (`apps/backend/`)
- **アーキテクチャ**: クリーンアーキテクチャ / DDD

## ビルド・テストコマンド

### フロントエンド (`apps/frontend/`)

```bash
npm install              # 依存関係インストール
npm run dev              # 開発サーバー起動
npm run build            # プロダクションビルド
npm run test:run         # テスト実行
npm run lint             # ESLint 実行
npm run format:check     # Prettier チェック
```

### バックエンド (`apps/backend/`)

```bash
./gradlew.bat build      # ビルド
./gradlew.bat test       # テスト実行
./gradlew.bat check      # Lint + テスト
```

### ルート

```bash
npm run frontend:check   # フロントエンド Lint + Format
npm run backend:check    # バックエンド Check
```

## コード規約

### 言語・スタイル

- **日本語**でコミュニケーション（技術用語は英語）
- 日本語と半角英数字の間に**半角スペース**を入れる
- **ですます調**、句読点は「。」「、」

### TypeScript (Frontend)

- ESLint + Prettier による自動フォーマット
- 関数コンポーネント + Hooks パターン
- `@/` パスエイリアスを使用

### Java (Backend)

- Checkstyle によるフォーマット
- クリーンアーキテクチャの層構造を遵守

## テスト指示

- **TDD サイクル**に従う: Red → Green → Refactor
- テストが通る状態でのみコミット
- フロントエンド: Vitest + React Testing Library
- バックエンド: JUnit 5 + Mockito

## コミット規約

- **Conventional Commits** 形式を使用
- 構造変更と動作変更を同一コミットに含めない
- 日本語でコミットメッセージを記述

```
feat: 新機能を追加
fix: バグを修正
docs: ドキュメントを更新
refactor: リファクタリング
test: テストを追加
```

## PR 指示

- すべてのテストがパスしていること
- Lint エラーがないこと
- 変更内容を簡潔に説明

## 参照ドキュメント

- `CLAUDE.md` - 詳細な AI エージェント実行ガイドライン
- `docs/reference/開発ガイド.md` - 開発ライフサイクル
- `docs/reference/よいソフトウェアとは.md` - 品質基準
- `.claude/README.md` - カスタムコマンド一覧

---
name: developing-frontend
description: フロントエンド開発の TDD ワークフロー。Red-Green-Refactor サイクル、アウトサイドインアプローチ、コンポーネント設計。React/TypeScript のフロントエンド実装時に使用。
---

# フロントエンド開発ガイド

TDD サイクルに従ったフロントエンド開発を支援します。

## Instructions

### 1. 参照ドキュメント

- @docs/reference/コーディングとテストガイド.md - ワークフロー
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ
- @docs/design/ui-design.md - UI 設計
- @docs/design/tech_stack.md - 技術スタック
- @docs/design/test_strategy.md - テスト戦略

### 2. TDD サイクルの実践

Red-Green-Refactor サイクルを厳密に実行：

1. **Red フェーズ**: 失敗するテストを最初に書く
2. **Green フェーズ**: テストを通す最小限のコードを実装
3. **Refactor フェーズ**: 重複を除去し設計を改善

### 3. アプローチ戦略の選択

- **アウトサイドイン**: UI から開始しロジックを段階的に実装（推奨）
- **インサイドアウト**: ユーティリティ/hooks から開始し上位層へ展開

### 4. テストコマンド

```bash
# 全テスト実行
cd apps/frontend && npm run test

# ウォッチモードでテスト実行
cd apps/frontend && npm run test:watch

# テストカバレッジ確認
cd apps/frontend && npm run test:coverage

# E2E テスト実行
cd apps/frontend && npm run test:e2e
```

### 5. 品質チェックリスト

- [ ] すべてのテストがパス
- [ ] ESLint の警告がゼロ
- [ ] テストカバレッジが目標を満たしている
- [ ] 単一の論理的作業単位を表現
- [ ] コミットメッセージが変更内容を明確に説明

### 6. コンテキスト管理

長時間の開発セッションでは Context limit reached エラーを回避するため、タスクの区切りごとに `/compact` を実施してコンテキストを圧縮する。

**`/compact` を実施するタイミング**:

- TDD サイクル（Red-Green-Refactor）を数回繰り返した後
- コンポーネント 1 件の実装が完了したとき
- コミット完了後、次のタスクに着手する前
- テストスイートの実行と結果確認が完了したとき

**運用ルール**:

1. `/compact` 実施前に、現在の作業状態と次のタスクをメモとして出力する
2. `/compact` 実施後、次のタスクの作業を継続する
3. 大規模なユーザーストーリーでは、サブタスクごとに `/compact` を検討する

### 7. 注意事項

- **前提条件**: Node.js/npm のテスト環境が設定済みであること
- **制限事項**: TDD の三原則を厳密に守る（テストなしでプロダクションコードを書かない）
- **推奨事項**: コミット前に必ず品質チェックリストを実行
- 作業完了後に対象のイテレーション @docs/development/iteration_plan-N.md の進捗を更新する

## Examples

### 新コンポーネントの TDD 実装

1. 失敗するテストを書く（Red）
2. テストを通す最小限のコードを実装（Green）
3. 重複を排除し設計を改善（Refactor）
4. 品質チェックリストを実行してコミット

### ベストプラクティス

1. **TODO 駆動開発**: タスクを細かい TODO に分割してから実装開始
2. **小さなサイクル**: Red-Green-Refactor を 10-15 分で完了させる
3. **継続的コミット**: 各サイクル完了時に動作する状態でコミット
4. **Rule of Three**: 同じコードが 3 回現れたらリファクタリング
5. **コンポーネント分割**: 単一責任の原則に従いコンポーネントを小さく保つ

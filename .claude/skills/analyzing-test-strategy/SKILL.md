---
name: analyzing-test-strategy
description: テスト戦略を策定。テストピラミッド設計、テスト種別の定義、カバレッジ目標の設定。テスト計画や品質戦略の検討時に使用。
---

# テスト戦略策定支援

ピラミッド型・ダイヤモンド型・逆ピラミッド型テストの選択を支援します。

## Instructions

### 1. 参照ドキュメント

- @docs/reference/テスト戦略ガイド.md - テスト戦略の進め方

### 2. 入力

- @docs/requirements/requirements_definition.md - 要件定義
- @docs/requirements/business_usecase.md - ビジネスユースケース
- @docs/requirements/system_usecase.md - システムユースケース
- @docs/requirements/user_story.md - ユーザーストーリー
- @docs/design/architecture_backend.md - バックエンドアーキテクチャ
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ

### 3. 成果物

- @docs/design/test_strategy.md - テスト戦略

### 4. 作業内容

#### テスト形状の選択

- ピラミッド型（ユニット重視）
- ダイヤモンド型（統合テスト重視）
- 逆ピラミッド型（E2E 重視）

#### テストレベルの定義

- ユニットテスト
- 統合テスト
- E2E テスト
- 受け入れテスト

#### テスト戦略の策定

- カバレッジ目標
- テストツールの選定
- CI/CD との連携

#### トレーサビリティの確保

- 要件とテストケースのマッピング

### 5. 注意事項

- **前提条件**: アーキテクチャ設計が完了していること
- **制限事項**: テスト戦略はアーキテクチャパターンに適合させること
- **推奨事項**: TDD/BDD の適用を検討する

### 6. 記述ルール

タスク項目などは一行開けて記述する。

OK:

```markdown
**受入条件**:

- [ ] テスト形状が選択されている
- [ ] カバレッジ目標が設定されている
```

NG:

```markdown
**受入条件**:
- [ ] テスト形状が選択されている
- [ ] カバレッジ目標が設定されている
```

## Examples

### アーキテクチャに適したテスト戦略の策定

1. バックエンド・フロントエンドのアーキテクチャを読み込む
2. @docs/reference/テスト戦略ガイド.md に基づいてテスト戦略を策定
3. テスト形状、テストレベル、カバレッジ目標を定義

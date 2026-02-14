---
name: analyzing-ui-design
description: UI 設計を支援。画面遷移図、画面イメージ、コンポーネント設計。UI/UX 設計やフロントエンド画面の検討時に使用。
---

# UI 設計支援

画面遷移図と画面イメージを PlantUML で設計します。

## Instructions

### 1. 参照ドキュメント

- @docs/reference/UI設計ガイド.md - UI 設計の進め方

### 2. 入力

- @docs/requirements/requirements_definition.md - 要件定義
- @docs/requirements/business_usecase.md - ビジネスユースケース
- @docs/requirements/system_usecase.md - システムユースケース
- @docs/requirements/user_story.md - ユーザーストーリー
- @docs/design/architecture_backend.md - バックエンドアーキテクチャ
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ

### 3. 成果物

- @docs/design/ui_design.md - UI 設計

### 4. 作業内容

#### 画面一覧作成

- ユースケースから必要な画面を識別
- 画面の目的と機能を定義

#### 画面遷移図作成

- PlantUML のステートチャート図を使用
- 画面間の遷移条件を定義

#### 画面イメージ作成

- PlantUML の salt 図を使用
- 入力項目・ボタン・表示項目のレイアウト

#### インタラクション設計

- ユーザー操作フローの定義
- エラー処理・フィードバックの設計

### 5. 注意事項

- **前提条件**: 要件定義とユースケースが完了していること
- **制限事項**:
  - 画面遷移には PlantUML のステートチャート図を使用すること
  - 画面イメージには PlantUML の salt 図を使用すること
- **推奨事項**: ユーザビリティを考慮し、一貫性のある UI を設計する

### 6. 記述ルール

タスク項目などは一行開けて記述する。

OK:

```markdown
**受入条件**:

- [ ] 画面一覧が作成されている
- [ ] 画面遷移図が作成されている
```

NG:

```markdown
**受入条件**:
- [ ] 画面一覧が作成されている
- [ ] 画面遷移図が作成されている
```

## Examples

### ユーザーストーリーに基づく画面設計

1. ユーザーストーリーとフロントエンドアーキテクチャを読み込む
2. @docs/reference/UI設計ガイド.md に基づいて設計
3. 画面一覧、画面遷移図、画面イメージを作成

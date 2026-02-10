---
name: analyzing-data-model
description: データモデル設計を支援。ER 図作成、テーブル定義、リレーション設計。データベース設計やテーブル構造の検討時に使用。
---

# データモデル設計支援

PlantUML の ER 図を使用してデータモデルを設計します。

## Instructions

### 1. 参照ドキュメント

- @docs/reference/データモデル設計ガイド.md - データモデル設計の進め方

### 2. 入力

- @docs/requirements/requirements_definition.md - 要件定義
- @docs/requirements/business_usecase.md - ビジネスユースケース
- @docs/requirements/system_usecase.md - システムユースケース
- @docs/requirements/user_story.md - ユーザーストーリー
- @docs/design/architecture_backend.md - バックエンドアーキテクチャ
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ

### 3. 成果物

- @docs/design/data-model.md - データモデル設計

### 4. 作業内容

#### 概念データモデル作成

- エンティティの識別
- リレーションシップの定義

#### 論理データモデル作成

- テーブル定義
- 主キー・外部キーの設計
- 正規化の適用

#### ER 図作成

- PlantUML を使用した ER 図の作成
- テーブル間の関係の可視化

### 5. 注意事項

- **前提条件**: 要件定義とアーキテクチャ設計が完了していること
- **制限事項**: PlantUML の ER 図を使用すること
- **推奨事項**: ドメインモデルとの整合性を確認しながら設計する

### 6. 記述ルール

タスク項目などは一行開けて記述する。

OK:

```markdown
**受入条件**:

- [ ] テーブル定義が完了している
- [ ] ER 図が作成されている
```

NG:

```markdown
**受入条件**:
- [ ] テーブル定義が完了している
- [ ] ER 図が作成されている
```

## Examples

### 要件に基づくデータモデル設計

1. 要件定義とバックエンドアーキテクチャを読み込む
2. @docs/reference/データモデル設計ガイド.md に基づいて設計
3. ドメインモデルとの整合性を確認しながらデータモデルを作成

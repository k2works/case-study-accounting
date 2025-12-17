## Analysis Data Model Command

データモデル設計を支援するコマンド。PlantUML の ER 図を使用してデータモデルを設計します。

### 使い方

```bash
/analysis-data-model
```

### 基本例

```bash
# データモデル設計の支援
/analysis-data-model
「要件とアーキテクチャに基づくデータモデル設計」

# 既存のアーキテクチャを考慮
cat docs/design/architecture_backend.md
/analysis-data-model
「バックエンドアーキテクチャに沿ったデータモデル設計」
```

### 詳細機能

#### データモデル設計サポート

@docs/reference/データモデル設計ガイド.md に基づくデータモデル設計ドキュメントを作成します。

**参照ドキュメント:**
- @docs/reference/データモデル設計ガイド.md - データモデル設計の進め方

**入力:**
- @docs/requirements/requirements_definition.md - 要件定義
- @docs/requirements/business_usecase.md - ビジネスユースケース
- @docs/requirements/system_usecase.md - システムユースケース
- @docs/requirements/user_story.md - ユーザーストーリー
- @docs/design/architecture_backend.md - バックエンドアーキテクチャ
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ

**成果物:**
- @docs/design/data-model.md - データモデル設計

#### 作業内容

1. **概念データモデル作成**
   - エンティティの識別
   - リレーションシップの定義

2. **論理データモデル作成**
   - テーブル定義
   - 主キー・外部キーの設計
   - 正規化の適用

3. **ER 図作成**
   - PlantUML を使用した ER 図の作成
   - テーブル間の関係の可視化

### Claude との連携

```bash
# 要件とアーキテクチャを読み込んでデータモデル設計
cat docs/requirements/requirements_definition.md
cat docs/design/architecture_backend.md
/analysis-data-model
「ドメインモデルと整合性のあるデータモデル設計」
```

### 注意事項

- **前提条件**: 要件定義とアーキテクチャ設計が完了していること
- **制限事項**: PlantUML の ER 図を使用すること
- **推奨事項**: ドメインモデルとの整合性を確認しながら設計する

### 関連コマンド

- `/analysis` : 分析フェーズ全体の支援
- `/analysis-domain-model` : ドメインモデル設計支援

## Analysis Domain Model Command

ドメインモデル設計を支援するコマンド。DDD の戦術的設計パターンに基づくドメインモデルを設計します。

### 使い方

```bash
/analysis-domain-model
```

### 基本例

```bash
# ドメインモデル設計の支援
/analysis-domain-model
「要件とアーキテクチャに基づくドメインモデル設計」

# 既存のユースケースを考慮
cat docs/requirements/system_usecase.md
/analysis-domain-model
「ユースケースに基づくドメインモデルの識別と設計」
```

### 詳細機能

#### ドメインモデル設計サポート

@docs/reference/ドメインモデル設計ガイド.md に基づくドメインモデル設計ドキュメントを作成します。

**参照ドキュメント:**
- @docs/reference/ドメインモデル設計ガイド.md - ドメインモデル設計の進め方

**入力:**
- @docs/requirements/requirements_definition.md - 要件定義
- @docs/requirements/business_usecase.md - ビジネスユースケース
- @docs/requirements/system_usecase.md - システムユースケース
- @docs/requirements/user_story.md - ユーザーストーリー
- @docs/design/architecture_backend.md - バックエンドアーキテクチャ
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ

**成果物:**
- @docs/design/domain-model.md - ドメインモデル設計

#### 作業内容

1. **エンティティ定義**
   - ライフサイクルを持つドメインオブジェクトの識別
   - 識別子の設計

2. **値オブジェクト定義**
   - 不変で識別子を持たないドメインオブジェクトの識別
   - バリデーションルールの定義

3. **集約の設計**
   - 集約ルートの識別
   - 集約境界の定義
   - 不変条件の設計

4. **ドメインサービス定義**
   - エンティティに属さないビジネスロジックの識別

5. **ダイアグラム作成**
   - PlantUML を使用したクラス図・オブジェクト図の作成

### Claude との連携

```bash
# ユースケースを読み込んでドメインモデル設計
cat docs/requirements/system_usecase.md
cat docs/requirements/user_story.md
/analysis-domain-model
「ユースケースから導出されるドメインモデルの設計」
```

### 注意事項

- **前提条件**: 要件定義とアーキテクチャ設計が完了していること
- **制限事項**: PlantUML を使用してダイアグラムを作成すること
- **推奨事項**: ユビキタス言語を使用してドメインエキスパートと共通認識を持つ

### 関連コマンド

- `/analysis` : 分析フェーズ全体の支援
- `/analysis-data-model` : データモデル設計支援

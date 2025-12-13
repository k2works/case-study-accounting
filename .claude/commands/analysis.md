## Analysis Command

分析フェーズの作業を支援するコマンド。要件定義から非機能要件まで包括的な分析作業を支援します。

### 使い方

```bash
/analysis [オプション]
```

### オプション

- なし : 分析フェーズ全体の支援表示
- `--requirements` : 要件定義関連の作業支援
- `--usecases` : ユースケース・ユーザーストーリー作成支援
- `--architecture` : アーキテクチャ設計支援
- `--data-model` : データモデル設計支援
- `--domain-model` : ドメインモデル設計支援
- `--ui-design` : UI設計支援
- `--test-strategy` : テスト戦略策定支援
- `--non-functional` : 非機能要件定義支援
- `--operation` : 運用要件定義支援
- `--tech-stack` : 技術スタック選定支援

### 基本例

```bash
# 分析フェーズ全体のワークフロー表示
/analysis
「分析フェーズの全体的な進め方と各工程の説明」

# 要件定義の支援
/analysis --requirements
「要件定義のRDRAモデルに基づいた体系的なアプローチの説明」

# アーキテクチャ設計の支援
/analysis --architecture
「業務領域とデータ構造の複雑さに基づくアーキテクチャパターンの選択支援」

# テスト戦略の策定
/analysis --test-strategy
「ピラミッド型・ダイヤモンド型・逆ピラミッド型テストの選択支援」
```

### 詳細機能

#### 要件定義サポート

@docs/reference/要件定義支援.md に基づく要件定義作成を支援します。
@docs/template/要件定義.md をテンプレートにして成果物を作成します。
@docs/template/要件定義.md をテンプレートは絶対に編集しないこと。

```bash
/analysis --requirements
```

- @docs/requirements/requirements_definition.md が無ければ新規要件定義を開始する
- システム価値の明確化
- システム外部環境の分析
- システム境界の定義
- システム内部構造の設計
- 成果物として @docs/requirements/requirements_definition.md を作成

#### ユースケースサポート

docs/reference/ユースケース作成ガイド.md に基づくユースケース作成を支援します。
@docs/template/完全形式のユースケース.md をテンプレートにして成果物を作成します。
@docs/template/完全形式のユースケース.md をテンプレートは絶対に編集しないこと。

```bash
/analysis --usecases
```

- @docs/requirements/requirements_definition.md からのユースケース抽出
- @docs/requirements/business_usecase.md を作成
- @docs/requirements/system_usecase.md を作成
- @docs/requirements/user_story.md を作成
- ユースケースとユーザーストーリーでトレーサビリティを維持する
- user_story.mdにはユーザーストーリーのみ記述する、リリース計画とイテレーション計画は別途作成する

#### アーキテクチャ設計サポート

@docs/reference/アーキテクチャ設計ガイド.md に基づくアーキテクチャ設計ドキュメントを作成します。
成果物は architecture_backend.md と architecture_frontend.md architecture_infrastructure.md です。

```bash
/analysis --architecture
```
- @docs/requirements/requirements_definition.md を参照
- @docs/requirements/business_usecase.md を参照
- @docs/requirements/system_usecase.md を参照
- @docs/requirements/user_story.md を参照
- バックエンドアーキテクチャ設計を実施して @docs/design/architecture_backend.md を作成
- フロンエンドアーキテクチャ設計を実施して @docs/design/architecture_frontend.md を作成
- インフラストラクチャアーキテクチャ設計を実施して @docs/design/architecture_infrastructure.md を作成

#### データモデル設計サポート

@docs/reference/データモデル設計ガイド.md に基づくデータモデル設計ドキュメントを作成します。
成果物は data-model.md です。
plantumlのER図を使います。

```bash
/analysis --data-model
```
- @docs/requirements/requirements_definition.md を参照
- @docs/requirements/business_usecase.md を参照
- @docs/requirements/system_usecase.md を参照
- @docs/requirements/user_story.md を参照
- @docs/design/architecture_backend.md を参照
- @docs/design/architecture_frontend.md を参照
- @docs/design/data-model.md を作成

#### ドメインモデル設計サポート

@docs/reference/ドメインモデル設計ガイド.md に基づくドメインモデル設計ドキュメントを作成します。
成果物は domain_model.md です。
ダイアグラムにはplantumlを使います。

```bash
/analysis --domain-model
```
- @docs/requirements/requirements_definition.md を参照
- @docs/requirements/business_usecase.md を参照
- @docs/requirements/system_usecase.md を参照
- @docs/requirements/user_story.md を参照
- @docs/design/architecture_backend.md を参照
- @docs/design/architecture_frontend.md を参照
- @docs/design/domain-model.md を作成

#### UI設計サポート

@docs/reference/UI設計ガイド.md に基づくUI設計ドキュメントを作成します。
成果物は ui_design.md です。
画面遷移にはplantumlのステートチャート図を使います。
画面イメージはplantumlのsalt図を使います。

```bash
/analysis --ui-design
```
- @docs/requirements/requirements_definition.md を参照
- @docs/requirements/business_usecase.md を参照
- @docs/requirements/system_usecase.md を参照
- @docs/requirements/user_story.md を参照
- @docs/design/architecture_backend.md を参照
- @docs/design/architecture_frontend.md を参照
- @docs/design/ui_design.md を作成

#### テスト戦略サポート

テスト戦略ドキュメントを作成します。
成果物は test-strategy.md です。

```bash
/analysis --test-strategy
```
- @docs/design/test_strategy.md を作成

#### 非機能要件サポート

非機能要件定義ドキュメントを作成します。
成果物は non-functional.md です。

```bash
/analysis --non-functional
```

- @docs/design/test_strategy.md を作成

#### 運用要件定義サポート

運用要件定義ドキュメントを作成します。
成果物は operation.md です。

```bash
/analysis --operation
```

- @docs/design/operation.md を作成

#### 技術スタックサポート

表形式の技術スタック一覧作成をサポートします

```bash
/analysis --tech-stack
```
- @docs/design/architecture_backend.md を参照
- @docs/design/architecture_frontend.md を参照
- @docs/design/tech_stack.md を作成

### Claude との連携

```bash
# プロジェクト情報の確認後に分析開始
ls -la docs/
cat README.md
/analysis
「プロジェクトの現状を踏まえた分析フェーズの進め方を提案」

# 既存の要件ドキュメントがある場合
cat docs/requirements_definition.md
/analysis --requirements
「既存要件を基にした詳細化とRDRAモデルへのマッピング」

# 技術的制約がある場合
cat package.json
/acat pom.xml
/analysis --tech-stack
「既存技術スタックを考慮したアーキテクチャ選択の提案」
```

### 注意事項

- **前提条件**: プロジェクトの基本的な背景情報の把握が必要
- **制限事項**: 分析結果は開発フェーズで継続的に見直し・改善が必要
- **推奨事項**: 各工程の成果物を文書化し、チーム内で共有することを推奨

### ベストプラクティス

1. **段階的分析**: 要件定義から始めて段階的に詳細化する
2. **チーム連携**: 分析結果をチーム全体で共有し、合意形成を行う
3. **継続的改善**: 開発フェーズでのフィードバックを基に分析結果を見直す
4. **文書化**: 分析結果はPlantUMLやMarkdownで視覚的に文書化する

### 関連コマンド

- `/plan` : 実装計画の策定
- `/spec` : 仕様書の作成・更新
- `/design-patterns` : 設計パターンの適用検討
- `/tech-debt` : 技術的負債の分析
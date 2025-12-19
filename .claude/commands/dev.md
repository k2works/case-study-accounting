## Development Guide Reference

開発フェーズ全体の作業を支援するコマンド。TDD サイクルに従った開発ワークフローを表示します。

### 使い方

```bash
/dev
```

### 基本例

```bash
# 開発フェーズ全体のワークフロー表示
/dev
「開発フェーズの全体的な進め方と TDD サイクルの説明」
```

### 詳細機能

#### 開発フェーズの全体像

開発フェーズは以下の工程で構成されます：

1. **バックエンド開発** (`/dev-backend`)
   - Java/Kotlin での実装
   - Gradle によるビルド・テスト
   - インサイドアウトアプローチ推奨

2. **フロントエンド開発** (`/dev-frontend`)
   - TypeScript/React での実装
   - npm によるビルド・テスト
   - アウトサイドインアプローチ推奨

#### TDD サイクルの実践

Red-Green-Refactor サイクルを厳密に実行：

1. **Red フェーズ**: 失敗するテストを最初に書く
2. **Green フェーズ**: テストを通す最小限のコードを実装
3. **Refactor フェーズ**: 重複を除去し設計を改善
4. @docs/reference/コーディングとテストガイド.md のワークフローに従う

#### 参照ドキュメント

- @docs/design/architecture_backend.md を参照
- @docs/design/architecture_frontend.md を参照
- @docs/design/data-model.md を参照
- @docs/design/domain-model.md を参照
- @docs/design/tech_stack.md を参照
- @docs/design/ui-design.md を参照
- @docs/design/test_strategy.md を参照
- 作業完了後に対象のイテレーション @docs/development/iteration_plan-N.md の進捗を更新する

#### アプローチ戦略の選択

プロジェクトの状態に応じた最適なアプローチを選択：

- **インサイドアウト**: データ層から開始し上位層へ展開（バックエンド推奨）
- **アウトサイドイン**: UI から開始しドメインロジックを段階的に実装（フロントエンド推奨）

### Claude との連携

```bash
# プロジェクト情報の確認後に開発開始
ls -la apps/
cat README.md
/dev
「プロジェクトの現状を踏まえた開発フェーズの進め方を提案」

# バックエンド開発の開始
/dev-backend --tdd
「User エンティティのテストから開始します」

# フロントエンド開発の開始
/dev-frontend --tdd
「LoginForm コンポーネントのテストから開始します」
```

### 注意事項

- **前提条件**: プロジェクトのテスト環境が設定済みであること
- **制限事項**: TDD の三原則を厳密に守る（テストなしでプロダクションコードを書かない）
- **推奨事項**: コミット前に必ず品質チェックリストを実行

### ベストプラクティス

1. **TODO 駆動開発**: タスクを細かい TODO に分割してから実装開始
2. **小さなサイクル**: Red-Green-Refactor を 10-15 分で完了させる
3. **継続的コミット**: 各サイクル完了時に動作する状態でコミット
4. **Rule of Three**: 同じコードが 3 回現れたらリファクタリング

### 関連コマンド

- `/dev-backend` : バックエンド開発ガイド
- `/dev-frontend` : フロントエンド開発ガイド

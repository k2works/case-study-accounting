## Development Guide Reference

コーディングとテストガイドを参照し、TDD サイクルに従った開発を支援します。

### 使い方

```bash
/dev [オプション]
```

### オプション

- なし : ガイド全体の要約と TDD サイクルの説明
- `--tdd` : TDD の Red-Green-Refactor サイクルの詳細
- `--approach` : インサイドアウト/アウトサイドインアプローチの選択指針
- `--checklist` : コミット前の品質チェックリスト表示
- `--refactor` : リファクタリングパターンの一覧
- `--test` : テスト作成のベストプラクティス

### 基本例

```bash
# TDD サイクルの開始
/dev --tdd
「現在のタスクに対して Red-Green-Refactor サイクルを開始」

# アプローチ戦略の確認
/dev --approach
「実装アプローチ（インサイドアウト/アウトサイドイン）の選択を支援」

# 品質チェックの実行
/dev --checklist
「コミット前の必須確認事項を順次実行」

# リファクタリング支援
/dev --refactor
「現在のコードに適用可能なリファクタリングパターンを提案」
```

### 詳細機能

#### TDD サイクルの実践

Red-Green-Refactor サイクルを厳密に実行：

1. **Red フェーズ**: 失敗するテストを最初に書く
2. **Green フェーズ**: テストを通す最小限のコードを実装
3. **Refactor フェーズ**: 重複を除去し設計を改善
4. @docs/reference/コーディングとテストガイド.md のワークフローに従う

```bash
# 新機能の TDD 実装開始
/dev --tdd
「User エンティティのテストから開始します」
```
- @docs/design/architecture_backend.md を参照
- @docs/design/architecture_frontend.md を参照
- @docs/design/data-model.md を参照
- @docs/design/domain-model.md を参照
- @docs/design/tech_stack.md を参照
- 作業完了後に対象のイテレーション @docs/development/iteration_plan-N.md の進捗を更新する

#### アプローチ戦略の選択

プロジェクトの状態に応じた最適なアプローチを選択：

- **インサイドアウト**: データ層から開始し上位層へ展開
- **アウトサイドイン**: UI から開始しドメインロジックを段階的に実装

### Claude との連携

```bash
# 現在のコードを分析してリファクタリング提案
cat src/User.java
/dev --refactor
「このクラスに適用可能なリファクタリングパターンを分析」

# テストカバレッジを確認してテスト追加
npm run test:coverage
/dev --test
「カバレッジが低い箇所のテストを追加」

# コミット前の品質確認
git status
/dev --checklist
「全ての品質基準を満たすまで確認を実行」
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

- `/task` : タスク管理と TODO リストの作成
- `/review` : コードレビューの実施
- `/test` : テスト実行と結果確認
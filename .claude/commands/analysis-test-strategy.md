## Analysis Test Strategy Command

テスト戦略の策定を支援するコマンド。ピラミッド型・ダイヤモンド型・逆ピラミッド型テストの選択を支援します。

### 使い方

```bash
/analysis-test-strategy
```

### 基本例

```bash
# テスト戦略の策定
/analysis-test-strategy
「ピラミッド型・ダイヤモンド型・逆ピラミッド型テストの選択支援」

# アーキテクチャを考慮したテスト戦略
cat docs/design/architecture_backend.md
/analysis-test-strategy
「アーキテクチャパターンに適したテスト戦略の策定」
```

### 詳細機能

#### テスト戦略サポート

テスト戦略ドキュメントを作成します。

**参照ドキュメント:**
- @docs/reference/テスト戦略ガイド.md - テスト戦略の進め方

**入力:**
- @docs/requirements/requirements_definition.md - 要件定義
- @docs/requirements/business_usecase.md - ビジネスユースケース
- @docs/requirements/system_usecase.md - システムユースケース
- @docs/requirements/user_story.md - ユーザーストーリー
- @docs/design/architecture_backend.md - バックエンドアーキテクチャ
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ

**成果物:**
- @docs/design/test_strategy.md - テスト戦略

#### 作業内容

1. **テスト形状の選択**
   - ピラミッド型（ユニット重視）
   - ダイヤモンド型（統合テスト重視）
   - 逆ピラミッド型（E2E 重視）

2. **テストレベルの定義**
   - ユニットテスト
   - 統合テスト
   - E2E テスト
   - 受け入れテスト

3. **テスト戦略の策定**
   - カバレッジ目標
   - テストツールの選定
   - CI/CD との連携

4. **トレーサビリティの確保**
   - 要件とテストケースのマッピング

### Claude との連携

```bash
# アーキテクチャを読み込んでテスト戦略策定
cat docs/design/architecture_backend.md
cat docs/design/architecture_frontend.md
/analysis-test-strategy
「バックエンド・フロントエンド両方を考慮したテスト戦略」
```

### 注意事項

- **前提条件**: アーキテクチャ設計が完了していること
- **制限事項**: テスト戦略はアーキテクチャパターンに適合させること
- **推奨事項**: TDD/BDD の適用を検討する
- 以下の記述ルールに従うこと
   - タスク項目などは一行開けて記述する
   - NG
  ```markdown
    **受入条件**:
    - [ ] ログアウトボタンをクリックするとログアウトできる
    - [ ] ログアウト後、ログイン画面に遷移する
    - [ ] JWT トークンが無効化される
  ```
   - OK
  ```markdown
    **受入条件**:
  
    - [ ] ログアウトボタンをクリックするとログアウトできる
    - [ ] ログアウト後、ログイン画面に遷移する
    - [ ] JWT トークンが無効化される
  ```

### 関連コマンド

- `/analysis` : 分析フェーズ全体の支援
- `/analysis-architecture` : アーキテクチャ設計支援

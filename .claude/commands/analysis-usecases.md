## Analysis Usecases Command

ユースケース・ユーザーストーリー作成を支援するコマンド。要件定義からユースケースを抽出し、トレーサビリティを維持します。

### 使い方

```bash
/analysis-usecases
```

### 基本例

```bash
# ユースケース作成の支援
/analysis-usecases
「要件定義からユースケースを抽出し、ユーザーストーリーを作成」

# 既存の要件定義を基にした作成
cat docs/requirements/requirements_definition.md
/analysis-usecases
「既存要件からのユースケース抽出とトレーサビリティ確保」
```

### 詳細機能

#### ユースケースサポート

@docs/reference/ユースケース作成ガイド.md に基づくユースケース作成を支援します。

**参照ドキュメント:**
- @docs/reference/ユースケース作成ガイド.md - ユースケース作成の進め方

**テンプレート:**
- @docs/template/完全形式のユースケース.md - ユースケーステンプレート（編集禁止）

**入力:**
- @docs/requirements/requirements_definition.md - 要件定義

**成果物:**
- @docs/requirements/business_usecase.md - ビジネスユースケース
- @docs/requirements/system_usecase.md - システムユースケース
- @docs/requirements/user_story.md - ユーザーストーリー

#### 作業内容

1. **ビジネスユースケース作成**
   - 要件定義からビジネスユースケースを抽出
   - アクターとユースケースの関係を定義

2. **システムユースケース作成**
   - ビジネスユースケースを詳細化
   - システム境界を明確化

3. **ユーザーストーリー作成**
   - システムユースケースからユーザーストーリーを導出
   - 受け入れ基準の定義

4. **トレーサビリティ維持**
   - ユースケースとユーザーストーリー間のトレーサビリティを確保

### Claude との連携

```bash
# 要件定義を読み込んでユースケース作成
cat docs/requirements/requirements_definition.md
/analysis-usecases
「要件定義に基づくユースケースの体系的な作成」
```

### 注意事項

- **前提条件**: @docs/requirements/requirements_definition.md が存在すること
- **制限事項**:
  - テンプレート @docs/template/完全形式のユースケース.md は絶対に編集しないこと
  - user_story.md にはユーザーストーリーのみ記述する
  - リリース計画とイテレーション計画は別途作成する
- **推奨事項**: ユースケースとユーザーストーリーでトレーサビリティを維持する

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
- `/analysis-requirements` : 要件定義関連の作業支援

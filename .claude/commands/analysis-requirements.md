## Analysis Requirements Command

要件定義関連の作業を支援するコマンド。RDRA モデルに基づいた体系的な要件定義を作成します。

### 使い方

```bash
/analysis-requirements
```

### 基本例

```bash
# 要件定義の支援
/analysis-requirements
「要件定義の RDRA モデルに基づいた体系的なアプローチの説明」

# 既存の要件ドキュメントがある場合
cat docs/requirements_definition.md
/analysis-requirements
「既存要件を基にした詳細化と RDRA モデルへのマッピング」
```

### 詳細機能

#### 要件定義サポート

@docs/reference/要件定義支援.md に基づく要件定義作成を支援します。

**参照ドキュメント:**
- @docs/reference/要件定義支援.md - 要件定義の進め方ガイド

**テンプレート:**
- @docs/template/要件定義.md - 要件定義テンプレート（編集禁止）

**成果物:**
- @docs/requirements/requirements_definition.md

#### 作業内容

1. **システム価値の明確化**
   - システムコンテキスト図の作成
   - 要求モデルの定義

2. **システム外部環境の分析**
   - ビジネスコンテキストの把握
   - ビジネスユースケースの識別
   - 業務フローの整理
   - 利用シーンの特定

3. **システム境界の定義**
   - ユースケース複合図の作成
   - 画面・帳票モデルの定義
   - イベントモデルの設計

4. **システム内部構造の設計**
   - 情報モデルの作成
   - 状態モデルの定義

### Claude との連携

```bash
# 新規プロジェクトで要件定義を開始
ls -la docs/
/analysis-requirements
「RDRA モデルに基づいた要件定義の開始」

# 既存要件の詳細化
cat docs/requirements/requirements_definition.md
/analysis-requirements
「既存要件の分析と改善提案」
```

### 注意事項

- **前提条件**: @docs/requirements/requirements_definition.md が無ければ新規要件定義を開始
- **制限事項**: テンプレート @docs/template/要件定義.md は絶対に編集しないこと
- **推奨事項**: ステークホルダーとの合意形成を行いながら進める
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
- `/analysis-usecases` : ユースケース・ユーザーストーリー作成支援

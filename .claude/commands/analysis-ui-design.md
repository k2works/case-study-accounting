## Analysis UI Design Command

UI 設計を支援するコマンド。画面遷移図と画面イメージを PlantUML で設計します。

### 使い方

```bash
/analysis-ui-design
```

### 基本例

```bash
# UI 設計の支援
/analysis-ui-design
「ユースケースに基づく画面設計と遷移図の作成」

# 既存のユーザーストーリーを考慮
cat docs/requirements/user_story.md
/analysis-ui-design
「ユーザーストーリーに基づく画面設計」
```

### 詳細機能

#### UI 設計サポート

@docs/reference/UI設計ガイド.md に基づく UI 設計ドキュメントを作成します。

**参照ドキュメント:**
- @docs/reference/UI設計ガイド.md - UI 設計の進め方

**入力:**
- @docs/requirements/requirements_definition.md - 要件定義
- @docs/requirements/business_usecase.md - ビジネスユースケース
- @docs/requirements/system_usecase.md - システムユースケース
- @docs/requirements/user_story.md - ユーザーストーリー
- @docs/design/architecture_backend.md - バックエンドアーキテクチャ
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ

**成果物:**
- @docs/design/ui_design.md - UI 設計

#### 作業内容

1. **画面一覧作成**
   - ユースケースから必要な画面を識別
   - 画面の目的と機能を定義

2. **画面遷移図作成**
   - PlantUML のステートチャート図を使用
   - 画面間の遷移条件を定義

3. **画面イメージ作成**
   - PlantUML の salt 図を使用
   - 入力項目・ボタン・表示項目のレイアウト

4. **インタラクション設計**
   - ユーザー操作フローの定義
   - エラー処理・フィードバックの設計

### Claude との連携

```bash
# ユーザーストーリーを読み込んで UI 設計
cat docs/requirements/user_story.md
cat docs/design/architecture_frontend.md
/analysis-ui-design
「ユーザー体験を考慮した画面設計」
```

### 注意事項

- **前提条件**: 要件定義とユースケースが完了していること
- **制限事項**:
  - 画面遷移には PlantUML のステートチャート図を使用すること
  - 画面イメージには PlantUML の salt 図を使用すること
- **推奨事項**: ユーザビリティを考慮し、一貫性のある UI を設計する
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

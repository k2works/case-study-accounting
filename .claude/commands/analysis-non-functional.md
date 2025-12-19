## Analysis Non-Functional Command

非機能要件の定義を支援するコマンド。性能、セキュリティ、可用性などの非機能要件を定義します。

### 使い方

```bash
/analysis-non-functional
```

### 基本例

```bash
# 非機能要件の定義
/analysis-non-functional
「性能・セキュリティ・可用性などの非機能要件定義」

# 既存の要件を考慮
cat docs/requirements/requirements_definition.md
/analysis-non-functional
「機能要件と整合性のある非機能要件の定義」
```

### 詳細機能

#### 非機能要件サポート

非機能要件定義ドキュメントを作成します。

**参照ドキュメント:**
- @docs/reference/非機能要件定義ガイド.md - 非機能要件定義の進め方

**入力:**
- @docs/requirements/requirements_definition.md - 要件定義
- @docs/design/architecture_backend.md - バックエンドアーキテクチャ
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ
- @docs/design/architecture_infrastructure.md - インフラストラクチャアーキテクチャ

**成果物:**
- @docs/design/non_functional.md - 非機能要件定義

#### 作業内容

1. **性能要件**
   - レスポンスタイム
   - スループット
   - 同時接続数

2. **セキュリティ要件**
   - 認証・認可
   - データ暗号化
   - 監査ログ

3. **可用性要件**
   - 稼働率目標
   - 障害復旧時間（RTO）
   - データ復旧時点（RPO）

4. **保守性要件**
   - ログ出力
   - 監視項目
   - アラート設定

5. **拡張性要件**
   - スケーラビリティ
   - 将来の拡張性

### Claude との連携

```bash
# アーキテクチャを読み込んで非機能要件定義
cat docs/design/architecture_infrastructure.md
/analysis-non-functional
「インフラアーキテクチャに基づく非機能要件の定義」
```

### 注意事項

- **前提条件**: 機能要件とアーキテクチャ設計が完了していること
- **制限事項**: 非機能要件は測定可能な形で定義すること
- **推奨事項**: SLA/SLO を明確に定義する
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
- `/analysis-operation` : 運用要件定義支援

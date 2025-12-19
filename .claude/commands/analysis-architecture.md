## Analysis Architecture Command

アーキテクチャ設計を支援するコマンド。業務領域とデータ構造の複雑さに基づくアーキテクチャパターンの選択を支援します。

### 使い方

```bash
/analysis-architecture
```

### 基本例

```bash
# アーキテクチャ設計の支援
/analysis-architecture
「業務領域とデータ構造の複雑さに基づくアーキテクチャパターンの選択支援」

# 技術的制約がある場合
cat package.json
cat pom.xml
/analysis-architecture
「既存技術スタックを考慮したアーキテクチャ選択の提案」
```

### 詳細機能

#### アーキテクチャ設計サポート

@docs/reference/アーキテクチャ設計ガイド.md に基づくアーキテクチャ設計ドキュメントを作成します。

**参照ドキュメント:**
- @docs/reference/アーキテクチャ設計ガイド.md - アーキテクチャ設計の進め方

**入力:**
- @docs/requirements/requirements_definition.md - 要件定義
- @docs/requirements/business_usecase.md - ビジネスユースケース
- @docs/requirements/system_usecase.md - システムユースケース
- @docs/requirements/user_story.md - ユーザーストーリー

**成果物:**
- @docs/design/architecture_backend.md - バックエンドアーキテクチャ
- @docs/design/architecture_frontend.md - フロントエンドアーキテクチャ
- @docs/design/architecture_infrastructure.md - インフラストラクチャアーキテクチャ

#### 作業内容

1. **バックエンドアーキテクチャ設計**
   - アーキテクチャパターンの選択（レイヤード、ヘキサゴナル、クリーン等）
   - CQRS/イベントソーシングの適用判断
   - API 設計方針

2. **フロントエンドアーキテクチャ設計**
   - フレームワーク選定
   - 状態管理パターン
   - コンポーネント設計方針

3. **インフラストラクチャアーキテクチャ設計**
   - クラウド/オンプレミス選定
   - コンテナ化戦略
   - CI/CD パイプライン設計

### Claude との連携

```bash
# 要件を読み込んでアーキテクチャ設計
cat docs/requirements/requirements_definition.md
cat docs/requirements/user_story.md
/analysis-architecture
「要件に基づく最適なアーキテクチャパターンの提案」
```

### 注意事項

- **前提条件**: 要件定義とユースケースが完了していること
- **制限事項**: アーキテクチャ決定は ADR（Architecture Decision Record）で記録すること
- **推奨事項**: 業務の複雑さとチームのスキルセットを考慮して選択する
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
- `/analysis-tech-stack` : 技術スタック選定支援

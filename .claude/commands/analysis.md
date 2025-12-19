## Analysis Command

分析フェーズ全体の作業を支援するコマンド。要件定義から非機能要件まで包括的な分析ワークフローを表示します。

### 使い方

```bash
/analysis
```

### 基本例

```bash
# 分析フェーズ全体のワークフロー表示
/analysis
「分析フェーズの全体的な進め方と各工程の説明」
```

### 詳細機能

#### 分析フェーズの全体像

分析フェーズは以下の工程で構成されます：

1. **要件定義** (`/analysis-requirements`)
   - システム価値の明確化
   - システム外部環境の分析
   - システム境界の定義

2. **ユースケース作成** (`/analysis-usecases`)
   - ビジネスユースケースの抽出
   - システムユースケースの定義
   - ユーザーストーリーの作成

3. **アーキテクチャ設計** (`/analysis-architecture`)
   - バックエンドアーキテクチャ
   - フロントエンドアーキテクチャ
   - インフラストラクチャアーキテクチャ

4. **データモデル設計** (`/analysis-data-model`)
   - ER 図の作成
   - テーブル定義

5. **ドメインモデル設計** (`/analysis-domain-model`)
   - エンティティ定義
   - 値オブジェクト定義
   - 集約の設計

6. **UI 設計** (`/analysis-ui-design`)
   - 画面遷移図
   - 画面イメージ

7. **テスト戦略** (`/analysis-test-strategy`)
   - テストピラミッド設計
   - テスト種別の定義

8. **非機能要件** (`/analysis-non-functional`)
   - 性能要件
   - セキュリティ要件

9. **運用要件** (`/analysis-operation`)
   - 運用フロー
   - 監視設計

10. **技術スタック** (`/analysis-tech-stack`)
    - 技術選定
    - バージョン管理

### Claude との連携

```bash
# プロジェクト情報の確認後に分析開始
ls -la docs/
cat README.md
/analysis
「プロジェクトの現状を踏まえた分析フェーズの進め方を提案」
```

### 注意事項

- **前提条件**: プロジェクトの基本的な背景情報の把握が必要
- **制限事項**: 分析結果は開発フェーズで継続的に見直し・改善が必要
- **推奨事項**: 各工程の成果物を文書化し、チーム内で共有することを推奨

### ベストプラクティス

1. **段階的分析**: 要件定義から始めて段階的に詳細化する
2. **チーム連携**: 分析結果をチーム全体で共有し、合意形成を行う
3. **継続的改善**: 開発フェーズでのフィードバックを基に分析結果を見直す
4. **文書化**: 分析結果は PlantUML や Markdown で視覚的に文書化する

### 関連コマンド

- `/analysis-requirements` : 要件定義関連の作業支援
- `/analysis-usecases` : ユースケース・ユーザーストーリー作成支援
- `/analysis-architecture` : アーキテクチャ設計支援
- `/analysis-data-model` : データモデル設計支援
- `/analysis-domain-model` : ドメインモデル設計支援
- `/analysis-ui-design` : UI 設計支援
- `/analysis-test-strategy` : テスト戦略策定支援
- `/analysis-non-functional` : 非機能要件定義支援
- `/analysis-operation` : 運用要件定義支援
- `/analysis-tech-stack` : 技術スタック選定支援

---
name: orchestrating-analysis
description: 分析フェーズ全体のワークフローをオーケストレーション。要件定義から非機能要件まで各 analyzing-* スキルの実行順序を案内。分析フェーズの開始や全体像の把握時に使用。
---

# 分析フェーズオーケストレーション

分析フェーズ全体の作業を支援します。要件定義から非機能要件まで包括的な分析ワークフローを提供します。

## Instructions

### 1. 分析フェーズの全体像

分析フェーズは以下の工程で構成されます:

1. **ビジネスアーキテクチャ分析** (Skill: `analyzing-business`)

   - ビジネスモデルキャンバスの作成
   - バリューストリームの設計
   - ビジネスケイパビリティモデルの構築

2. **要件定義** (Skill: `analyzing-requirements`)

   - システム価値の明確化
   - システム外部環境の分析
   - システム境界の定義

3. **ユースケース作成** (Skill: `analyzing-usecases`)

   - ビジネスユースケースの抽出
   - システムユースケースの定義
   - ユーザーストーリーの作成

4. **アーキテクチャ設計** (Skill: `analyzing-architecture`)

   - バックエンドアーキテクチャ
   - フロントエンドアーキテクチャ
   - インフラストラクチャアーキテクチャ

5. **データモデル設計** (Skill: `analyzing-data-model`)

   - ER 図の作成
   - テーブル定義

6. **ドメインモデル設計** (Skill: `analyzing-domain-model`)

   - エンティティ定義
   - 値オブジェクト定義
   - 集約の設計

7. **UI 設計** (Skill: `analyzing-ui-design`)

   - 画面遷移図
   - 画面イメージ

8. **テスト戦略** (Skill: `analyzing-test-strategy`)

   - テストピラミッド設計
   - テスト種別の定義

9. **非機能要件** (Skill: `analyzing-non-functional`)

   - 性能要件
   - セキュリティ要件

10. **運用要件** (Skill: `analyzing-operation`)

    - 運用フロー
    - 監視設計

11. **技術スタック** (Skill: `analyzing-tech-stack`)

    - 技術選定
    - バージョン管理

### 2. 連携シナリオ

```bash
# プロジェクト情報の確認後に分析開始
ls -la docs/
cat README.md
# 「プロジェクトの現状を踏まえた分析フェーズの進め方を提案」
```

### 3. 注意事項

- **前提条件**: プロジェクトの基本的な背景情報の把握が必要
- **制限事項**: 分析結果は開発フェーズで継続的に見直し・改善が必要
- **推奨事項**: 各工程の成果物を文書化し、チーム内で共有することを推奨

### 4. ベストプラクティス

1. **段階的分析**: 要件定義から始めて段階的に詳細化する
2. **チーム連携**: 分析結果をチーム全体で共有し、合意形成を行う
3. **継続的改善**: 開発フェーズでのフィードバックを基に分析結果を見直す
4. **文書化**: 分析結果は PlantUML や Markdown で視覚的に文書化する

### 関連スキル

- `analyzing-business` : ビジネスアーキテクチャ分析支援
- `analyzing-requirements` : 要件定義関連の作業支援
- `analyzing-usecases` : ユースケース・ユーザーストーリー作成支援
- `analyzing-architecture` : アーキテクチャ設計支援
- `analyzing-data-model` : データモデル設計支援
- `analyzing-domain-model` : ドメインモデル設計支援
- `analyzing-ui-design` : UI 設計支援
- `analyzing-test-strategy` : テスト戦略策定支援
- `analyzing-non-functional` : 非機能要件定義支援
- `analyzing-operation` : 運用要件定義支援
- `analyzing-tech-stack` : 技術スタック選定支援

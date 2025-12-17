## Analysis Operation Command

運用要件の定義を支援するコマンド。運用フロー、監視、バックアップなどの運用要件を定義します。

### 使い方

```bash
/analysis-operation
```

### 基本例

```bash
# 運用要件の定義
/analysis-operation
「運用フロー・監視・バックアップなどの運用要件定義」

# インフラアーキテクチャを考慮
cat docs/design/architecture_infrastructure.md
/analysis-operation
「インフラ構成に基づく運用要件の定義」
```

### 詳細機能

#### 運用要件サポート

運用要件定義ドキュメントを作成します。

**参照ドキュメント:**
- @docs/reference/運用要件定義ガイド.md - 運用要件定義の進め方

**入力:**
- @docs/requirements/requirements_definition.md - 要件定義
- @docs/design/architecture_infrastructure.md - インフラストラクチャアーキテクチャ
- @docs/design/non_functional.md - 非機能要件定義

**成果物:**
- @docs/design/operation.md - 運用要件定義

#### 作業内容

1. **運用フロー設計**
   - 日次運用
   - 月次運用
   - 年次運用

2. **監視設計**
   - 監視項目の定義
   - アラート閾値の設定
   - エスカレーションフロー

3. **バックアップ設計**
   - バックアップ方式
   - バックアップスケジュール
   - リストア手順

4. **障害対応設計**
   - 障害検知方法
   - 復旧手順
   - 連絡体制

5. **変更管理設計**
   - リリース手順
   - ロールバック手順
   - 変更承認フロー

### Claude との連携

```bash
# 非機能要件とインフラを読み込んで運用要件定義
cat docs/design/non_functional.md
cat docs/design/architecture_infrastructure.md
/analysis-operation
「SLA を満たすための運用要件の定義」
```

### 注意事項

- **前提条件**: 非機能要件とインフラアーキテクチャが完了していること
- **制限事項**: 運用手順は自動化を前提に設計すること
- **推奨事項**: IaC（Infrastructure as Code）を活用する

### 関連コマンド

- `/analysis` : 分析フェーズ全体の支援
- `/analysis-non-functional` : 非機能要件定義支援

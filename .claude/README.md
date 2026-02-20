# Claude Code Booster - Basic Template

Claude Code をより効率的に使うための基本設定テンプレートです。

このテンプレートは最小限の構成で、プロジェクトに合わせてカスタマイズできる基盤を提供します。

## 主要機能

3 つの機能で Claude Code の動作をカスタマイズできます。

- **Skills**: タスクに応じて自動発動する専門スキル（Progressive Disclosure）
- **Roles**: 専門家の視点で回答するための役割設定
- **Hooks**: 特定のタイミングでスクリプトを自動実行

---

## 機能一覧

### Skills（スキル）

`skills/` ディレクトリ内の `SKILL.md` ファイルとして保存されています。タスク内容に応じて自動的に発動し、必要な指示をコンテキストに読み込みます。

#### オーケストレーション

| スキル | 説明 |
| :--- | :--- |
| `orchestrating-analysis` | 分析フェーズ全体のワークフローをオーケストレーション。各 analyzing-* スキルの実行順序を案内。 |
| `orchestrating-development` | 開発フェーズ全体の TDD ワークフローをオーケストレーション。Codex 分業体制を案内。 |
| `orchestrating-project` | 計画・進捗管理フェーズ全体のワークフローをオーケストレーション。リリース計画、GitHub Project 同期、進捗追跡の実行順序を案内。 |

#### 分析系

| スキル | 説明 |
| :--- | :--- |
| `analyzing-business` | ビジネスアーキテクチャ分析を支援。ビジネスモデルキャンバス、バリューストリーム、ケイパビリティマップ等の作成。 |
| `analyzing-requirements` | RDRA 2.0 に基づいた体系的な要件定義を作成。 |
| `analyzing-usecases` | ユースケース・ユーザーストーリー作成を支援。 |
| `analyzing-architecture` | アーキテクチャパターンの選択と設計ドキュメント作成。 |
| `analyzing-data-model` | ER 図作成、テーブル定義、リレーション設計。 |
| `analyzing-domain-model` | エンティティ、値オブジェクト、集約の設計。 |
| `analyzing-ui-design` | 画面遷移図と画面イメージを設計。 |
| `analyzing-tech-stack` | フレームワーク、ライブラリ、インフラの選定と評価。 |
| `analyzing-test-strategy` | テストピラミッド設計、テスト種別の定義、カバレッジ目標の設定。 |
| `analyzing-non-functional` | 性能、セキュリティ、可用性、保守性の要件策定。 |
| `analyzing-operation` | 運用フロー、監視設計、障害対応手順の策定。 |

#### 開発系

| スキル | 説明 |
| :--- | :--- |
| `developing-backend` | バックエンド開発の TDD ワークフロー。インサイドアウトアプローチ。 |
| `developing-frontend` | フロントエンド開発の TDD ワークフロー。アウトサイドインアプローチ。 |
| `developing-release` | リリースワークフロー。品質ゲート、バージョンバンプ、CHANGELOG 生成、git commit + tag。 |

#### 計画・進捗系

| スキル | 説明 |
| :--- | :--- |
| `planning-releases` | アジャイルなリリース計画とイテレーション計画を作成・管理。 |
| `syncing-github-project` | リリース計画を GitHub Project・Issue・Milestone に同期。 |
| `tracking-progress` | プロジェクトの開発進捗を包括的に分析しレポート生成。 |

#### 運用系

| スキル | 説明 |
| :--- | :--- |
| `managing-operations` | 環境構築、デプロイ、監視、バックアップ、日次運用タスクの実行。 |
| `killing-processes` | 開発サーバーや Node.js プロセスを強制終了。ポート競合の解決。 |

#### ドキュメント・Git 系

| スキル | 説明 |
| :--- | :--- |
| `managing-docs` | 設計ドキュメントの一覧表示、進捗確認、インデックス更新、Markdown Lint。 |
| `git-commit` | 意味のある変更単位ごとにコミットを作成。Conventional Commits 準拠。 |
| `creating-adr` | Architecture Decision Record の作成を支援。 |

#### 共通

| スキル | 説明 |
| :--- | :--- |
| `ai-agent-guidelines` | AI Agent の実行ガイドライン。TDD サイクル、品質保証、完了報告のルール。 |

### Roles（役割設定）

`agents/roles/` ディレクトリ内の Markdown ファイルで定義されます。

現在、このテンプレートにはロールが含まれていません。必要に応じて `.md` ファイルを追加してください。

### Hooks（自動化スクリプト）

`settings.json` で設定して、開発作業を自動化できます。

| 実行スクリプト | イベント | 説明 |
| :--- | :--- | :--- |
| `deny-check.sh` | `PreToolUse` | `rm -rf /` のような危険なコマンドの実行を未然に防ぐ。 |
| `check-ai-commit.sh` | `PreToolUse` | `git commit` でコミットメッセージに AI の署名が含まれている場合にエラーを出す。 |
| `preserve-file-permissions.sh` | `PreToolUse` / `PostToolUse` | ファイル編集前に元の権限を保存し、編集後に復元する。 |
| `ja-space-format.sh` | `PostToolUse` | ファイル保存時に、日本語と英数字の間のスペースを自動で整形する。 |
| `auto-comment.sh` | `PostToolUse` | 新規ファイル作成時や大幅な編集時に、docstring の追加を促す。 |
| `(osascript)` | `Notification` | Claude がユーザーの確認を待っている時に、macOS の通知センターでお知らせする。 |
| `check-continue.sh` | `Stop` | タスク完了時に、継続可能なタスクがないか確認する。 |
| `(osascript)` | `Stop` | 全タスク完了時に、macOS の通知センターで完了を知らせる。 |

**注意**: スクリプトファイルは `scripts/` ディレクトリに配置する必要があります。このテンプレートには `.gitkeep` のみが含まれているため、実際のスクリプトは `~/.claude/scripts/` から参照するか、プロジェクトに合わせて作成してください。

---

## ディレクトリ構造

```
.claude/
├── agents/
│   └── roles/                          # 役割定義ファイル（.md）
├── assets/                             # 通知音などのアセット
├── scripts/                            # Hooks 用スクリプト
├── skills/                             # スキル定義（SKILL.md + 参照ファイル）
│   ├── ai-agent-guidelines/SKILL.md
│   ├── git-commit/SKILL.md
│   ├── creating-adr/SKILL.md
│   ├── analyzing-business/SKILL.md
│   ├── analyzing-requirements/SKILL.md
│   ├── analyzing-usecases/SKILL.md
│   ├── analyzing-architecture/SKILL.md
│   ├── analyzing-data-model/SKILL.md
│   ├── analyzing-domain-model/SKILL.md
│   ├── analyzing-ui-design/SKILL.md
│   ├── analyzing-tech-stack/SKILL.md
│   ├── analyzing-test-strategy/SKILL.md
│   ├── analyzing-non-functional/SKILL.md
│   ├── analyzing-operation/SKILL.md
│   ├── developing-backend/SKILL.md
│   ├── developing-frontend/SKILL.md
│   ├── developing-release/SKILL.md
│   ├── killing-processes/SKILL.md
│   ├── managing-operations/
│   │   ├── SKILL.md
│   │   ├── SETUP_JAVA.md
│   │   ├── SETUP_FRONTEND.md
│   │   ├── SETUP_CSHARP.md
│   │   └── DEPLOY.md
│   ├── managing-docs/SKILL.md
│   ├── tracking-progress/SKILL.md
│   ├── syncing-github-project/SKILL.md
│   ├── planning-releases/SKILL.md
│   ├── orchestrating-analysis/SKILL.md
│   ├── orchestrating-development/SKILL.md
│   └── orchestrating-project/SKILL.md
├── SKILLS_TEMPLATE.md                  # スキル作成テンプレート
├── README.md
├── settings.json                       # Claude Code 設定
└── settings.local.json                 # ローカル環境用設定
```

---

## カスタマイズ

- **スキルの追加**: `skills/<skill-name>/SKILL.md` を作成するだけです
- **ロールの追加**: `agents/roles/` に `.md` ファイルを追加するだけです
- **Hooks の編集**: `settings.json` を編集して、自動化処理を変更できます
- **スクリプトの追加**: `scripts/` にシェルスクリプトを追加し、`settings.json` で参照します

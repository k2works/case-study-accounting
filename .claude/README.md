# Claude Code Booster - Basic Template

Claude Code をより効率的に使うための基本設定テンプレートです。

このテンプレートは最小限の構成で、プロジェクトに合わせてカスタマイズできる基盤を提供します。

## 主要機能

3 つの機能で Claude Code の動作をカスタマイズできます。

- **Commands**: `/` で始まるカスタムコマンド
- **Roles**: 専門家の視点で回答するための役割設定
- **Hooks**: 特定のタイミングでスクリプトを自動実行

---

## 機能一覧

### Commands（カスタムコマンド）

`commands/` ディレクトリ内の Markdown ファイルとして保存されています。`/` に続けてファイル名を入力すると実行できます。

#### 分析系コマンド

| コマンド                   | 説明                                                       |
| :------------------------- | :--------------------------------------------------------- |
| `/analysis`                | 分析フェーズ全体の作業を支援。要件定義から非機能要件まで包括的な分析ワークフローを表示。 |
| `/analysis-requirements`   | RDRA モデルに基づいた体系的な要件定義を作成。              |
| `/analysis-usecases`       | ユースケース・ユーザーストーリー作成を支援。トレーサビリティを維持。 |
| `/analysis-architecture`   | 業務領域とデータ構造の複雑さに基づくアーキテクチャパターンの選択を支援。 |
| `/analysis-data-model`     | PlantUML の ER 図を使用してデータモデルを設計。            |
| `/analysis-domain-model`   | DDD の戦術的設計パターンに基づくドメインモデルを設計。     |
| `/analysis-ui-design`      | 画面遷移図と画面イメージを PlantUML で設計。               |
| `/analysis-tech-stack`     | 表形式の技術スタック一覧を作成。                           |
| `/analysis-test-strategy`  | ピラミッド型・ダイヤモンド型・逆ピラミッド型テストの選択を支援。 |
| `/analysis-non-functional` | 性能、セキュリティ、可用性などの非機能要件を定義。         |
| `/analysis-operation`      | 運用フロー、監視、バックアップなどの運用要件を定義。       |

#### 開発系コマンド

| コマンド        | 説明                                                         |
| :-------------- | :----------------------------------------------------------- |
| `/dev`          | 開発フェーズ全体の作業を支援。TDD サイクルに従った開発ワークフローを表示。 |
| `/dev-backend`  | バックエンド開発のためのコーディングとテストガイドを参照。   |
| `/dev-frontend` | フロントエンド開発のためのコーディングとテストガイドを参照。 |

#### 計画・進捗系コマンド

| コマンド       | 説明                                                         |
| :------------- | :----------------------------------------------------------- |
| `/plan`        | アジャイルなリリース計画とイテレーション計画を作成・管理。   |
| `/plan-github` | リリース計画を GitHub Project・Issue・Milestone に同期。     |
| `/progress`    | プロジェクトの開発進捗を包括的に確認し、現在の状況を詳細に報告。 |

#### 運用系コマンド

| コマンド | 説明                                                                   |
| :------- | :--------------------------------------------------------------------- |
| `/ops`   | アプリケーションの運用・構築・配置を統括的に管理。本番環境への安全かつ効率的なデプロイを実現。 |
| `/kill`  | 開発サーバーや Node.js プロセスを強制終了。複数ポートで起動している開発プロセスを一括停止。 |

#### ドキュメント・Git 系コマンド

| コマンド      | 説明                                               |
| :------------ | :------------------------------------------------- |
| `/docs`       | 設計ドキュメントの一覧表示、進捗確認、内容参照。   |
| `/git-commit` | 意味のある変更単位ごとにコミットを作成。           |

### Roles（役割設定）

`agents/roles/` ディレクトリ内の Markdown ファイルで定義されます。

現在、このテンプレートにはロールが含まれていません。必要に応じて `.md` ファイルを追加してください。

### Hooks（自動化スクリプト）

`settings.json` で設定して、開発作業を自動化できます。

| 実行スクリプト                 | イベント      | 説明                                                                 |
| :----------------------------- | :------------ | :------------------------------------------------------------------- |
| `deny-check.sh`                | `PreToolUse`  | `rm -rf /` のような危険なコマンドの実行を未然に防ぐ。                |
| `check-ai-commit.sh`           | `PreToolUse`  | `git commit` でコミットメッセージに AI の署名が含まれている場合にエラーを出す。 |
| `preserve-file-permissions.sh` | `PreToolUse` / `PostToolUse` | ファイル編集前に元の権限を保存し、編集後に復元する。 |
| `ja-space-format.sh`           | `PostToolUse` | ファイル保存時に、日本語と英数字の間のスペースを自動で整形する。     |
| `auto-comment.sh`              | `PostToolUse` | 新規ファイル作成時や大幅な編集時に、docstring の追加を促す。         |
| `(osascript)`                  | `Notification` | Claude がユーザーの確認を待っている時に、macOS の通知センターでお知らせする。 |
| `check-continue.sh`            | `Stop`        | タスク完了時に、継続可能なタスクがないか確認する。                   |
| `(osascript)`                  | `Stop`        | 全タスク完了時に、macOS の通知センターで完了を知らせる。             |

**注意**: スクリプトファイルは `scripts/` ディレクトリに配置する必要があります。このテンプレートには `.gitkeep` のみが含まれているため、実際のスクリプトは `~/.claude/scripts/` から参照するか、プロジェクトに合わせて作成してください。

---

## ディレクトリ構造

```
.claude/
├── agents/
│   └── roles/           # 役割定義ファイル（.md）
├── assets/              # 通知音などのアセット
├── commands/            # カスタムコマンド（.md）
│   ├── analysis.md
│   ├── analysis-architecture.md
│   ├── analysis-data-model.md
│   ├── analysis-domain-model.md
│   ├── analysis-non-functional.md
│   ├── analysis-operation.md
│   ├── analysis-requirements.md
│   ├── analysis-tech-stack.md
│   ├── analysis-test-strategy.md
│   ├── analysis-ui-design.md
│   ├── analysis-usecases.md
│   ├── dev.md
│   ├── dev-backend.md
│   ├── dev-frontend.md
│   ├── docs.md
│   ├── git-commit.md
│   ├── kill.md
│   ├── ops.md
│   ├── plan.md
│   ├── plan-github.md
│   └── progress.md
├── scripts/             # Hooks 用スクリプト
├── .gitignore
├── .mcp.json            # MCP サーバー設定
├── COMMAND_TEMPLATE.md  # コマンド作成テンプレート
├── README.md
├── settings.json        # Claude Code 設定
└── settings.local.json  # ローカル環境用設定
```

---

## カスタマイズ

- **コマンドの追加**: `commands/` に `.md` ファイルを追加するだけです
- **ロールの追加**: `agents/roles/` に `.md` ファイルを追加するだけです
- **Hooks の編集**: `settings.json` を編集して、自動化処理を変更できます
- **スクリプトの追加**: `scripts/` にシェルスクリプトを追加し、`settings.json` で参照します


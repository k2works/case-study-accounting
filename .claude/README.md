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

| コマンド       | 説明                                                         |
| :------------- | :----------------------------------------------------------- |
| `/analysis`    | 開発標準の分析ワークフローを実行する。要件定義からアーキテクチャ設計まで支援。 |
| `/dev`         | TDD サイクルに従った開発ワークフローを実行する。             |
| `/git-commit`  | 意味のある変更単位ごとにコミットを作成する。                 |
| `/kill`        | 開発サーバーや Node.js プロセスを強制終了する。              |
| `/ops`         | 環境構築・ビルド・デプロイなど運用ワークフローを実行する。   |
| `/plan`        | リリース計画とイテレーション計画を作成・管理する。           |
| `/progress`    | プロジェクトの開発進捗を包括的に確認する。                   |

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
│   ├── dev.md
│   ├── git-commit.md
│   ├── kill.md
│   ├── ops.md
│   ├── plan.md
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


---
name: developing-release
description: アプリケーションのリリースワークフロー。品質ゲート、バージョンバンプ、CHANGELOG 生成、git commit + tag を一貫実行。リリース作業やバージョン管理時に使用。
---

# リリースワークフローガイド

品質ゲート → バージョンバンプ → CHANGELOG 生成 → git commit + tag を一貫して実行するリリースワークフローを支援します。

## Instructions

### 1. 参照ドキュメント

- @docs/reference/リリースガイド.md - リリースワークフロー全体

### 2. リリースフロー

リリースは以下の順序で実行します:

1. **ドライラン**: CHANGELOG プレビューとバージョン計算を確認
2. **リリース種別選択**: patch / minor / major を選択
3. **品質ゲート（preflight）**: 全チェックを通過
4. **リリース実行**: バージョンバンプ → CHANGELOG 生成 → commit + tag
5. **リモートプッシュ**: コミットとタグをプッシュ
6. **デプロイ**: 必要に応じて本番デプロイ

### 3. オプション

- なし : ドライランを実行（デフォルト）
- `--dry-run` : CHANGELOG プレビュー + バージョン計算
- `--patch` : パッチリリース（バグ修正）
- `--minor` : マイナーリリース（新機能追加、後方互換あり）
- `--major` : メジャーリリース（破壊的変更）
- `--preflight` : 品質ゲートのみ実行
- `--deploy` : リリース + デプロイ（`--patch` / `--minor` / `--major` と併用）

### 4. 基本例

```bash
# ドライラン（プレビュー）
npm run release:dry-run

# パッチリリース
npm run release:patch

# マイナーリリース
npm run release:minor

# メジャーリリース
npm run release:major

# 品質ゲートのみ実行
npm run release:preflight

# パッチリリース + デプロイ
npm run release:deploy:patch
```

### 5. 品質ゲート（preflight）

リリース前に以下のチェックを直列実行します。全チェック通過が必須です:

| チェック | コマンド | 内容 |
| :--- | :--- | :--- |
| working tree クリーン | `release:preflight:clean` | 未コミット変更がないこと |
| 静的解析 | `release:preflight:lint` | lint エラーがないこと |
| ユニットテスト | `release:preflight:test` | 全テストパス |
| ビルド確認 | `release:preflight:build` | ビルド成功 |
| E2E テスト | `release:preflight:e2e` | E2E テスト全パス |

### 6. バージョニング規則

[Semantic Versioning](https://semver.org/) に従います:

| 種類 | 変更例 | バージョン変化 |
| :--- | :--- | :--- |
| `patch` | バグ修正、軽微な改善 | `0.1.0` → `0.1.1` |
| `minor` | 新機能追加（後方互換あり） | `0.1.0` → `0.2.0` |
| `major` | 破壊的変更 | `0.1.0` → `1.0.0` |

モノレポ構成の場合、全パッケージのバージョンを同期管理します。

### 7. CHANGELOG 生成ルール

[Conventional Commits](https://www.conventionalcommits.org/) に基づいて自動生成:

| prefix | カテゴリ |
| :--- | :--- |
| `feat` | Features |
| `fix` | Bug Fixes |
| `docs` | Documentation |
| `refactor` | Refactoring |
| `test` | Tests |
| `chore` | Chores |
| `perf` | Performance |
| `ci` | CI |
| `style` | Styles |
| `build` | Build |

- 直近の git tag から HEAD までのコミットを対象
- タグが存在しない場合は全コミット履歴を対象
- `CHANGELOG.md` の先頭に新しいエントリを追加

### 8. リリース実行ステップ

| ステップ | 内容 |
| :--- | :--- |
| [1/4] バージョン更新 | 対象パッケージのバージョンをセマンティックバージョニングに従って更新 |
| [2/4] CHANGELOG 生成 | 直近タグから HEAD までのコミットを分類し CHANGELOG.md を生成 |
| [3/4] git commit + tag | 変更ファイルをステージング → `release: vX.X.X` でコミット → `vX.X.X` タグ作成 |
| [4/4] サマリー表示 | バージョン変化、タグ名、次のステップを表示 |

### 9. トラブルシューティング

- **working tree がクリーンでない**: 変更をコミットまたは `git stash` してからリリース再実行
- **テスト失敗**: テストを修正してからリリースを再実行
- **リリースを元に戻したい**: リモートプッシュ前なら `git tag -d vX.X.X && git reset --hard HEAD~1`

### 10. コンテキスト管理

長時間のリリースセッションでは Context limit reached エラーを回避するため、タスクの区切りごとに `/compact` を実施してコンテキストを圧縮する。

**`/compact` を実施するタイミング**:

- 品質ゲート（preflight）の実行が完了したとき
- バージョンバンプと CHANGELOG 生成が完了したとき
- リリースコミット・タグ作成が完了したとき
- デプロイ完了後

**運用ルール**:

1. `/compact` 実施前に、現在のリリース状態と次のステップをメモとして出力する
2. `/compact` 実施後、次のステップの作業を継続する

### 11. 注意事項

- **前提条件**: ビルドツール・パッケージマネージャーがセットアップ済み、依存関係インストール済み
- **制限事項**: working tree がクリーンでないとリリース不可
- **推奨事項**: リリース前に必ずドライランで内容を確認

### 12. ベストプラクティス

1. **ドライランファースト**: 必ずドライランで CHANGELOG プレビューを確認してからリリース
2. **品質ゲート厳守**: preflight の全チェックを通過させてからリリース実行
3. **セマンティックバージョニング**: 変更内容に応じた適切なバージョン種別を選択
4. **Conventional Commits**: コミットメッセージを規約に従って記述し CHANGELOG の品質を確保
5. **段階的デプロイ**: リリースとデプロイを分離し、必要に応じてデプロイを実行

### 関連スキル

- `git-commit` : Conventional Commits 準拠のコミット作成
- `managing-operations` : デプロイ・運用管理
- `planning-releases` : リリース・イテレーション計画
- `orchestrating-development` : 開発フェーズ全体のワークフロー（リリースはこのフェーズの最終工程）

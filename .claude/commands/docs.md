## Docs Command

設計ドキュメントの一覧表示、進捗確認、内容参照を行うコマンド。

### 使い方

```bash
/docs [オプション]
```

### オプション

- なし : ドキュメント一覧と進捗状況を表示
- `--list` : ドキュメント一覧のみを表示
- `--status` : ドキュメントの作成状況を詳細表示
- `--read <ファイル名>` : 指定したドキュメントの内容を表示
- `--summary` : 全ドキュメントの概要を表示
- `--update` : `docs/index.md` と `mkdocs.yml` を現在のドキュメント構成に合わせて更新
- `--update-index` : `docs/index.md` のみを更新
- `--update-mkdocs` : `mkdocs.yml` のみを更新
- `--lint` : Markdown フォーマットをチェックし、違反を自動修正

### 基本例

```bash
# ドキュメント一覧と進捗確認
/docs
「設計ドキュメントの一覧と作成状況を確認」

# 一覧のみ表示
/docs --list
「docs/design/ と docs/requirements/ のファイル一覧を表示」

# 詳細な進捗状況
/docs --status
「各ドキュメントの完成度や更新日時を含む詳細情報を表示」

# 特定ドキュメントの参照
/docs --read tech_stack
「技術スタック選定ドキュメントの内容を表示」

# 全ドキュメントの概要
/docs --summary
「全ドキュメントの目的と主要な内容を要約表示」

# docs/index.md と mkdocs.yml を更新
/docs --update
「現在のドキュメント構成に合わせて両ファイルを更新」

# docs/index.md のみ更新
/docs --update-index
「docs/index.md を現在のドキュメント構成に合わせて更新」

# mkdocs.yml のみ更新
/docs --update-mkdocs
「mkdocs.yml の nav セクションを現在のドキュメント構成に合わせて更新」

# Markdown フォーマットをチェック・修正
/docs --lint
「タスク項目の前に空行がないなどのフォーマット違反を検出し自動修正」
```

### 詳細機能

#### ドキュメント構成

本プロジェクトのドキュメントは以下の構成で管理されています：

**要件定義ドキュメント** (`docs/requirements/`)
- `requirements_definition.md` : 要件定義書（RDRA 2.0）
- `business_usecase.md` : ビジネスユースケース
- `system_usecase.md` : システムユースケース
- `user_story.md` : ユーザーストーリー

**設計ドキュメント** (`docs/design/`)
- `architecture_backend.md` : バックエンドアーキテクチャ
- `architecture_frontend.md` : フロントエンドアーキテクチャ
- `architecture_infrastructure.md` : インフラストラクチャ
- `data-model.md` : データモデル設計
- `domain-model.md` : ドメインモデル設計
- `ui-design.md` : UI 設計
- `test_strategy.md` : テスト戦略
- `non_functional.md` : 非機能要件
- `operation.md` : 運用要件
- `tech_stack.md` : 技術スタック選定

#### ドキュメント参照

以下のコマンドで特定のドキュメントを参照できます：

```bash
# 技術スタックを参照
/docs --read tech_stack

# アーキテクチャを参照
/docs --read architecture_backend

# 要件定義を参照
/docs --read requirements_definition
```

#### ドキュメント更新機能

`--update` オプションを使用すると、現在のドキュメント構成に合わせて `docs/index.md` と `mkdocs.yml` を自動更新できます。

**docs/index.md の更新内容**:
- ドキュメント一覧をカテゴリ別に整理
- 各ドキュメントへのリンクと説明を生成
- 「まずこれを読もうリスト」形式で構成

**mkdocs.yml の更新内容**:
- `nav` セクションを現在のドキュメント構成に合わせて更新
- 要件定義、設計、開発、運用などのカテゴリで階層化
- 新しいドキュメントを自動的にナビゲーションに追加

```bash
# 更新前に差分を確認
/docs --status
「現在のドキュメント構成を確認」

# 両ファイルを更新
/docs --update
「docs/index.md と mkdocs.yml を更新」
```

#### Lint 機能

`--lint` オプションを使用すると、Markdown ドキュメントのフォーマットをチェックし、違反を自動修正できます。

**チェックルール**:

- タスク項目（リスト）の前には空行が必要
- 番号付きリストのサブリスト前にも空行が必要
- コロンで終わる行の直後にリストがある場合も空行が必要
- 太字ラベル（半角・全角コロン両方）の直後にリストがある場合も空行が必要

**NG 例 1** - ラベルの直後にリスト:

```markdown
**受入条件**:
- [ ] ログアウトボタンをクリックするとログアウトできる
- [ ] ログアウト後、ログイン画面に遷移する
```

**OK 例 1**:

```markdown
**受入条件**:

- [ ] ログアウトボタンをクリックするとログアウトできる
- [ ] ログアウト後、ログイン画面に遷移する
```

**NG 例 2** - 番号付きリストの直後にサブリスト:

```markdown
1. **対処**: SMB バージョンを確認
   - コントロールパネル > ファイルサービス > SMB で設定
```

**OK 例 2**:

```markdown
1. **対処**: SMB バージョンを確認

   - コントロールパネル > ファイルサービス > SMB で設定
```

**NG 例 3** - コロンで終わる行の直後にリスト:

```markdown
PMD はカスタム設定で以下をチェック：
- マジックナンバーの使用
- 空の catch ブロック
- 循環複雑度が 7 を超えるメソッド
```

**OK 例 3**:

```markdown
PMD はカスタム設定で以下をチェック：

- マジックナンバーの使用
- 空の catch ブロック
- 循環複雑度が 7 を超えるメソッド
```

**NG 例 4** - 太字ラベル（全角コロン）の直後にリスト:

```markdown
**タイプの種類：**
- `feat`: 新機能の追加
- `fix`: バグ修正
```

**OK 例 4**:

```markdown
**タイプの種類：**

- `feat`: 新機能の追加
- `fix`: バグ修正
```

**実行手順**:

1. `docs/` 配下の全 Markdown ファイルをスキャン
2. 上記ルールに違反する箇所を検出
3. 違反箇所を自動修正
4. 修正結果を報告

```bash
# Lint を実行
/docs --lint
「docs/ 配下の Markdown ファイルをチェックし、フォーマット違反を修正」
```

### 出力例

```
設計ドキュメント一覧
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📁 docs/requirements/
├─ ✅ requirements_definition.md (要件定義書)
├─ ✅ business_usecase.md (ビジネスユースケース)
├─ ✅ system_usecase.md (システムユースケース)
└─ ✅ user_story.md (ユーザーストーリー)

📁 docs/design/
├─ ✅ architecture_backend.md (バックエンドアーキテクチャ)
├─ ✅ architecture_frontend.md (フロントエンドアーキテクチャ)
├─ ✅ architecture_infrastructure.md (インフラストラクチャ)
├─ ✅ data-model.md (データモデル設計)
├─ ✅ domain-model.md (ドメインモデル設計)
├─ ✅ ui-design.md (UI 設計)
├─ ✅ test_strategy.md (テスト戦略)
├─ ✅ non_functional.md (非機能要件)
├─ ✅ operation.md (運用要件)
└─ ✅ tech_stack.md (技術スタック選定)

進捗: 14/14 ドキュメント完成 (100%)
```

#### --update 実行時の出力例

```
ドキュメント更新
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📝 docs/index.md を更新しています...
   ✅ 要件定義セクションを追加
   ✅ 設計ドキュメントセクションを追加
   ✅ 開発ドキュメントセクションを追加
   ✅ 運用ドキュメントセクションを追加

📝 mkdocs.yml を更新しています...
   ✅ nav セクションを更新
   ✅ 14 件のドキュメントをナビゲーションに追加

更新完了！
```

#### --lint 実行時の出力例

```
Markdown Lint
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🔍 docs/ 配下をスキャン中...

📄 docs/development/iteration_plan-1.md
   ⚠️  Line 52: リストの前に空行がありません
   ✅ 修正しました

📄 docs/requirements/user_story.md
   ⚠️  Line 128: リストの前に空行がありません
   ⚠️  Line 156: リストの前に空行がありません
   ✅ 2 箇所を修正しました

結果: 14 ファイルをスキャン、2 ファイルで 3 箇所を修正
```

### Claude との連携

```bash
# ドキュメント状況確認後に分析
/docs --status
「不足しているドキュメントがあれば作成を提案」

# 特定ドキュメントを参照して修正
/docs --read tech_stack
「技術スタックの内容を確認して改善点を提案」

# 全体概要を確認してレビュー
/docs --summary
「ドキュメント全体の整合性をレビュー」

# 新しいドキュメント作成後にインデックスを更新
/docs --update
「docs/index.md と mkdocs.yml を最新のドキュメント構成に同期」

# MkDocs でドキュメントサイトをプレビュー
/docs --update-mkdocs
「mkdocs.yml を更新後、mkdocs serve でプレビュー確認」

# ドキュメントのフォーマットをチェック・修正
/docs --lint
「Markdown フォーマットの一貫性を確保」
```

### 注意事項

- **前提条件**: `docs/` ディレクトリが存在すること
- **制限事項**: Markdown 形式のドキュメントのみ対応
- **推奨事項**: 定期的にドキュメントの進捗を確認し、最新の状態を維持すること
- **更新時の注意**: `--update` 実行前に現在の `docs/index.md` と `mkdocs.yml` をバックアップすることを推奨
- **MkDocs 依存**: `--update-mkdocs` を使用する場合、MkDocs がインストールされている必要がある

### ベストプラクティス

1. **定期確認**: 開発フェーズ移行前にドキュメントの完成度を確認する
2. **整合性維持**: コード変更時は関連ドキュメントも更新する
3. **レビュー活用**: チームレビュー前にドキュメント概要を共有する
4. **バージョン管理**: ドキュメントの変更は Git でコミットする
5. **インデックス同期**: 新しいドキュメント作成後は `--update` でインデックスを同期する
6. **プレビュー確認**: `--update-mkdocs` 後は `mkdocs serve` でナビゲーションを確認する
7. **フォーマット統一**: コミット前に `--lint` でフォーマットの一貫性を確保する

### 関連コマンド

- `/analysis` : 分析フェーズ全体の作業支援
- `/analysis-requirements` : 要件定義関連の作業支援
- `/analysis-architecture` : アーキテクチャ設計支援
- `/progress` : プロジェクト全体の進捗確認

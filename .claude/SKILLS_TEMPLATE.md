# スキル作成テンプレート

新しいスキルを作成する際のテンプレートです。

## ファイル構成

```
skills/<skill-name>/
├── SKILL.md          # メインのスキル定義（必須、500 行以内）
├── REFERENCE_1.md    # 参照ファイル（任意、内容が多い場合に分割）
└── REFERENCE_2.md    # 参照ファイル（任意）
```

## 命名規則

- **ディレクトリ名**: 動名詞形（-ing）を採用（例: `analyzing-requirements`, `developing-backend`）
- **name フィールド**: 小文字・数字・ハイフンのみ、64 文字以内
- **description フィールド**: 1024 文字以内、「何をするか」+「いつ使うか」を含める

## SKILL.md テンプレート

```markdown
---
name: skill-name
description: 何をするスキルか。いつ使うかのトリガー条件。
---

# スキルのタイトル

スキルの簡潔な説明（1-2 行で何をするスキルか説明）。

## Instructions

### 1. セクション名

内容を記述。

- 箇条書きで要点を整理
- 参照ドキュメントは `@docs/path/to/file.md` 形式で指定

### 2. セクション名

#### サブセクション

詳細な内容を記述。

### 3. 注意事項

- **前提条件**: スキル実行に必要な前提条件
- **制限事項**: 機能の制限や注意すべき点
- **推奨事項**: 効果的な使用方法の推奨

### 4. ベストプラクティス

1. **原則 1**: 推奨される方法の説明
2. **原則 2**: 避けるべき方法の説明

### 関連スキル

- `related-skill-1` : 関連する機能を持つスキル
- `related-skill-2` : 組み合わせて使うと効果的なスキル
```

## YAML フロントマターのルール

| フィールド | 必須 | 制約 |
| :--- | :--- | :--- |
| `name` | 必須 | 小文字・数字・ハイフンのみ、64 文字以内 |
| `description` | 必須 | 1024 文字以内 |

## 作成時のチェックリスト

- [ ] `name` が小文字・数字・ハイフンのみで 64 文字以内
- [ ] `description` が 1024 文字以内
- [ ] `description` に「何をするか」と「いつ使うか」の両方を含む
- [ ] SKILL.md が 500 行以内（超過分は参照ファイルに分割）
- [ ] 参照ファイルがある場合、SKILL.md から `@` パスでリンクしている
- [ ] 三人称で記述されている
- [ ] 関連スキルセクションでスキル名を正しく参照している

## 大容量コンテンツの分割例

SKILL.md が 500 行を超える場合、参照ファイルに分割します。

```
skills/managing-operations/
├── SKILL.md            # コア指示（概要、オプション一覧、基本例）
├── SETUP_JAVA.md       # Java 環境構築の詳細
├── SETUP_FRONTEND.md   # TypeScript/React 環境構築の詳細
└── DEPLOY.md           # デプロイ・運用監視の詳細
```

SKILL.md から参照ファイルを指す場合:

```markdown
### 環境構築の詳細

- **Java**: @.claude/skills/managing-operations/SETUP_JAVA.md
- **TypeScript/React**: @.claude/skills/managing-operations/SETUP_FRONTEND.md
```

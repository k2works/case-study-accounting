# GitHub Project 運用ガイド

## 概要

本プロジェクトでは GitHub Projects V2 を使用してプロジェクト管理を行います。リリース計画に基づいた Issue 管理、イテレーション（Iteration）によるタイムボックス管理を実現します。

## プロジェクト情報

| 項目 | 値 |
|------|-----|
| プロジェクト名 | 会計システムのケーススタディ |
| URL | https://github.com/users/k2works/projects/8 |
| プロジェクト ID | `PVT_kwHOADFh2M4BLGg3` |

## フィールド構成

### 標準フィールド

| フィールド名 | タイプ | 説明 |
|-------------|--------|------|
| Title | TITLE | Issue タイトル |
| Status | SINGLE_SELECT | Todo / In Progress / Done |
| Assignees | ASSIGNEES | 担当者 |
| Labels | LABELS | ラベル |
| Milestone | MILESTONE | マイルストーン（リリース） |

### カスタムフィールド

| フィールド名 | タイプ | 説明 |
|-------------|--------|------|
| イテレーション | ITERATION | イテレーション（2週間） |
| リリース | SINGLE_SELECT | 1.0 MVP / 2.0 機能拡張版 / 3.0 完成版 |
| 優先度 | SINGLE_SELECT | 必須 / 重要 |
| SP | NUMBER | ストーリーポイント |
| カテゴリ | SINGLE_SELECT | 機能カテゴリ |

## イテレーション（Iteration）設定

### イテレーションスケジュール

| イテレーション | 期間 | リリース |
|----------------|------|----------|
| イテレーション 1 | 2026-01-05 〜 2026-01-18 | 1.0 MVP |
| イテレーション 2 | 2026-01-19 〜 2026-02-01 | 1.0 MVP |
| イテレーション 3 | 2026-02-02 〜 2026-02-15 | 1.0 MVP |
| イテレーション 4 | 2026-02-16 〜 2026-03-01 | 1.0 MVP |
| イテレーション 5 | 2026-03-02 〜 2026-03-15 | 2.0 機能拡張版 |
| イテレーション 6 | 2026-03-16 〜 2026-03-29 | 2.0 機能拡張版 |
| イテレーション 7 | 2026-03-30 〜 2026-04-12 | 2.0 機能拡張版 |
| イテレーション 8 | 2026-04-13 〜 2026-04-26 | 2.0 機能拡張版 |
| イテレーション 9 | 2026-04-27 〜 2026-05-10 | 3.0 完成版 |
| イテレーション 10 | 2026-05-11 〜 2026-05-24 | 3.0 完成版 |
| イテレーション 11 | 2026-05-25 〜 2026-06-07 | 3.0 完成版 |
| イテレーション 12 | 2026-06-08 〜 2026-06-21 | 3.0 完成版 |

### Iteration フィールドの特徴

- **ネイティブ Iteration タイプ**: GitHub Projects V2 の Iteration フィールドを使用
- **タイムボックス管理**: 開始日と期間（14日）が自動計算
- **進捗可視化**: Roadmap ビューでイテレーションごとの進捗を可視化

## GraphQL API リファレンス

### プロジェクト情報の取得

```graphql
query {
  node(id: "PVT_kwHOADFh2M4BLGg3") {
    ... on ProjectV2 {
      title
      fields(first: 20) {
        nodes {
          ... on ProjectV2Field {
            id
            name
            dataType
          }
          ... on ProjectV2SingleSelectField {
            id
            name
            options {
              id
              name
            }
          }
          ... on ProjectV2IterationField {
            id
            name
            configuration {
              iterations {
                id
                title
                startDate
                duration
              }
            }
          }
        }
      }
    }
  }
}
```

### Iteration フィールドの作成

```graphql
mutation {
  createProjectV2Field(input: {
    projectId: "PVT_kwHOADFh2M4BLGg3"
    dataType: ITERATION
    name: "イテレーション"
  }) {
    projectV2Field {
      ... on ProjectV2IterationField {
        id
        name
      }
    }
  }
}
```

### Iteration の追加・更新

```graphql
mutation {
  updateProjectV2Field(input: {
    fieldId: "PVTIF_lAHOADFh2M4BLGg3zg61KPA"
    name: "イテレーション"
    iterationConfiguration: {
      startDate: "2026-01-05"
      duration: 14
      iterations: [
        { title: "イテレーション 1", startDate: "2026-01-05", duration: 14 }
        { title: "イテレーション 2", startDate: "2026-01-19", duration: 14 }
        # ... 続く
      ]
    }
  }) {
    projectV2Field {
      ... on ProjectV2IterationField {
        id
        configuration {
          iterations {
            id
            title
            startDate
          }
        }
      }
    }
  }
}
```

### Issue にイテレーションを割り当て

```graphql
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: "PVT_kwHOADFh2M4BLGg3"
    itemId: "PVTI_xxx"
    fieldId: "PVTIF_lAHOADFh2M4BLGg3zg61KPA"
    value: {
      iterationId: "8947f07d"  # イテレーション 1 の ID
    }
  }) {
    projectV2Item {
      id
    }
  }
}
```

## gh CLI コマンド

### プロジェクト一覧

```bash
gh project list --owner k2works
```

### フィールド一覧

```bash
gh project field-list 8 --owner k2works --format json
```

### Issue 一覧

```bash
gh project item-list 8 --owner k2works --format json
```

### Issue をプロジェクトに追加

```bash
gh project item-add 8 --owner k2works --url https://github.com/k2works/case-study-accounting/issues/XX
```

### フィールド値の更新（SingleSelect）

```bash
gh project item-edit --project-id PVT_kwHOADFh2M4BLGg3 \
  --id PVTI_xxx \
  --field-id PVTSSF_xxx \
  --single-select-option-id xxx
```

### GraphQL API 経由での更新

```bash
gh api graphql -f query='
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: "PVT_kwHOADFh2M4BLGg3"
    itemId: "PVTI_xxx"
    fieldId: "PVTIF_xxx"
    value: { iterationId: "xxx" }
  }) {
    projectV2Item { id }
  }
}'
```

## 運用フロー

### 1. リリース計画の同期

```bash
# release_plan.md と GitHub の差異確認
/plan-github --verify

# 差異がある場合は同期
/plan-github
```

### 2. イテレーション計画

1. イテレーション開始時に対象 Issue を確認
2. Issue のステータスを「In Progress」に更新
3. 担当者をアサイン

### 3. 進捗確認

```bash
# イテレーションごとの Issue 数を確認
gh project item-list 8 --owner k2works --format json | \
  jq '[.items[] | {iteration: .iteration.title}] | group_by(.iteration) | map({iteration: .[0].iteration, count: length})'
```

### 4. イテレーション完了

1. 完了した Issue のステータスを「Done」に更新
2. GitHub Issue をクローズ
3. 次のイテレーションに未完了 Issue を移動（必要に応じて）

## ビュー設定

### Board ビュー

- ステータス別にカード表示
- イテレーションでフィルタリング可能

### Roadmap ビュー

- イテレーションをタイムラインで表示
- リリースマイルストーンの可視化

### Table ビュー

- 全フィールドを一覧表示
- ソート・フィルタリング・グループ化

## トラブルシューティング

### Issue がプロジェクトに表示されない

```bash
# Issue をプロジェクトに追加
gh project item-add 8 --owner k2works --url <issue-url>
```

### フィールド値が更新されない

- GraphQL API のフィールド ID が正しいか確認
- Iteration の場合は `iterationId` を使用（`singleSelectOptionId` ではない）

### 日本語フィールド名が文字化けする

- `gh project item-list` の出力でエンコーディングの問題が発生することがある
- データ自体は正常なので、GraphQL API で直接確認する

## 関連ドキュメント

- [リリース計画](../development/release_plan.md)
- [イテレーション計画](../development/iteration_plan-1.md)
- [GitHub Docs - Projects](https://docs.github.com/en/issues/planning-and-tracking-with-projects)
- [GitHub Docs - Using the API to manage Projects](https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/using-the-api-to-manage-projects)

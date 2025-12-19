# 外部サービス連携

本ドキュメントでは、プロジェクトで使用している外部サービスとその設定方法について説明します。

## サービス一覧

| サービス | 用途 | URL |
|----------|------|-----|
| GitHub Actions | CI/CD パイプライン | https://github.com/k2works/case-study-accounting/actions |
| SonarCloud | コード品質分析 | https://sonarcloud.io |
| Qlty | コードカバレッジ・保守性分析 | https://qlty.sh |
| Heroku | デモ環境ホスティング | https://heroku.com |

---

## GitHub Actions

### 概要

GitHub Actions を使用して CI/CD パイプラインを構築しています。

### ワークフロー一覧

| ワークフロー | ファイル | トリガー | 説明 |
|--------------|----------|----------|------|
| Backend CI | `backend-ci.yml` | push/PR to main, develop | バックエンドのビルド、テスト、静的解析 |
| Frontend CI | `frontend-ci.yml` | push/PR to main, develop | フロントエンドのビルド、テスト、Lint |
| Backend Deploy | `backend-deploy.yml` | push to main | バックエンドを Heroku にデプロイ |
| Frontend Deploy | `frontend-deploy.yml` | push to main | フロントエンドを Heroku にデプロイ |
| Backend SonarQube | `backend-sonar-qube.yml` | push/PR to main | バックエンド SonarCloud 分析 |
| Frontend SonarQube | `frontend-sonar-qube.yml` | push/PR to main | フロントエンド SonarCloud 分析 |
| Deploy MkDocs | `mkdocs.yml` | push to main | ドキュメントを GitHub Pages にデプロイ |

### バッジ

README.md に以下のバッジを表示しています：

```markdown
[![Backend CI](https://github.com/k2works/case-study-accounting/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/k2works/case-study-accounting/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/k2works/case-study-accounting/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/k2works/case-study-accounting/actions/workflows/frontend-ci.yml)
[![Backend Deploy to Heroku](https://github.com/k2works/case-study-accounting/actions/workflows/backend-deploy.yml/badge.svg)](https://github.com/k2works/case-study-accounting/actions/workflows/backend-deploy.yml)
[![Frontend Deploy to Heroku](https://github.com/k2works/case-study-accounting/actions/workflows/frontend-deploy.yml/badge.svg)](https://github.com/k2works/case-study-accounting/actions/workflows/frontend-deploy.yml)
[![Deploy MkDocs](https://github.com/k2works/case-study-accounting/actions/workflows/mkdocs.yml/badge.svg)](https://github.com/k2works/case-study-accounting/actions/workflows/mkdocs.yml)
```

---

## SonarCloud

### 概要

SonarCloud を使用してコード品質を継続的に分析しています。

### 機能

- コードの品質ゲート
- バグ検出
- コードスメル検出
- セキュリティ脆弱性検出
- コードカバレッジ表示
- 重複コード検出

### 設定

#### 1. SonarCloud でプロジェクト作成

1. [SonarCloud](https://sonarcloud.io) にログイン
2. GitHub リポジトリをインポート
3. プロジェクトキーと組織を確認

#### 2. GitHub Secrets 設定

| シークレット名 | 内容 |
|----------------|------|
| `SONAR_TOKEN` | SonarCloud のアクセストークン |

#### 3. build.gradle.kts 設定

```kotlin
plugins {
    id("org.sonarqube") version "5.1.0.4882"
}

sonar {
    properties {
        property("sonar.projectKey", "k2works_case-study-accounting")
        property("sonar.organization", "k2works")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
```

### ワークフロー

```yaml
# .github/workflows/backend-sonar-qube.yml
name: SonarQube
on:
  push:
    branches:
      - main
  pull_request:
    types: [opened, synchronize, reopened]
jobs:
  build:
    name: Build and analyze
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'oracle'

      - name: Build and analyze
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: ./gradlew build sonar --info
        working-directory: apps/backend
```

---

## Qlty

### 概要

Qlty を使用してコードカバレッジと保守性を分析しています。

### 機能

- コードカバレッジ追跡
- 保守性スコア（Maintainability）
- 技術的負債の可視化
- トレンド分析

### 設定

#### 1. Qlty でプロジェクト設定

1. [Qlty](https://qlty.sh) にログイン
2. GitHub リポジトリを連携
3. カバレッジトークンを取得

#### 2. GitHub Secrets 設定

| シークレット名 | 内容 |
|----------------|------|
| `QLTY_COVERAGE_TOKEN` | Qlty のカバレッジトークン |

#### 3. ワークフロー設定

```yaml
# backend-ci.yml に追加
- name: Upload coverage to Codecov
  uses: qltysh/qlty-action/coverage@v1
  with:
    token: ${{ secrets.QLTY_COVERAGE_TOKEN }}
    files: apps/backend/build/reports/jacoco/test/jacocoTestReport.xml
```

### バッジ

```markdown
[![Maintainability](https://qlty.sh/gh/k2works/projects/case-study-accounting/maintainability.svg)](https://qlty.sh/gh/k2works/projects/case-study-accounting)
[![Code Coverage](https://qlty.sh/gh/k2works/projects/case-study-accounting/coverage.svg)](https://qlty.sh/gh/k2works/projects/case-study-accounting)
```

---

## Heroku

### 概要

Heroku Container Registry を使用してデモ環境をホスティングしています。

### アプリケーション

| アプリ名 | URL | 用途 |
|----------|-----|------|
| case-study-accounting-backend | https://case-study-accounting-backend-8d23bb5e8bbe.herokuapp.com | バックエンド API |
| case-study-accounting-frontend | https://case-study-accounting-frontend-2cb4e7e16f2f.herokuapp.com | フロントエンド SPA |

### 設定

#### 1. Heroku CLI インストール

```bash
# macOS
brew tap heroku/brew && brew install heroku

# Windows
choco install heroku-cli
```

#### 2. Heroku API キー取得

```bash
heroku auth:token
```

#### 3. GitHub Secrets 設定

| シークレット名 | 内容 |
|----------------|------|
| `HEROKU_API_KEY` | Heroku API キー |

#### 4. 環境変数（Config Vars）

**バックエンド:**

| 変数名 | 値 |
|--------|-----|
| `SPRING_PROFILES_ACTIVE` | demo |
| `JWT_SECRET` | (自動生成) |

**フロントエンド:**

| 変数名 | 値 |
|--------|-----|
| `API_URL` | https://case-study-accounting-backend-8d23bb5e8bbe.herokuapp.com/api |

### デプロイ方法

詳細は [デモ環境デプロイ手順](deploy_demo.md) を参照。

---

## GitHub Secrets 一覧

プロジェクトで必要な GitHub Secrets の一覧です。

| シークレット名 | サービス | 用途 |
|----------------|----------|------|
| `SONAR_TOKEN` | SonarCloud | コード品質分析 |
| `QLTY_COVERAGE_TOKEN` | Qlty | カバレッジレポート |
| `HEROKU_API_KEY` | Heroku | デモ環境デプロイ |

### 設定手順

1. GitHub リポジトリの **Settings** を開く
2. **Secrets and variables** → **Actions** を選択
3. **New repository secret** をクリック
4. Name と Secret を入力して保存

---

## トラブルシューティング

### SonarCloud 分析が失敗する

1. `SONAR_TOKEN` が正しく設定されているか確認
2. SonarCloud でプロジェクトが存在するか確認
3. `build.gradle.kts` の sonar 設定を確認

### Qlty カバレッジがアップロードされない

1. `QLTY_COVERAGE_TOKEN` が正しく設定されているか確認
2. JaCoCo レポートが生成されているか確認
3. レポートファイルのパスが正しいか確認

### Heroku デプロイが失敗する

1. `HEROKU_API_KEY` が有効か確認
2. Heroku アプリが存在するか確認
3. Docker ビルドがローカルで成功するか確認

---

## 関連ドキュメント

- [デモ環境デプロイ手順](deploy_demo.md)
- [バックエンドデモ環境](backend_demo_env.md)
- [フロントエンドデモ環境](frontend_demo_env.md)
- [SonarQube セットアップガイド](sonarqube_setup.md)

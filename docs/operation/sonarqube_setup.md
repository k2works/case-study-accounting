# SonarQube セットアップガイド

## 概要

本ドキュメントは、SonarQube を使用したコード品質分析環境のセットアップ手順を説明します。

SonarScanner for Gradle を使用することで、Gradle プロジェクトの設定から自動的にパラメータを取得し、効率的にコード解析を実行できます。

## 技術スタック

| コンポーネント | バージョン | 説明 |
|---------------|-----------|------|
| SonarQube | community | コード品質プラットフォーム |
| SonarScanner for Gradle | 7.2.1.6560 | Gradle 用スキャナー（Gradle 9 対応） |
| PostgreSQL | 15 | SonarQube 用データベース |

## エディション比較

本プロジェクトでは **SonarQube Community Edition** を使用しています。

| 機能 | Community | Developer | Enterprise |
|------|:---------:|:---------:|:----------:|
| コード品質分析 | ✅ | ✅ | ✅ |
| セキュリティ分析 | ✅ | ✅ | ✅ |
| ブランチ分析 | ❌ (main のみ) | ✅ | ✅ |
| PR デコレーション | ❌ | ✅ | ✅ |
| ポートフォリオ管理 | ❌ | ❌ | ✅ |

> **Note**: Community Edition では **main ブランチのみ** 解析可能です。
> 複数ブランチや PR 分析が必要な場合は、[SonarCloud](https://sonarcloud.io/)（OSS プロジェクト無料）の利用を検討してください。

## 前提条件

- Docker / Docker Compose がインストールされていること
- Java 25 がインストールされていること
- Gradle Wrapper が使用可能であること

## アーキテクチャ

```
┌─────────────────┐      ┌─────────────────┐
│   Developer     │      │   CI/CD         │
│   Workstation   │      │   (GitHub       │
│                 │      │    Actions)     │
└────────┬────────┘      └────────┬────────┘
         │                        │
         │  ./gradlew sonar       │
         │                        │
         ▼                        ▼
┌─────────────────────────────────────────┐
│           SonarQube Server              │
│           (localhost:9000)              │
├─────────────────────────────────────────┤
│           PostgreSQL 15                 │
│           (sonarqube-db)                │
└─────────────────────────────────────────┘
```

## セットアップ手順

### 1. SonarQube の起動

```bash
# プロジェクトルートで実行
docker-compose up -d sonarqube sonarqube-db
```

初回起動時は SonarQube の初期化に数分かかります。

```bash
# ログを確認
docker-compose logs -f sonarqube
```

「SonarQube is operational」と表示されたら起動完了です。

### 2. SonarQube へのアクセス

ブラウザで http://localhost:9000 にアクセスします。

**初期認証情報:**
- ユーザー名: `admin`
- パスワード: `admin`

初回ログイン時にパスワードの変更を求められます。

### 3. トークンの生成

1. 右上のユーザーアイコンをクリック
2. **My Account** を選択
3. **Security** タブを選択
4. **Generate Tokens** セクションで:
   - Name: `accounting-backend`
   - Type: `Project Analysis Token`
   - Project: `accounting-backend`（初回は Global を選択）
5. **Generate** をクリック
6. 生成されたトークンをコピーして安全に保管

### 4. 解析の実行

```bash
cd apps/backend
```

**Linux / macOS:**
```bash
export JAVA_HOME=/path/to/jdk-25
./gradlew sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<生成したトークン>
```

**Windows (CMD):**
```cmd
set JAVA_HOME=C:\path\to\jdk-25
gradlew.bat sonar "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=<生成したトークン>"
```

> **Note**: Windows では `-D` パラメータを引用符で囲む必要があります。

### 5. 結果の確認

http://localhost:9000/dashboard?id=accounting-backend で解析結果を確認できます。

## Gradle 設定

### build.gradle.kts

```kotlin
plugins {
    id("org.sonarqube") version "7.2.1.6560"
}

sonar {
    properties {
        property("sonar.projectKey", "accounting-backend")
        property("sonar.projectName", "Accounting Backend")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
        property("sonar.java.binaries", "build/classes/java/main")
        property("sonar.java.test.binaries", "build/classes/java/test")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.java.checkstyle.reportPaths", "build/reports/checkstyle/main.xml,build/reports/checkstyle/test.xml")
        property("sonar.java.pmd.reportPaths", "build/reports/pmd/main.xml,build/reports/pmd/test.xml")
        property("sonar.java.spotbugs.reportPaths", "build/reports/spotbugs/main.xml,build/reports/spotbugs/test.xml")
    }
}

tasks.named("sonar") {
    dependsOn("test", "jacocoTestReport")
}
```

### gradle.properties（オプション）

トークンを毎回指定しない場合、`~/.gradle/gradle.properties` に設定できます:

```properties
systemProp.sonar.host.url=http://localhost:9000
systemProp.sonar.token=<your-token>
```

> **Warning**: トークンは秘密情報です。リポジトリにコミットしないでください。

## Docker Compose 設定

### docker-compose.yml

```yaml
services:
  sonarqube:
    image: sonarqube:community
    container_name: accounting-sonarqube
    depends_on:
      - sonarqube-db
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://sonarqube-db:5432/sonar
      SONAR_JDBC_USERNAME: sonar
      SONAR_JDBC_PASSWORD: sonar
    ports:
      - "9000:9000"
    volumes:
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_extensions:/opt/sonarqube/extensions
      - sonarqube_logs:/opt/sonarqube/logs

  sonarqube-db:
    image: postgres:15
    container_name: accounting-sonarqube-db
    environment:
      POSTGRES_DB: sonar
      POSTGRES_USER: sonar
      POSTGRES_PASSWORD: sonar
    volumes:
      - sonarqube_db_data:/var/lib/postgresql/data

volumes:
  sonarqube_data:
  sonarqube_extensions:
  sonarqube_logs:
  sonarqube_db_data:
```

## フロントエンド解析

フロントエンド（TypeScript/React）プロジェクトは SonarScanner for npm を使用します。

### トークンの生成（フロントエンド用）

1. SonarQube にログイン
2. 右上のユーザーアイコン > **My Account** > **Security**
3. **Generate Tokens** で:
   - Name: `accounting-frontend`
   - Type: `Project Analysis Token`
   - Project: `accounting-frontend`（初回は Global を選択）
4. 生成されたトークンを保管

### 環境変数の設定

`.env.local.example` をコピーして `.env.local` を作成します。

```bash
cd apps/frontend
cp .env.local.example .env.local
```

`.env.local` を編集して SonarQube の設定を行います:

```properties
SONAR_HOST_URL=http://localhost:9000
SONAR_TOKEN=<生成したトークン>
```

### 解析の実行

```bash
cd apps/frontend

# 解析実行（カバレッジ + ESLint レポート + SonarQube）
npm run sonar
```

### 設定ファイル

`apps/frontend/sonar-project.properties`:

```properties
sonar.projectKey=accounting-frontend
sonar.projectName=Accounting Frontend
sonar.sources=src
sonar.tests=src
sonar.test.inclusions=**/*.test.ts,**/*.test.tsx
sonar.exclusions=**/node_modules/**,**/dist/**,**/coverage/**
sonar.typescript.lcov.reportPaths=coverage/lcov.info
sonar.eslint.reportPaths=eslint-report.json
```

### npm スクリプト

| コマンド | 説明 |
|---------|------|
| `npm run sonar` | カバレッジ + ESLint レポート + SonarQube 解析 |
| `npm run sonar:ci` | CI 用（レポート生成済みの場合） |
| `npm run lint:report` | ESLint レポートを JSON 出力 |

### 結果の確認

http://localhost:9000/dashboard?id=accounting-frontend

## バックエンド解析（Gradle）

### Gradle タスク

| コマンド | 説明 |
|---------|------|
| `./gradlew sonar` | SonarQube 解析を実行 |
| `./gradlew sonar -Dsonar.host.url=<URL>` | URL を指定して実行 |
| `./gradlew sonar -Dsonar.token=<TOKEN>` | トークンを指定して実行 |

## 静的解析ツールとの連携

SonarScanner for Gradle は以下のレポートを自動的に取り込みます:

| ツール | レポートパス |
|--------|------------|
| JaCoCo | `build/reports/jacoco/test/jacocoTestReport.xml` |
| Checkstyle | `build/reports/checkstyle/main.xml`, `test.xml` |
| PMD | `build/reports/pmd/main.xml`, `test.xml` |
| SpotBugs | `build/reports/spotbugs/main.xml`, `test.xml` |

### 全解析を実行

```bash
# 品質チェック + テスト + カバレッジ + SonarQube
./gradlew fullCheck sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<TOKEN>
```

## Quality Gate

SonarQube の Quality Gate を使用して、コード品質の基準を設定できます。

### デフォルト Quality Gate の条件

| メトリクス | 条件 |
|-----------|------|
| Coverage | 80% 以上 |
| Duplicated Lines | 3% 以下 |
| Maintainability Rating | A |
| Reliability Rating | A |
| Security Rating | A |

### カスタム Quality Gate の作成

1. **Quality Gates** メニューを選択
2. **Create** をクリック
3. 条件を設定
4. プロジェクトに適用

## CI/CD 連携

### GitHub Actions

```yaml
name: SonarQube Analysis

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  sonarqube:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # SonarQube のブランチ分析に必要

      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - name: Cache SonarQube packages
        uses: actions/cache@v4
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar

      - name: Build and analyze
        working-directory: apps/backend
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
        run: |
          ./gradlew build sonar \
            -Dsonar.host.url=$SONAR_HOST_URL \
            -Dsonar.token=$SONAR_TOKEN
```

### GitHub Secrets の設定

1. リポジトリの **Settings** > **Secrets and variables** > **Actions**
2. 以下のシークレットを追加:
   - `SONAR_TOKEN`: SonarQube のトークン
   - `SONAR_HOST_URL`: SonarQube サーバーの URL

## トラブルシューティング

### SonarQube が起動しない

**エラー:** `max virtual memory areas vm.max_map_count [65530] is too low`

**解決方法（Linux）:**
```bash
sudo sysctl -w vm.max_map_count=524288
```

永続化する場合は `/etc/sysctl.conf` に追加:
```
vm.max_map_count=524288
```

### 解析が失敗する

**エラー:** `Not authorized. Please check the properties sonar.login and sonar.password`

**解決方法:**
- トークンが正しいか確認
- トークンの有効期限が切れていないか確認
- プロジェクトへのアクセス権があるか確認

### カバレッジが表示されない

**確認事項:**
1. JaCoCo の XML レポートが生成されているか
   ```bash
   ls build/reports/jacoco/test/jacocoTestReport.xml
   ```
2. `sonar.coverage.jacoco.xmlReportPaths` が正しいパスを指しているか

### Java バージョンエラー

**エラー:** `Unsupported class file major version 69`

**解決方法:**
- SonarQube LTS は Java 17 で動作しますが、解析対象は Java 25 で問題ありません
- Gradle の `JAVA_HOME` が Java 25 に設定されていることを確認

## 参考資料

- [SonarScanner for Gradle - 公式ドキュメント](https://docs.sonarsource.com/sonarqube-server/analyzing-source-code/scanners/sonarscanner-for-gradle)
- [SonarQube Docker - GitHub](https://github.com/SonarSource/docker-sonarqube)
- [SonarScanner for Gradle - GitHub](https://github.com/SonarSource/sonar-scanner-gradle)

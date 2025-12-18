# バックエンド構築手順書

## 概要

本ドキュメントは、会計システムのバックエンド環境を構築するための手順書です。

## 技術スタック

| カテゴリ | 技術 | バージョン |
|---------|------|-----------|
| 言語 | Java | 25 |
| フレームワーク | Spring Boot | 4.0.0 |
| ビルドツール | Gradle | 9.2.1 |
| O/R マッパー | MyBatis | 3.0.4 |
| データベース | PostgreSQL | 16 |
| マイグレーション | Flyway | 11.x |
| 認証 | JJWT | 0.12.6 |
| テスト | JUnit 5, Testcontainers, ArchUnit | - |
| 静的解析 | Checkstyle, PMD, SpotBugs, JaCoCo | - |
| ドキュメント生成 | JIG, JIG-ERD | 2025.11.1, 0.2.1 |

## 前提条件

以下のツールがインストールされていること:

- Java 25 以上
- Docker / Docker Compose
- Git

## ディレクトリ構成

```
apps/backend/
├── build.gradle.kts              # ビルド設定
├── settings.gradle.kts           # プロジェクト設定
├── gradle.properties             # Gradle プロパティ
├── jig.properties                # JIG 設定
├── gradlew / gradlew.bat         # Gradle Wrapper
├── config/
│   ├── checkstyle/
│   │   └── checkstyle.xml        # Checkstyle 設定
│   ├── pmd/
│   │   └── ruleset.xml           # PMD 設定
│   └── spotbugs/
│       └── exclude.xml           # SpotBugs 除外設定
└── src/
    ├── main/
    │   ├── java/com/example/accounting/
    │   │   ├── Application.java
    │   │   └── infrastructure/
    │   │       ├── config/
    │   │       ├── security/
    │   │       └── web/
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    └── test/
        └── java/com/example/accounting/
            └── documentation/    # ドキュメント生成テスト
```

## セットアップ手順

### 1. データベースの起動

```bash
# プロジェクトルートで実行
docker-compose up -d postgres
```

### 2. 依存関係のインストール

```bash
cd apps/backend
./gradlew build -x test
```

### 3. アプリケーションの起動

```bash
./gradlew bootRun
```

### 4. 動作確認

```bash
curl http://localhost:8080/api/health
```

期待するレスポンス:
```json
{
  "status": "UP",
  "database": "connected"
}
```

## Gradle タスク一覧

### 基本コマンド

| コマンド | 説明 |
|---------|------|
| `./gradlew build` | ビルド実行 |
| `./gradlew test` | テスト実行 |
| `./gradlew bootRun` | アプリケーション起動 |
| `./gradlew clean` | ビルド成果物削除 |

### TDD / テスト

| コマンド | 説明 |
|---------|------|
| `./gradlew tdd` | TDD モード（常に実行） |
| `./gradlew test --continuous` | 継続的テスト実行 |
| `./gradlew test --tests "クラス名"` | 特定テスト実行 |

### 品質チェック

| コマンド | 説明 |
|---------|------|
| `./gradlew checkstyleMain` | Checkstyle（メインコード） |
| `./gradlew checkstyleTest` | Checkstyle（テストコード） |
| `./gradlew pmdMain` | PMD（メインコード） |
| `./gradlew pmdTest` | PMD（テストコード） |
| `./gradlew spotbugsMain` | SpotBugs（メインコード） |
| `./gradlew spotbugsTest` | SpotBugs（テストコード） |
| `./gradlew jacocoTestReport` | カバレッジレポート生成 |

### 統合コマンド

| コマンド | 説明 |
|---------|------|
| `./gradlew qualityCheck` | 全品質チェック実行 |
| `./gradlew fullCheck` | テスト + 品質チェック + カバレッジ |

### ドキュメント生成

| コマンド | 説明 |
|---------|------|
| `./gradlew jigReports` | JIG ドキュメント生成 |
| `./gradlew verifyJigEnvironment` | JIG 環境確認 |
| `./gradlew test --tests "JigErdGeneratorTest"` | ER 図生成 |

## 設定ファイル

### application.yml

```yaml
spring:
  application:
    name: accounting-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/accounting
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

mybatis:
  configuration:
    map-underscore-to-camel-case: true
  type-aliases-package: com.example.accounting.domain.model

jwt:
  secret: your-secret-key-here-minimum-256-bits-for-hs256
  expiration: 86400000
  refresh-expiration: 604800000

server:
  port: 8080
```

### build.gradle.kts の主要設定

```kotlin
plugins {
    java
    jacoco
    checkstyle
    pmd
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.spotbugs") version "6.0.26"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // Database
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4")
    runtimeOnly("org.postgresql:postgresql")

    // Migration
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
}
```

## 静的解析設定

### Checkstyle

主要ルール:
- 循環複雑度: 最大 7
- メソッド長: 最大 50 行
- パラメータ数: 最大 7
- ファイル長: 最大 500 行

### PMD

主要ルール:
- 循環複雑度: メソッドレベル 7
- 認知複雑度: 7
- ベストプラクティス、コードスタイル、設計、セキュリティルール

### SpotBugs

除外設定:
- テストコード
- Spring 設定クラス
- Record クラス
- Lombok 生成コード

### JaCoCo

- バージョン: 0.8.14（Java 25 対応）
- レポート形式: HTML, XML

## ドキュメント生成

### JIG

JIG はドメイン駆動設計に基づいたドキュメントを自動生成するツールです。

**生成されるドキュメント:**
- パッケージ関連図
- ビジネスルール一覧
- アプリケーション一覧
- 分岐一覧
- カテゴリ図
- ユースケース図

**設定ファイル:** `jig.properties`

```properties
# ドメインモデルのパッケージパターン
jig.model.pattern=.+\\.domain\\.(model|type)\\..+

# 出力ドキュメントタイプ
jig.document.types=PackageRelationDiagram,BusinessRuleList,ApplicationList,BranchList,CategoryDiagram,CategoryUsageDiagram,ServiceMethodCallHierarchyDiagram,CompositeUsecaseDiagram

# 出力ディレクトリ
jig.output.directory=build/jig
```

**実行:**
```bash
./gradlew jigReports
```

出力先: `build/jig/`

### JIG-ERD

JIG-ERD はデータベースから ER 図を自動生成するツールです。

**実行:**
```bash
./gradlew test --tests "JigErdGeneratorTest"
```

出力先: `build/jig-erd/`

**注意事項:**
- Graphviz がインストールされている場合は PNG/SVG 画像が出力されます
- Graphviz がない場合は DOT ファイルのみ出力されます

## マイグレーション

### ファイル命名規則

```
V{バージョン}___{説明}.sql

例:
V1__create_accounts_table.sql
V2__add_user_table.sql
```

### マイグレーション実行

アプリケーション起動時に自動実行されます。

手動実行する場合:
```bash
./gradlew flywayMigrate
```

## テスト

### テスト構成

| 種類 | 対象 | ツール |
|------|------|--------|
| 単体テスト | ドメインモデル、サービス | JUnit 5, AssertJ |
| 統合テスト | リポジトリ、API | Testcontainers |
| アーキテクチャテスト | パッケージ依存関係 | ArchUnit |

### Testcontainers

テスト実行時に PostgreSQL コンテナが自動起動します。

```java
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DatabaseConnectionTest {
    // テスト実行時に PostgreSQL コンテナが起動
}
```

## CI/CD

GitHub Actions による自動化:

- プッシュ/PR 時に自動実行
- ビルド、テスト、静的解析
- カバレッジレポート生成
- 各種レポートのアーティファクト保存

ワークフローファイル: `.github/workflows/backend-ci.yml`

## トラブルシューティング

### Gradle Wrapper が動作しない

```bash
# 実行権限を付与
chmod +x gradlew
```

### データベース接続エラー

1. PostgreSQL コンテナが起動しているか確認:
   ```bash
   docker-compose ps
   ```

2. 接続情報が正しいか確認:
   ```bash
   docker-compose logs postgres
   ```

### テストが失敗する

1. Docker が起動しているか確認（Testcontainers 用）
2. ポート 5432 が使用されていないか確認

### 静的解析エラー

警告のみで失敗しない設定になっています。厳格化する場合:

```kotlin
// build.gradle.kts
checkstyle {
    isIgnoreFailures = false
}
pmd {
    isIgnoreFailures = false
}
spotbugs {
    ignoreFailures = false
}
```

## 参考資料

- [Spring Boot 4.0 リファレンス](https://docs.spring.io/spring-boot/docs/4.0.0/reference/html/)
- [MyBatis Spring Boot Starter](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
- [Flyway ドキュメント](https://documentation.red-gate.com/fd)
- [Testcontainers ドキュメント](https://testcontainers.com/)
- [JIG GitHub](https://github.com/dddjava/jig)
- [JIG-ERD GitHub](https://github.com/irof/jig-erd)

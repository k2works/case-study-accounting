plugins {
    java
    jacoco
    checkstyle
    pmd
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.spotbugs") version "6.0.27"
    id("org.dddjava.jig-gradle-plugin") version "2025.11.1"
    id("org.sonarqube") version "7.2.1.6560"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// バージョン管理
val mybatisVersion = "4.0.0"
val jjwtVersion = "0.12.6"
val vavrVersion = "0.10.4"
val springdocVersion = "2.8.8"
val testcontainersVersion = "1.20.4"
val archunitVersion = "1.3.0"
val jigErdVersion = "0.2.1"
val poiVersion = "5.4.0"
val openpdfVersion = "2.0.3"

dependencies {
    // === implementation ===
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // Database
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisVersion")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    // Functional Programming
    implementation("io.vavr:vavr:$vavrVersion")
    // OpenAPI / Swagger UI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    // Export (PDF / Excel)
    implementation("org.apache.poi:poi-ooxml:$poiVersion")
    implementation("com.github.librepdf:openpdf:$openpdfVersion")

    // === runtimeOnly ===
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.springframework.boot:spring-boot-h2console")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // === compileOnly ===
    compileOnly("org.projectlombok:lombok")

    // === annotationProcessor ===
    annotationProcessor("org.projectlombok:lombok")

    // === testImplementation ===
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
    testImplementation("com.github.irof:jig-erd:$jigErdVersion")

    // === testCompileOnly ===
    testCompileOnly("org.projectlombok:lombok")

    // === testAnnotationProcessor ===
    testAnnotationProcessor("org.projectlombok:lombok")

    // === testRuntimeOnly ===
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

// JaCoCo
jacoco {
    toolVersion = "0.8.14" // Java 25 support
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/Application.class",
                    "**/Application$*.class"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.0".toBigDecimal() // 初期は0%、徐々に上げる
            }
        }
    }
}

// Checkstyle
checkstyle {
    toolVersion = "10.20.2"
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
}

// SpotBugs (Java 25 対応: 4.9.7+)
spotbugs {
    ignoreFailures = false
    excludeFilter = file("${rootDir}/config/spotbugs/exclude.xml")
    toolVersion = "4.9.8"
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports.create("html") {
        required = true
    }
    reports.create("xml") {
        required = true // SonarQube 連携用
    }
}

// PMD (Java 25 対応: 7.16.0+)
pmd {
    toolVersion = "7.16.0"
    isConsoleOutput = true
    ruleSetFiles = files("${rootDir}/config/pmd/ruleset.xml", "${rootDir}/config/pmd/functional-rules.xml")
    ruleSets = listOf() // ruleSetFilesを使用するため空に
    isIgnoreFailures = false
}

// JIG (ドキュメント生成)
// デフォルト設定を使用、カスタマイズは jig.properties で行う

// JIG タスク依存関係
tasks.named("classes") {
    mustRunAfter("clean")
}
tasks.named("jigReports") {
    dependsOn("classes")
}

// カスタムタスク: TDD用の継続的テスト実行
tasks.register<Test>("tdd") {
    description = "Run tests in TDD mode (always executes)"
    group = "verification"
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    outputs.upToDateWhen { false }
}

// カスタムタスク: 品質チェック全実行
tasks.register("qualityCheck") {
    description = "Run all quality checks (Checkstyle, PMD, SpotBugs)"
    group = "verification"
    dependsOn("checkstyleMain", "checkstyleTest", "pmdMain", "pmdTest", "spotbugsMain", "spotbugsTest")
}

// カスタムタスク: すべてのテストと品質チェックを実行
tasks.register("fullCheck") {
    description = "Run all tests and quality checks"
    group = "verification"
    dependsOn("test", "qualityCheck", "jacocoTestReport")
}

// SonarQube (ローカル: Community Edition, CI: SonarCloud)
// ローカルでは ./gradlew sonar で localhost:9000 に接続
// CI ではワークフローで -Dsonar.host.url=https://sonarcloud.io 等を渡して上書き
sonar {
    properties {
        property("sonar.projectKey", "accounting-backend")
        property("sonar.projectName", "Accounting Backend")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.exclusions", "**/db/demo-data.sql")
    }
}

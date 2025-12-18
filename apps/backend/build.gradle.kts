plugins {
    java
    jacoco
    checkstyle
    pmd
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.spotbugs") version "6.0.26"
    id("org.dddjava.jig-gradle-plugin") version "2025.11.1"
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
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
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // ArchUnit
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")

    // JIG-ERD (ER図生成)
    testImplementation("com.github.irof:jig-erd:0.2.1")
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
    isIgnoreFailures = true // 初期は警告のみ
}

// SpotBugs
spotbugs {
    ignoreFailures = true // 初期は警告のみ
    excludeFilter = file("${rootDir}/config/spotbugs/exclude.xml")
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports.create("html") {
        required = true
    }
    reports.create("xml") {
        required = false
    }
}

// PMD
pmd {
    toolVersion = "7.8.0"
    isConsoleOutput = true
    ruleSetFiles = files("${rootDir}/config/pmd/ruleset.xml")
    ruleSets = listOf() // ruleSetFilesを使用するため空に
    isIgnoreFailures = true // 初期は警告のみ
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

package com.example.accounting.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ヘキサゴナルアーキテクチャ（Ports & Adapters）のルールを検証するテスト。
 *
 * <p>パッケージ構成:
 * <ul>
 *   <li>domain - ドメイン層（model, service）</li>
 *   <li>application - アプリケーション層（port/in, port/out, service）</li>
 *   <li>infrastructure - インフラストラクチャ層（persistence, web, config）</li>
 * </ul>
 *
 * @see docs/design/architecture_backend.md
 */
@DisplayName("ヘキサゴナルアーキテクチャテスト")
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
class HexagonalArchitectureTest {

    private static final String BASE_PACKAGE = "com.example.accounting";

    // 層パッケージ
    private static final String DOMAIN_PACKAGE = BASE_PACKAGE + ".domain..";
    private static final String APPLICATION_PACKAGE = BASE_PACKAGE + ".application..";
    private static final String INFRASTRUCTURE_PACKAGE = BASE_PACKAGE + ".infrastructure..";

    // アプリケーション層の詳細パッケージ
    private static final String PORT_IN_PACKAGE = BASE_PACKAGE + ".application.port.in..";
    private static final String PORT_OUT_PACKAGE = BASE_PACKAGE + ".application.port.out..";
    private static final String APPLICATION_SERVICE_PACKAGE = BASE_PACKAGE + ".application.service..";

    // インフラストラクチャ層の詳細パッケージ
    private static final String PERSISTENCE_PACKAGE = BASE_PACKAGE + ".infrastructure.persistence..";
    private static final String PERSISTENCE_REPOSITORY_PACKAGE =
            BASE_PACKAGE + ".infrastructure.persistence.repository..";
    private static final String WEB_PACKAGE = BASE_PACKAGE + ".infrastructure.web..";
    private static final String WEB_CONTROLLER_PACKAGE = BASE_PACKAGE + ".infrastructure.web.controller..";
    private static final String CONFIG_PACKAGE = BASE_PACKAGE + ".infrastructure.config..";

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Nested
    @DisplayName("ドメイン層のルール")
    class DomainLayerRules {

        @Test
        @DisplayName("ドメイン層はアプリケーション層に依存しない")
        void domainShouldNotDependOnApplication() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(APPLICATION_PACKAGE)
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("ドメイン層はインフラストラクチャ層に依存しない")
        void domainShouldNotDependOnInfrastructure() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("ドメイン層は Spring に依存しない")
        void domainShouldNotDependOnSpring() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("アプリケーション層のルール")
    class ApplicationLayerRules {

        @Test
        @DisplayName("アプリケーション層はインフラストラクチャ層に依存しない")
        void applicationShouldNotDependOnInfrastructure() {
            noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("Input Port は Web 層とアプリケーションサービスからのみアクセスされる")
        void inputPortsShouldOnlyBeAccessedByWebAndService() {
            classes()
                    .that().resideInAPackage(PORT_IN_PACKAGE)
                    .should().onlyBeAccessed()
                    .byAnyPackage(WEB_PACKAGE, APPLICATION_SERVICE_PACKAGE, PORT_IN_PACKAGE)
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("Output Port はアプリケーションサービスと永続化層からのみアクセスされる")
        void outputPortsShouldOnlyBeAccessedByServiceAndPersistence() {
            classes()
                    .that().resideInAPackage(PORT_OUT_PACKAGE)
                    .should().onlyBeAccessed()
                    .byAnyPackage(APPLICATION_SERVICE_PACKAGE, PERSISTENCE_PACKAGE, PORT_OUT_PACKAGE)
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("インフラストラクチャ層のルール")
    class InfrastructureLayerRules {

        @Test
        @DisplayName("コントローラーは infrastructure.web.controller パッケージに配置される")
        void controllersShouldBeInWebControllerPackage() {
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .and().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .should().resideInAPackage(WEB_CONTROLLER_PACKAGE)
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("リポジトリ実装は infrastructure.persistence.repository パッケージに配置される")
        void repositoryImplsShouldBeInPersistenceRepositoryPackage() {
            classes()
                    .that().haveSimpleNameEndingWith("RepositoryImpl")
                    .and().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .should().resideInAPackage(PERSISTENCE_REPOSITORY_PACKAGE)
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("設定クラスは infrastructure.config パッケージに配置される")
        void configClassesShouldBeInConfigPackage() {
            classes()
                    .that().haveSimpleNameEndingWith("Config")
                    .and().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .should().resideInAPackage(CONFIG_PACKAGE)
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("Web 層はドメイン層に直接依存しない（アプリケーション層経由で使用）")
        void webLayerShouldNotDirectlyDependOnDomainService() {
            noClasses()
                    .that().resideInAPackage(WEB_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(BASE_PACKAGE + ".domain.service..")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("永続化層はドメインサービスに依存しない")
        void persistenceLayerShouldNotDependOnDomainService() {
            noClasses()
                    .that().resideInAPackage(PERSISTENCE_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(BASE_PACKAGE + ".domain.service..")
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }
}

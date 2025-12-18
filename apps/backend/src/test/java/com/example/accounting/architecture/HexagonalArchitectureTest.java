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

@DisplayName("ヘキサゴナルアーキテクチャテスト")
class HexagonalArchitectureTest {

    private static final String BASE_PACKAGE = "com.example.accounting";
    private static final String DOMAIN_PACKAGE = BASE_PACKAGE + ".domain..";
    private static final String APPLICATION_PACKAGE = BASE_PACKAGE + ".application..";
    private static final String INFRASTRUCTURE_PACKAGE = BASE_PACKAGE + ".infrastructure..";

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
        @DisplayName("ドメイン層はSpringに依存しない")
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
    }

    @Nested
    @DisplayName("インフラストラクチャ層のルール")
    class InfrastructureLayerRules {

        @Test
        @DisplayName("コントローラーはcontrollerパッケージに配置される")
        void controllersShouldBeInControllerPackage() {
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .and().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .should().resideInAPackage("..controller..")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("設定クラスはconfigパッケージに配置される")
        void configClassesShouldBeInConfigPackage() {
            classes()
                    .that().haveSimpleNameEndingWith("Config")
                    .and().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .should().resideInAPackage("..config..")
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }
}

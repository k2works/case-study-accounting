package com.example.accounting.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.USE_JAVA_UTIL_LOGGING;

@DisplayName("コーディング規約テスト")
class CodingRulesTest {

    private static final String BASE_PACKAGE = "com.example.accounting";

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Test
    @DisplayName("System.out/System.errを使用しない")
    void noClassesShouldUseStandardStreams() {
        noClasses()
                .should(ACCESS_STANDARD_STREAMS)
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("java.util.loggingを使用しない")
    void noClassesShouldUseJavaUtilLogging() {
        noClasses()
                .should(USE_JAVA_UTIL_LOGGING)
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("フィールドインジェクションを使用しない")
    void noFieldInjection() {
        noFields()
                .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("インターフェースは'I'プレフィックスを使用しない")
    void interfacesShouldNotHaveIPrefix() {
        noClasses()
                .that().areInterfaces()
                .should().haveSimpleNameStartingWith("I")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("例外クラスはExceptionサフィックスを持つ")
    void exceptionsShouldEndWithException() {
        classes()
                .that().areAssignableTo(Exception.class)
                .should().haveSimpleNameEndingWith("Exception")
                .allowEmptyShould(true)
                .check(classes);
    }
}

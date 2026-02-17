package com.example.accounting.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Email")
class EmailTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("有効なメールアドレスでインスタンスを生成できる")
        void shouldCreateWithValidEmail() {
            Email email = Email.of("test@example.com");

            assertThat(email.value()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("サブドメイン付きのメールアドレスで生成できる")
        void shouldCreateWithSubdomain() {
            Email email = Email.of("test@mail.example.com");

            assertThat(email.value()).isEqualTo("test@mail.example.com");
        }

        @Test
        @DisplayName("プラス記号付きのメールアドレスで生成できる")
        void shouldCreateWithPlusSign() {
            Email email = Email.of("test+tag@example.com");

            assertThat(email.value()).isEqualTo("test+tag@example.com");
        }
    }

    @Nested
    @DisplayName("validated")
    class Validated {

        @Test
        @DisplayName("有効なメールアドレスで Right を返す")
        void shouldReturnRightForValidEmail() {
            assertThat(Email.validated("test@example.com").isRight()).isTrue();
            assertThat(Email.validated("test@example.com").get().value()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("null の場合はエラーメッセージを返す")
        void shouldReturnLeftForNull() {
            assertThat(Email.validated(null).getLeft()).isEqualTo("メールアドレスは必須です");
        }

        @Test
        @DisplayName("空文字の場合はエラーメッセージを返す")
        void shouldReturnLeftForEmpty() {
            assertThat(Email.validated("").getLeft()).isEqualTo("メールアドレスは必須です");
        }

        @Test
        @DisplayName("空白のみの場合はエラーメッセージを返す")
        void shouldReturnLeftForBlank() {
            assertThat(Email.validated("   ").getLeft()).isEqualTo("メールアドレスは必須です");
        }

        @Test
        @DisplayName("@ がない場合はエラーメッセージを返す")
        void shouldReturnLeftForMissingAt() {
            assertThat(Email.validated("testexample.com").getLeft()).isEqualTo("メールアドレスの形式が不正です");
        }

        @Test
        @DisplayName("ドメインがない場合はエラーメッセージを返す")
        void shouldReturnLeftForMissingDomain() {
            assertThat(Email.validated("test@").getLeft()).isEqualTo("メールアドレスの形式が不正です");
        }

        @Test
        @DisplayName("TLD が短すぎる場合はエラーメッセージを返す")
        void shouldReturnLeftForShortTld() {
            assertThat(Email.validated("test@example.c").getLeft()).isEqualTo("メールアドレスの形式が不正です");
        }
    }

    @Nested
    @DisplayName("reconstruct")
    class Reconstruct {

        @Test
        @DisplayName("バリデーションをスキップしてインスタンスを生成する")
        void shouldCreateWithoutValidation() {
            Email email = Email.reconstruct("invalid");

            assertThat(email.value()).isEqualTo("invalid");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("値を文字列として返す")
        void shouldReturnValue() {
            Email email = Email.of("test@example.com");

            assertThat(email).hasToString("test@example.com");
        }
    }
}

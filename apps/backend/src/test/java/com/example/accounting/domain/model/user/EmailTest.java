package com.example.accounting.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        @DisplayName("null の場合は例外をスローする")
        void shouldThrowExceptionForNull() {
            assertThatThrownBy(() -> Email.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("メールアドレスは必須です");
        }

        @Test
        @DisplayName("空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmpty() {
            assertThatThrownBy(() -> Email.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("メールアドレスは必須です");
        }

        @Test
        @DisplayName("空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlank() {
            assertThatThrownBy(() -> Email.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("メールアドレスは必須です");
        }

        @Test
        @DisplayName("@ がない場合は例外をスローする")
        void shouldThrowExceptionForMissingAt() {
            assertThatThrownBy(() -> Email.of("testexample.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("メールアドレスの形式が不正です");
        }

        @Test
        @DisplayName("ドメインがない場合は例外をスローする")
        void shouldThrowExceptionForMissingDomain() {
            assertThatThrownBy(() -> Email.of("test@"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("メールアドレスの形式が不正です");
        }

        @Test
        @DisplayName("TLD が短すぎる場合は例外をスローする")
        void shouldThrowExceptionForShortTld() {
            assertThatThrownBy(() -> Email.of("test@example.c"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("メールアドレスの形式が不正です");
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

            assertThat(email.toString()).isEqualTo("test@example.com");
        }
    }
}

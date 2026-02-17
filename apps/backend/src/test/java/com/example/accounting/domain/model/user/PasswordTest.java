package com.example.accounting.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Password")
class PasswordTest {

    @Nested
    @DisplayName("fromRawPassword")
    class FromRawPassword {

        @Test
        @DisplayName("有効なパスワードでハッシュ化されたインスタンスを生成できる")
        void shouldCreateHashedPassword() {
            Password password = Password.fromRawPassword("password123");

            assertThat(password.value()).isNotEqualTo("password123");
            assertThat(password.value()).startsWith("$2a$");
        }

        @Test
        @DisplayName("8文字ちょうどで生成できる")
        void shouldCreateWithMinLength() {
            Password password = Password.fromRawPassword("pass1234");

            assertThat(password.value()).startsWith("$2a$");
        }
    }

    @Nested
    @DisplayName("validated")
    class Validated {

        @Test
        @DisplayName("有効なパスワードで Right を返す")
        void shouldReturnRightForValidPassword() {
            assertThat(Password.validated("password123").isRight()).isTrue();
            assertThat(Password.validated("password123").get().value()).startsWith("$2a$");
        }

        @Test
        @DisplayName("null の場合はエラーメッセージを返す")
        void shouldReturnLeftForNull() {
            assertThat(Password.validated(null).getLeft()).isEqualTo("パスワードは必須です");
        }

        @Test
        @DisplayName("空文字の場合はエラーメッセージを返す")
        void shouldReturnLeftForEmpty() {
            assertThat(Password.validated("").getLeft()).isEqualTo("パスワードは必須です");
        }

        @Test
        @DisplayName("空白のみの場合はエラーメッセージを返す")
        void shouldReturnLeftForBlank() {
            assertThat(Password.validated("   ").getLeft()).isEqualTo("パスワードは必須です");
        }

        @Test
        @DisplayName("8文字未満の場合はエラーメッセージを返す")
        void shouldReturnLeftForTooShort() {
            assertThat(Password.validated("pass123").getLeft())
                    .isEqualTo("パスワードは8文字以上で入力してください");
        }
    }

    @Nested
    @DisplayName("reconstruct")
    class Reconstruct {

        @Test
        @DisplayName("バリデーションをスキップしてインスタンスを生成する")
        void shouldCreateWithoutValidation() {
            String hashedValue = "$2a$10$abcdef";

            Password password = Password.reconstruct(hashedValue);

            assertThat(password.value()).isEqualTo(hashedValue);
        }
    }

    @Nested
    @DisplayName("matches")
    class Matches {

        @Test
        @DisplayName("正しいパスワードで一致する")
        void shouldMatchCorrectPassword() {
            Password password = Password.fromRawPassword("password123");

            assertThat(password.matches("password123")).isTrue();
        }

        @Test
        @DisplayName("間違ったパスワードで一致しない")
        void shouldNotMatchWrongPassword() {
            Password password = Password.fromRawPassword("password123");

            assertThat(password.matches("wrongpassword")).isFalse();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("マスクされた文字列を返す")
        void shouldReturnMaskedString() {
            Password password = Password.fromRawPassword("password123");

            assertThat(password).hasToString("********");
        }
    }
}

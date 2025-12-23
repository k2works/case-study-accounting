package com.example.accounting.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Username")
class UsernameTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("有効なユーザー名でインスタンスを生成できる")
        void shouldCreateWithValidUsername() {
            Username username = Username.of("testuser");

            assertThat(username.value()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("null の場合は例外をスローする")
        void shouldThrowExceptionForNull() {
            assertThatThrownBy(() -> Username.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は必須です");
        }

        @Test
        @DisplayName("空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmpty() {
            assertThatThrownBy(() -> Username.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は必須です");
        }

        @Test
        @DisplayName("空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlank() {
            assertThatThrownBy(() -> Username.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は必須です");
        }

        @Test
        @DisplayName("3文字未満の場合は例外をスローする")
        void shouldThrowExceptionForTooShort() {
            assertThatThrownBy(() -> Username.of("ab"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は3〜50文字で入力してください");
        }

        @Test
        @DisplayName("50文字を超える場合は例外をスローする")
        void shouldThrowExceptionForTooLong() {
            String longUsername = "a".repeat(51);

            assertThatThrownBy(() -> Username.of(longUsername))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は3〜50文字で入力してください");
        }

        @Test
        @DisplayName("3文字ちょうどで生成できる")
        void shouldCreateWithMinLength() {
            Username username = Username.of("abc");

            assertThat(username.value()).isEqualTo("abc");
        }

        @Test
        @DisplayName("50文字ちょうどで生成できる")
        void shouldCreateWithMaxLength() {
            String maxUsername = "a".repeat(50);

            Username username = Username.of(maxUsername);

            assertThat(username.value()).isEqualTo(maxUsername);
        }
    }

    @Nested
    @DisplayName("reconstruct")
    class Reconstruct {

        @Test
        @DisplayName("バリデーションをスキップしてインスタンスを生成する")
        void shouldCreateWithoutValidation() {
            Username username = Username.reconstruct("ab");

            assertThat(username.value()).isEqualTo("ab");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("値を文字列として返す")
        void shouldReturnValue() {
            Username username = Username.of("testuser");

            assertThat(username).hasToString("testuser");
        }
    }
}

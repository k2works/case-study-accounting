package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UpdateAccountCommand")
class UpdateAccountCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でインスタンスを生成できる")
        void shouldCreateWithValidValues() {
            UpdateAccountCommand command = new UpdateAccountCommand(
                    1,
                    "Cash",
                    "ASSET"
            );

            assertThat(command.accountId()).isEqualTo(1);
            assertThat(command.accountName()).isEqualTo("Cash");
            assertThat(command.accountType()).isEqualTo("ASSET");
        }

        @Test
        @DisplayName("accountId が null の場合は例外をスローする")
        void shouldThrowExceptionForNullAccountId() {
            assertThatThrownBy(() -> new UpdateAccountCommand(
                    null,
                    "Cash",
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目IDは必須です");
        }

        @Test
        @DisplayName("accountName が null の場合は例外をスローする")
        void shouldThrowExceptionForNullAccountName() {
            assertThatThrownBy(() -> new UpdateAccountCommand(
                    1,
                    null,
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountName が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyAccountName() {
            assertThatThrownBy(() -> new UpdateAccountCommand(
                    1,
                    "",
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountName が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankAccountName() {
            assertThatThrownBy(() -> new UpdateAccountCommand(
                    1,
                    "   ",
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountType が null の場合は例外をスローする")
        void shouldThrowExceptionForNullAccountType() {
            assertThatThrownBy(() -> new UpdateAccountCommand(
                    1,
                    "Cash",
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyAccountType() {
            assertThatThrownBy(() -> new UpdateAccountCommand(
                    1,
                    "Cash",
                    ""
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankAccountType() {
            assertThatThrownBy(() -> new UpdateAccountCommand(
                    1,
                    "Cash",
                    "   "
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が許可されていない値の場合は例外をスローする")
        void shouldThrowExceptionForInvalidAccountType() {
            assertThatThrownBy(() -> new UpdateAccountCommand(
                    1,
                    "Cash",
                    "INVALID"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("無効な勘定科目種別コードです: INVALID");
        }
    }
}

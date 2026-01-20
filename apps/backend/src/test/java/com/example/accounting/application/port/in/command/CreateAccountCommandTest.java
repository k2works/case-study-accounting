package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CreateAccountCommand")
class CreateAccountCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でインスタンスを生成できる")
        void shouldCreateWithValidValues() {
            CreateAccountCommand command = new CreateAccountCommand(
                    "1001",
                    "Cash",
                    "ASSET"
            );

            assertThat(command.accountCode()).isEqualTo("1001");
            assertThat(command.accountName()).isEqualTo("Cash");
            assertThat(command.accountType()).isEqualTo("ASSET");
        }

        @Test
        @DisplayName("accountCode が null の場合は例外をスローする")
        void shouldThrowExceptionForNullAccountCode() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    null,
                    "Cash",
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("accountCode が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyAccountCode() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "",
                    "Cash",
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("accountCode が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankAccountCode() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "   ",
                    "Cash",
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("accountCode が 4 桁の数字でない場合は例外をスローする")
        void shouldThrowExceptionForInvalidAccountCode() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "10A1",
                    "Cash",
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは 4 桁の数字である必要があります");
        }

        @Test
        @DisplayName("accountName が null の場合は例外をスローする")
        void shouldThrowExceptionForNullAccountName() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "1001",
                    null,
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountName が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyAccountName() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "1001",
                    "",
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountName が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankAccountName() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "1001",
                    "   ",
                    "ASSET"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountType が null の場合は例外をスローする")
        void shouldThrowExceptionForNullAccountType() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "1001",
                    "Cash",
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyAccountType() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "1001",
                    "Cash",
                    ""
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankAccountType() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "1001",
                    "Cash",
                    "   "
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が許可されていない値の場合は例外をスローする")
        void shouldThrowExceptionForInvalidAccountType() {
            assertThatThrownBy(() -> new CreateAccountCommand(
                    "1001",
                    "Cash",
                    "INVALID"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("無効な勘定科目種別コードです: INVALID");
        }
    }
}

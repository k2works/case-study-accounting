package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        @DisplayName("accountCode が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullAccountCode() {
            assertThat(CreateAccountCommand.of(
                    null,
                    "Cash",
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("accountCode が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyAccountCode() {
            assertThat(CreateAccountCommand.of(
                    "",
                    "Cash",
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("accountCode が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankAccountCode() {
            assertThat(CreateAccountCommand.of(
                    "   ",
                    "Cash",
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("accountCode が 4 桁の数字でない場合はバリデーションエラーになる")
        void shouldReturnLeftForInvalidAccountCode() {
            assertThat(CreateAccountCommand.of(
                    "10A1",
                    "Cash",
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目コードは 4 桁の数字である必要があります");
        }

        @Test
        @DisplayName("accountName が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullAccountName() {
            assertThat(CreateAccountCommand.of(
                    "1001",
                    null,
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountName が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyAccountName() {
            assertThat(CreateAccountCommand.of(
                    "1001",
                    "",
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountName が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankAccountName() {
            assertThat(CreateAccountCommand.of(
                    "1001",
                    "   ",
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountType が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullAccountType() {
            assertThat(CreateAccountCommand.of(
                    "1001",
                    "Cash",
                    null
            ).getLeft()).isEqualTo("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyAccountType() {
            assertThat(CreateAccountCommand.of(
                    "1001",
                    "Cash",
                    ""
            ).getLeft()).isEqualTo("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankAccountType() {
            assertThat(CreateAccountCommand.of(
                    "1001",
                    "Cash",
                    "   "
            ).getLeft()).isEqualTo("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が許可されていない値の場合はバリデーションエラーになる")
        void shouldReturnLeftForInvalidAccountType() {
            assertThat(CreateAccountCommand.of(
                    "1001",
                    "Cash",
                    "INVALID"
            ).getLeft()).isEqualTo("無効な勘定科目種別コードです: INVALID");
        }
    }
}

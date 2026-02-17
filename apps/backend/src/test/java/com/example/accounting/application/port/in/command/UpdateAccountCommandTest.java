package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        @DisplayName("accountId が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullAccountId() {
            assertThat(UpdateAccountCommand.of(
                    null,
                    "Cash",
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目IDは必須です");
        }

        @Test
        @DisplayName("accountName が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullAccountName() {
            assertThat(UpdateAccountCommand.of(
                    1,
                    null,
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountName が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyAccountName() {
            assertThat(UpdateAccountCommand.of(
                    1,
                    "",
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountName が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankAccountName() {
            assertThat(UpdateAccountCommand.of(
                    1,
                    "   ",
                    "ASSET"
            ).getLeft()).isEqualTo("勘定科目名は必須です");
        }

        @Test
        @DisplayName("accountType が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullAccountType() {
            assertThat(UpdateAccountCommand.of(
                    1,
                    "Cash",
                    null
            ).getLeft()).isEqualTo("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyAccountType() {
            assertThat(UpdateAccountCommand.of(
                    1,
                    "Cash",
                    ""
            ).getLeft()).isEqualTo("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankAccountType() {
            assertThat(UpdateAccountCommand.of(
                    1,
                    "Cash",
                    "   "
            ).getLeft()).isEqualTo("勘定科目種別は必須です");
        }

        @Test
        @DisplayName("accountType が許可されていない値の場合はバリデーションエラーになる")
        void shouldReturnLeftForInvalidAccountType() {
            assertThat(UpdateAccountCommand.of(
                    1,
                    "Cash",
                    "INVALID"
            ).getLeft()).isEqualTo("無効な勘定科目種別コードです: INVALID");
        }
    }
}

package com.example.accounting.domain.model.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Account エンティティ")
class AccountTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("create で正しく勘定科目が作成される（ID は null）")
        void shouldCreateAccount() {
            AccountCode code = AccountCode.of("1101");
            String name = "現金";
            AccountType type = AccountType.ASSET;

            Account account = Account.create(code, name, type);

            // ID は null（DB 保存時に自動採番される）
            assertThat(account.getId()).isNull();
            assertThat(account.getAccountCode()).isEqualTo(code);
            assertThat(account.getAccountName()).isEqualTo(name);
            assertThat(account.getAccountType()).isEqualTo(type);
        }

        @Test
        @DisplayName("勘定科目コードが null の場合は例外をスローする")
        void shouldThrowExceptionWhenCodeIsNull() {
            assertThatThrownBy(() -> Account.create(null, "現金", AccountType.ASSET))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("勘定科目名が null の場合は例外をスローする")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> Account.create(AccountCode.of("1101"), null, AccountType.ASSET))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("勘定科目名は必須です");
        }

        @Test
        @DisplayName("勘定科目種別が null の場合は例外をスローする")
        void shouldThrowExceptionWhenTypeIsNull() {
            assertThatThrownBy(() -> Account.create(AccountCode.of("1101"), "現金", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("勘定科目種別は必須です");
        }
    }

    @Nested
    @DisplayName("reconstruct")
    class Reconstruct {

        @Test
        @DisplayName("reconstruct で DB から復元できる")
        void shouldReconstructFromDb() {
            AccountId id = AccountId.of(10);
            AccountCode code = AccountCode.of("4101");
            String name = "売上高";
            AccountType type = AccountType.REVENUE;

            Account account = Account.reconstruct(id, code, name, type);

            assertThat(account.getId()).isEqualTo(id);
            assertThat(account.getAccountCode()).isEqualTo(code);
            assertThat(account.getAccountName()).isEqualTo(name);
            assertThat(account.getAccountType()).isEqualTo(type);
        }
    }
}

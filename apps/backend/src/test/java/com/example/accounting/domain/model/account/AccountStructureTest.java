package com.example.accounting.domain.model.account;

import io.vavr.control.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("勘定科目構成ドメインモデル")
class AccountStructureTest {

    @Nested
    @DisplayName("create メソッド")
    class CreateMethod {

        @Test
        @DisplayName("親なしでルート構成を作成する")
        void createWithoutParentShouldCreateRootStructure() {
            Either<String, AccountStructure> result = AccountStructure.create("1000", null, null, 1);

            assertThat(result.isRight()).isTrue();
            AccountStructure structure = result.get();
            assertThat(structure.getAccountCode()).isEqualTo("1000");
            assertThat(structure.getAccountPath()).isEqualTo("1000");
            assertThat(structure.getHierarchyLevel()).isEqualTo(1);
            assertThat(structure.getParentAccountCode()).isNull();
            assertThat(structure.getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("親ありで子構成を作成する")
        void createWithParentShouldCreateChildStructure() {
            Either<String, AccountStructure> result = AccountStructure.create("1100", "1000", "1000", 1);

            assertThat(result.isRight()).isTrue();
            AccountStructure structure = result.get();
            assertThat(structure.getAccountCode()).isEqualTo("1100");
            assertThat(structure.getAccountPath()).isEqualTo("1000~1100");
            assertThat(structure.getHierarchyLevel()).isEqualTo(2);
            assertThat(structure.getParentAccountCode()).isEqualTo("1000");
            assertThat(structure.getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("祖父親ありで 3 階層構成を作成する")
        void createWithGrandParentShouldCreateThreeLevelStructure() {
            Either<String, AccountStructure> result = AccountStructure.create("1110", "1100", "1000~1100", 1);

            assertThat(result.isRight()).isTrue();
            AccountStructure structure = result.get();
            assertThat(structure.getAccountCode()).isEqualTo("1110");
            assertThat(structure.getAccountPath()).isEqualTo("1000~1100~1110");
            assertThat(structure.getHierarchyLevel()).isEqualTo(3);
            assertThat(structure.getParentAccountCode()).isEqualTo("1100");
            assertThat(structure.getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("勘定科目コードが空の場合は Left を返す")
        void createWithBlankAccountCodeShouldReturnLeft() {
            Either<String, AccountStructure> result = AccountStructure.create("", null, null, 1);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("勘定科目コードが null の場合は Left を返す")
        void createWithNullAccountCodeShouldReturnLeft() {
            Either<String, AccountStructure> result = AccountStructure.create(null, null, null, 1);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("親コードがあり親パスがない場合は Left を返す")
        void createWithParentButNoPathShouldReturnLeft() {
            Either<String, AccountStructure> result = AccountStructure.create("1100", "1000", null, 1);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo("親勘定科目パスは必須です");
        }
    }

    @Nested
    @DisplayName("reconstruct メソッド")
    class ReconstructMethod {

        @Test
        @DisplayName("DB 値からバリデーションなしで復元する")
        void reconstructShouldCreateWithoutValidation() {
            AccountStructure structure = AccountStructure.reconstruct(
                    "9999",
                    "1000~9999",
                    2,
                    "1000",
                    9
            );

            assertThat(structure.getAccountCode()).isEqualTo("9999");
            assertThat(structure.getAccountPath()).isEqualTo("1000~9999");
            assertThat(structure.getHierarchyLevel()).isEqualTo(2);
            assertThat(structure.getParentAccountCode()).isEqualTo("1000");
            assertThat(structure.getDisplayOrder()).isEqualTo(9);
        }
    }
}

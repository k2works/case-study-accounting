package com.example.accounting.domain.model.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import io.vavr.control.Either;

import java.util.Objects;

/**
 * 勘定科目構成エンティティ
 */
@Value
@With
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountStructure {

    String accountCode;
    String accountPath;
    int hierarchyLevel;
    String parentAccountCode;
    int displayOrder;

    /**
     * 新規作成用ファクトリメソッド（バリデーション付き）
     */
    public static Either<String, AccountStructure> create(
            String accountCode,
            String parentAccountCode,
            String parentAccountPath,
            int displayOrder
    ) {
        return validated(accountCode, parentAccountCode, parentAccountPath)
                .map(v -> buildStructure(accountCode, parentAccountCode, parentAccountPath, displayOrder));
    }

    /**
     * DB 復元用ファクトリメソッド
     */
    public static AccountStructure reconstruct(
            String accountCode,
            String accountPath,
            int hierarchyLevel,
            String parentAccountCode,
            int displayOrder
    ) {
        return new AccountStructure(accountCode, accountPath, hierarchyLevel, parentAccountCode, displayOrder);
    }

    private static Either<String, Void> validated(
            String accountCode,
            String parentAccountCode,
            String parentAccountPath
    ) {
        if (accountCode == null || accountCode.isBlank()) {
            return Either.left("勘定科目コードは必須です");
        }
        if (hasParent(parentAccountCode) && (parentAccountPath == null || parentAccountPath.isBlank())) {
            return Either.left("親勘定科目パスは必須です");
        }
        return Either.right(null);
    }

    private static AccountStructure buildStructure(
            String accountCode,
            String parentAccountCode,
            String parentAccountPath,
            int displayOrder
    ) {
        String path;
        int level;

        if (hasParent(parentAccountCode)) {
            Objects.requireNonNull(parentAccountPath);
            path = parentAccountPath + "~" + accountCode;
            level = path.split("~").length;
        } else {
            path = accountCode;
            level = 1;
        }

        return new AccountStructure(
                accountCode,
                path,
                level,
                normalizeParentCode(parentAccountCode),
                displayOrder
        );
    }

    private static boolean hasParent(String parentAccountCode) {
        return parentAccountCode != null && !parentAccountCode.isBlank();
    }

    @SuppressWarnings("PMD.AvoidReturningNull")
    private static String normalizeParentCode(String parentAccountCode) {
        if (hasParent(parentAccountCode)) {
            return parentAccountCode;
        }
        return null;
    }
}

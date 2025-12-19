package com.example.accounting.domain.model.user;

import java.util.Arrays;

/**
 * ユーザーロール（権限）を表す列挙型
 *
 * <p>財務会計システムにおける4段階の権限レベルを定義する。
 * 各ロールは異なる操作権限を持ち、上位ロールは下位ロールの権限を含む。</p>
 *
 * <ul>
 *   <li>ADMIN: システム全体の管理権限を持つ</li>
 *   <li>MANAGER: 経理責任者として承認権限を持つ</li>
 *   <li>USER: 経理担当者として日常の仕訳作成を行う</li>
 *   <li>VIEWER: 閲覧のみ可能</li>
 * </ul>
 */
public enum Role {
    ADMIN("管理者", true, true, true, true, true),
    MANAGER("経理責任者", false, true, true, true, true),
    USER("経理担当者", false, false, true, false, true),
    VIEWER("閲覧者", false, false, false, false, true);

    private final String displayName;
    private final boolean canManageUsers;
    private final boolean canManageAccounts;
    private final boolean canCreateJournalEntries;
    private final boolean canApproveJournalEntries;
    private final boolean canViewReports;

    Role(String displayName,
         boolean canManageUsers,
         boolean canManageAccounts,
         boolean canCreateJournalEntries,
         boolean canApproveJournalEntries,
         boolean canViewReports) {
        this.displayName = displayName;
        this.canManageUsers = canManageUsers;
        this.canManageAccounts = canManageAccounts;
        this.canCreateJournalEntries = canCreateJournalEntries;
        this.canApproveJournalEntries = canApproveJournalEntries;
        this.canViewReports = canViewReports;
    }

    /**
     * ロールコードからロールを取得する
     *
     * @param code ロールコード
     * @return 対応するロール
     * @throws IllegalArgumentException 無効なロールコードの場合
     */
    public static Role fromCode(String code) {
        return Arrays.stream(values())
                .filter(role -> role.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "無効なロールコードです: " + code));
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canManageUsers() {
        return canManageUsers;
    }

    public boolean canManageAccounts() {
        return canManageAccounts;
    }

    public boolean canCreateJournalEntries() {
        return canCreateJournalEntries;
    }

    public boolean canApproveJournalEntries() {
        return canApproveJournalEntries;
    }

    public boolean canViewReports() {
        return canViewReports;
    }
}

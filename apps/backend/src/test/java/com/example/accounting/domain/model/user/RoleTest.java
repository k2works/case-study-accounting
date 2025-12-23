package com.example.accounting.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Role 列挙型のテスト
 */
@DisplayName("Role 列挙型")
class RoleTest {

    @Test
    @DisplayName("管理者ロールは全ての権限を持つ")
    void adminShouldHaveAllPermissions() {
        // Given
        Role admin = Role.ADMIN;

        // Then
        assertThat(admin.canManageUsers()).isTrue();
        assertThat(admin.canManageAccounts()).isTrue();
        assertThat(admin.canCreateJournalEntries()).isTrue();
        assertThat(admin.canApproveJournalEntries()).isTrue();
        assertThat(admin.canViewReports()).isTrue();
    }

    @Test
    @DisplayName("経理責任者は仕訳の承認ができる")
    void managerShouldApproveJournalEntries() {
        // Given
        Role manager = Role.MANAGER;

        // Then
        assertThat(manager.canManageUsers()).isFalse();
        assertThat(manager.canManageAccounts()).isTrue();
        assertThat(manager.canCreateJournalEntries()).isTrue();
        assertThat(manager.canApproveJournalEntries()).isTrue();
        assertThat(manager.canViewReports()).isTrue();
    }

    @Test
    @DisplayName("経理担当者は仕訳の作成ができるが承認はできない")
    void userShouldCreateButNotApproveJournalEntries() {
        // Given
        Role user = Role.USER;

        // Then
        assertThat(user.canManageUsers()).isFalse();
        assertThat(user.canManageAccounts()).isFalse();
        assertThat(user.canCreateJournalEntries()).isTrue();
        assertThat(user.canApproveJournalEntries()).isFalse();
        assertThat(user.canViewReports()).isTrue();
    }

    @Test
    @DisplayName("閲覧者はレポートの閲覧のみ可能")
    void viewerShouldOnlyViewReports() {
        // Given
        Role viewer = Role.VIEWER;

        // Then
        assertThat(viewer.canManageUsers()).isFalse();
        assertThat(viewer.canManageAccounts()).isFalse();
        assertThat(viewer.canCreateJournalEntries()).isFalse();
        assertThat(viewer.canApproveJournalEntries()).isFalse();
        assertThat(viewer.canViewReports()).isTrue();
    }

    @Test
    @DisplayName("ロールコードからロールを取得できる")
    void shouldGetRoleFromCode() {
        assertThat(Role.fromCode("ADMIN")).isEqualTo(Role.ADMIN);
        assertThat(Role.fromCode("MANAGER")).isEqualTo(Role.MANAGER);
        assertThat(Role.fromCode("USER")).isEqualTo(Role.USER);
        assertThat(Role.fromCode("VIEWER")).isEqualTo(Role.VIEWER);
    }

    @Test
    @DisplayName("無効なロールコードの場合は例外が発生する")
    void shouldThrowExceptionForInvalidRoleCode() {
        assertThatThrownBy(() -> Role.fromCode("INVALID"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ロール");
    }

    @Test
    @DisplayName("各ロールの表示名を取得できる")
    void shouldGetDisplayName() {
        assertThat(Role.ADMIN.getDisplayName()).isEqualTo("管理者");
        assertThat(Role.MANAGER.getDisplayName()).isEqualTo("経理責任者");
        assertThat(Role.USER.getDisplayName()).isEqualTo("経理担当者");
        assertThat(Role.VIEWER.getDisplayName()).isEqualTo("閲覧者");
    }
}

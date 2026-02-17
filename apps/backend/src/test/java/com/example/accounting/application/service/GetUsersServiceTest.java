package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetUsersQuery;
import com.example.accounting.application.port.in.query.UserSummary;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.Email;
import com.example.accounting.domain.model.user.Password;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.model.user.Username;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ユーザー一覧取得サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ユーザー一覧取得サービス")
class GetUsersServiceTest {

    @Mock
    private UserRepository userRepository;

    private GetUsersService getUsersService;

    @BeforeEach
    void setUp() {
        getUsersService = new GetUsersService(userRepository);
    }

    @Nested
    @DisplayName("一覧取得")
    class GetUsers {

        @Test
        @DisplayName("条件がない場合は全件取得する")
        void shouldGetAllUsersWhenNoFilter() {
            // Given
            User user = buildUser("user-1", "user1", "user1@example.com", "User One", Role.USER);
            when(userRepository.findAll()).thenReturn(Try.success(List.of(user)));

            // When
            List<UserSummary> summaries = getUsersService.execute(GetUsersQuery.all());

            // Then
            assertThat(summaries).hasSize(1);
            assertThat(summaries.get(0).id()).isEqualTo("user-1");
            assertThat(summaries.get(0).username()).isEqualTo("user1");
            assertThat(summaries.get(0).email()).isEqualTo("user1@example.com");
            assertThat(summaries.get(0).displayName()).isEqualTo("User One");
            assertThat(summaries.get(0).role()).isEqualTo("USER");
            assertThat(summaries.get(0).lastLoginAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
            verify(userRepository).findAll();
            verify(userRepository, never()).search(null, null);
        }

        @Test
        @DisplayName("条件がある場合は検索する")
        void shouldSearchUsersWhenFilterExists() {
            // Given
            User user = buildUser("user-2", "manager", "manager@example.com", "Manager", Role.MANAGER);
            when(userRepository.search("MANAGER", "man")).thenReturn(Try.success(List.of(user)));

            // When
            List<UserSummary> summaries = getUsersService.execute(new GetUsersQuery("MANAGER", "man"));

            // Then
            assertThat(summaries).hasSize(1);
            assertThat(summaries.get(0).id()).isEqualTo("user-2");
            assertThat(summaries.get(0).role()).isEqualTo("MANAGER");
            verify(userRepository, never()).findAll();
            verify(userRepository).search("MANAGER", "man");
        }
    }

    private User buildUser(String id,
                           String username,
                           String email,
                           String displayName,
                           Role role) {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0);
        return User.reconstruct(
                UserId.of(id),
                Username.of(username),
                Email.of(email),
                Password.reconstruct("hashed"),
                displayName,
                role,
                true,
                false,
                0,
                now,
                now.minusDays(1),
                now.minusDays(1)
        );
    }
}

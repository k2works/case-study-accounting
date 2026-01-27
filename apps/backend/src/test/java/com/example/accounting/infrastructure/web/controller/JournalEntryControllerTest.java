package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateJournalEntryUseCase;
import com.example.accounting.application.port.in.GetJournalEntryUseCase;
import com.example.accounting.application.port.in.UpdateJournalEntryUseCase;
import com.example.accounting.application.port.in.command.CreateJournalEntryCommand;
import com.example.accounting.application.port.out.CreateJournalEntryResult;
import com.example.accounting.application.port.out.UpdateJournalEntryResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.Email;
import com.example.accounting.domain.model.user.Password;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.model.user.Username;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.UpdateJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.UpdateJournalEntryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 仕訳登録コントローラのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳登録コントローラ")
class JournalEntryControllerTest {

    @Mock
    private CreateJournalEntryUseCase createJournalEntryUseCase;

    @Mock
    private UpdateJournalEntryUseCase updateJournalEntryUseCase;

    @Mock
    private GetJournalEntryUseCase getJournalEntryUseCase;

    @Mock
    private UserRepository userRepository;

    private JournalEntryController journalEntryController;

    @BeforeEach
    void setUp() {
        journalEntryController = new JournalEntryController(
                createJournalEntryUseCase,
                updateJournalEntryUseCase,
                getJournalEntryUseCase,
                userRepository
        );
    }

    @Nested
    @DisplayName("仕訳登録")
    class CreateJournalEntry {

        @Test
        @DisplayName("有効な情報で仕訳を登録できる")
        void shouldCreateJournalEntryWithValidRequest() {
            CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                    LocalDate.of(2024, 1, 31),
                    "売上計上",
                    List.of(
                            new CreateJournalEntryRequest.JournalEntryLineRequest(1, 1,
                                    new BigDecimal("1000"), null),
                            new CreateJournalEntryRequest.JournalEntryLineRequest(2, 2,
                                    null, new BigDecimal("1000"))
                    )
            );
            CreateJournalEntryResult result = CreateJournalEntryResult.success(
                    10,
                    LocalDate.of(2024, 1, 31),
                    "売上計上",
                    "DRAFT"
            );
            when(userRepository.findByUsername("user1"))
                    .thenReturn(Optional.of(dummyUser("user-1", "user1")));
            when(createJournalEntryUseCase.execute(any(CreateJournalEntryCommand.class))).thenReturn(result);

            ResponseEntity<CreateJournalEntryResponse> response =
                    journalEntryController.create(request, principal("user1"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().journalEntryId()).isEqualTo(10);
            assertThat(response.getBody().journalDate()).isEqualTo(LocalDate.of(2024, 1, 31));
            assertThat(response.getBody().description()).isEqualTo("売上計上");
            assertThat(response.getBody().status()).isEqualTo("DRAFT");
            assertThat(response.getBody().errorMessage()).isNull();
        }

        @Test
        @DisplayName("リクエストの内容が CreateJournalEntryCommand に正しく変換される")
        void shouldConvertRequestToCommand() {
            CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                    LocalDate.of(2024, 2, 1),
                    "仕入計上",
                    List.of(
                            new CreateJournalEntryRequest.JournalEntryLineRequest(1, 10,
                                    new BigDecimal("500"), null),
                            new CreateJournalEntryRequest.JournalEntryLineRequest(2, 20,
                                    null, new BigDecimal("500"))
                    )
            );
            when(userRepository.findByUsername("user1"))
                    .thenReturn(Optional.of(dummyUser("user-1", "user1")));
            when(createJournalEntryUseCase.execute(any(CreateJournalEntryCommand.class)))
                    .thenReturn(CreateJournalEntryResult.failure("error"));

            journalEntryController.create(request, principal("user1"));

            ArgumentCaptor<CreateJournalEntryCommand> captor =
                    ArgumentCaptor.forClass(CreateJournalEntryCommand.class);
            verify(createJournalEntryUseCase).execute(captor.capture());

            CreateJournalEntryCommand command = captor.getValue();
            assertThat(command.journalDate()).isEqualTo(LocalDate.of(2024, 2, 1));
            assertThat(command.description()).isEqualTo("仕入計上");
            assertThat(command.createdByUserId()).isEqualTo("user-1");
            assertThat(command.lines()).hasSize(2);
            assertThat(command.lines().get(0).lineNumber()).isEqualTo(1);
            assertThat(command.lines().get(0).accountId()).isEqualTo(10);
            assertThat(command.lines().get(0).debitAmount()).isEqualByComparingTo("500");
            assertThat(command.lines().get(0).creditAmount()).isNull();
            assertThat(command.lines().get(1).lineNumber()).isEqualTo(2);
            assertThat(command.lines().get(1).accountId()).isEqualTo(20);
            assertThat(command.lines().get(1).debitAmount()).isNull();
            assertThat(command.lines().get(1).creditAmount()).isEqualByComparingTo("500");
        }

        @Test
        @DisplayName("登録に失敗した場合は 400 を返す")
        void shouldReturnBadRequestWhenCreationFails() {
            CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                    LocalDate.of(2024, 1, 31),
                    "売上計上",
                    List.of(
                            new CreateJournalEntryRequest.JournalEntryLineRequest(1, 1,
                                    new BigDecimal("1000"), null)
                    )
            );
            when(userRepository.findByUsername("user1"))
                    .thenReturn(Optional.of(dummyUser("user-1", "user1")));
            when(createJournalEntryUseCase.execute(any(CreateJournalEntryCommand.class)))
                    .thenReturn(CreateJournalEntryResult.failure("勘定科目が存在しません"));

            ResponseEntity<CreateJournalEntryResponse> response =
                    journalEntryController.create(request, principal("user1"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("勘定科目が存在しません");
        }
    }

    @Nested
    @DisplayName("仕訳編集")
    class UpdateJournalEntry {

        @Test
        @DisplayName("有効な情報で仕訳を編集できる")
        void shouldUpdateJournalEntryWithValidRequest() {
            UpdateJournalEntryRequest request = new UpdateJournalEntryRequest(
                    LocalDate.of(2024, 2, 10),
                    "摘要更新",
                    List.of(
                            new UpdateJournalEntryRequest.JournalEntryLineRequest(1, 1,
                                    new BigDecimal("2000"), null),
                            new UpdateJournalEntryRequest.JournalEntryLineRequest(2, 2,
                                    null, new BigDecimal("2000"))
                    ),
                    1
            );
            UpdateJournalEntryResult result = UpdateJournalEntryResult.success(
                    10,
                    LocalDate.of(2024, 2, 10),
                    "摘要更新",
                    "DRAFT",
                    2
            );
            when(updateJournalEntryUseCase.execute(any()))
                    .thenReturn(result);

            ResponseEntity<UpdateJournalEntryResponse> response =
                    journalEntryController.update(10, request, principal("user1"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().journalEntryId()).isEqualTo(10);
            assertThat(response.getBody().journalDate()).isEqualTo(LocalDate.of(2024, 2, 10));
            assertThat(response.getBody().description()).isEqualTo("摘要更新");
            assertThat(response.getBody().status()).isEqualTo("DRAFT");
            assertThat(response.getBody().version()).isEqualTo(2);
            assertThat(response.getBody().message()).isEqualTo("仕訳を更新しました");
            assertThat(response.getBody().errorMessage()).isNull();
        }

        @Test
        @DisplayName("仕訳が存在しない場合は 404 を返す")
        void shouldReturnNotFoundWhenJournalEntryMissing() {
            UpdateJournalEntryRequest request = new UpdateJournalEntryRequest(
                    LocalDate.of(2024, 2, 10),
                    "摘要更新",
                    List.of(
                            new UpdateJournalEntryRequest.JournalEntryLineRequest(1, 1,
                                    new BigDecimal("2000"), null)
                    ),
                    1
            );
            when(updateJournalEntryUseCase.execute(any()))
                    .thenReturn(UpdateJournalEntryResult.failure("仕訳が見つかりません"));

            ResponseEntity<UpdateJournalEntryResponse> response =
                    journalEntryController.update(10, request, principal("user1"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("バリデーションエラーの場合は 400 を返す")
        void shouldReturnBadRequestWhenValidationFails() {
            UpdateJournalEntryRequest request = new UpdateJournalEntryRequest(
                    LocalDate.of(2024, 2, 10),
                    "摘要更新",
                    List.of(),
                    1
            );
            when(updateJournalEntryUseCase.execute(any()))
                    .thenReturn(UpdateJournalEntryResult.failure("仕訳明細は 1 行以上必要です"));

            ResponseEntity<UpdateJournalEntryResponse> response =
                    journalEntryController.update(10, request, principal("user1"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("仕訳明細は 1 行以上必要です");
        }

        @Test
        @DisplayName("楽観的ロックエラーの場合は 409 を返す")
        void shouldReturnConflictWhenOptimisticLockFails() {
            UpdateJournalEntryRequest request = new UpdateJournalEntryRequest(
                    LocalDate.of(2024, 2, 10),
                    "摘要更新",
                    List.of(
                            new UpdateJournalEntryRequest.JournalEntryLineRequest(1, 1,
                                    new BigDecimal("2000"), null)
                    ),
                    1
            );
            when(updateJournalEntryUseCase.execute(any()))
                    .thenReturn(UpdateJournalEntryResult.failure("仕訳のバージョンが一致しません"));

            ResponseEntity<UpdateJournalEntryResponse> response =
                    journalEntryController.update(10, request, principal("user1"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("仕訳のバージョンが一致しません");
        }
    }

    private Principal principal(String name) {
        return () -> name;
    }

    private User dummyUser(String userId, String username) {
        return User.reconstruct(
                UserId.of(userId),
                Username.reconstruct(username),
                Email.reconstruct("user@example.com"),
                Password.reconstruct("hashed"),
                "User",
                Role.USER,
                true,
                false,
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}

package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateJournalEntryUseCase;
import com.example.accounting.application.port.in.DeleteJournalEntryUseCase;
import com.example.accounting.application.port.in.GetJournalEntryUseCase;
import com.example.accounting.application.port.in.GetJournalEntriesUseCase;
import com.example.accounting.application.port.in.ApproveJournalEntryUseCase;
import com.example.accounting.application.port.in.RejectJournalEntryUseCase;
import com.example.accounting.application.port.in.SearchJournalEntriesUseCase;
import com.example.accounting.application.port.in.SubmitForApprovalUseCase;
import com.example.accounting.application.port.in.UpdateJournalEntryUseCase;
import com.example.accounting.application.port.in.command.CreateJournalEntryCommand;
import com.example.accounting.application.port.in.command.ApproveJournalEntryCommand;
import com.example.accounting.application.port.in.command.DeleteJournalEntryCommand;
import com.example.accounting.application.port.in.command.SubmitForApprovalCommand;
import com.example.accounting.application.port.in.query.GetJournalEntriesQuery;
import com.example.accounting.application.port.in.query.SearchJournalEntriesQuery;
import com.example.accounting.application.port.in.command.RejectJournalEntryCommand;
import com.example.accounting.application.port.out.ApproveJournalEntryResult;
import com.example.accounting.application.port.out.RejectJournalEntryResult;
import com.example.accounting.application.port.out.CreateJournalEntryResult;
import com.example.accounting.application.port.out.DeleteJournalEntryResult;
import com.example.accounting.application.port.out.GetJournalEntriesResult;
import com.example.accounting.application.port.out.JournalEntryDetailResult;
import com.example.accounting.application.port.out.SubmitForApprovalResult;
import com.example.accounting.application.port.out.UpdateJournalEntryResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.Email;
import com.example.accounting.domain.model.user.Password;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.model.user.Username;
import com.example.accounting.domain.shared.OptimisticLockException;
import com.example.accounting.infrastructure.web.dto.ApproveJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.RejectJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.RejectJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.DeleteJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.JournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.SubmitForApprovalResponse;
import com.example.accounting.infrastructure.web.dto.UpdateJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.UpdateJournalEntryResponse;
import com.example.accounting.infrastructure.web.exception.BusinessException;
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
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 仕訳登録コントローラのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳登録コントローラ")
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.ExcessiveImports"}) // テストクラスは多数のクラスを使用するため結合度が高くなる
class JournalEntryControllerTest {

    @Mock
    private CreateJournalEntryUseCase createJournalEntryUseCase;

    @Mock
    private UpdateJournalEntryUseCase updateJournalEntryUseCase;

    @Mock
    private GetJournalEntryUseCase getJournalEntryUseCase;

    @Mock
    private DeleteJournalEntryUseCase deleteJournalEntryUseCase;

    @Mock
    private GetJournalEntriesUseCase getJournalEntriesUseCase;

    @Mock
    private SearchJournalEntriesUseCase searchJournalEntriesUseCase;

    @Mock
    private SubmitForApprovalUseCase submitForApprovalUseCase;

    @Mock
    private ApproveJournalEntryUseCase approveJournalEntryUseCase;

    @Mock
    private RejectJournalEntryUseCase rejectJournalEntryUseCase;

    @Mock
    private UserRepository userRepository;

    private JournalEntryController journalEntryController;

    @BeforeEach
    void setUp() {
        journalEntryController = new JournalEntryController(
                createJournalEntryUseCase,
                updateJournalEntryUseCase,
                getJournalEntryUseCase,
                deleteJournalEntryUseCase,
                getJournalEntriesUseCase,
                searchJournalEntriesUseCase,
                submitForApprovalUseCase,
                approveJournalEntryUseCase,
                rejectJournalEntryUseCase,
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

    @Nested
    @DisplayName("仕訳一覧取得")
    class FindAllPaged {

        @Test
        @DisplayName("デフォルトパラメータで一覧を取得できる")
        void shouldReturnPagedResults() {
            GetJournalEntriesResult result = GetJournalEntriesResult.empty(0, 20);
            when(getJournalEntriesUseCase.execute(any(GetJournalEntriesQuery.class))).thenReturn(result);

            ResponseEntity<GetJournalEntriesResult> response =
                    journalEntryController.findAllPaged(0, 20, null, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().content()).isEmpty();
        }

        @Test
        @DisplayName("ステータスフィルタ付きで一覧を取得できる")
        void shouldReturnPagedResultsWithStatusFilter() {
            GetJournalEntriesResult result = GetJournalEntriesResult.empty(0, 20);
            when(getJournalEntriesUseCase.execute(any(GetJournalEntriesQuery.class))).thenReturn(result);

            ResponseEntity<GetJournalEntriesResult> response =
                    journalEntryController.findAllPaged(0, 20, List.of("DRAFT"), null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("日付範囲フィルタ付きで一覧を取得できる")
        void shouldReturnPagedResultsWithDateFilter() {
            GetJournalEntriesResult result = GetJournalEntriesResult.empty(0, 20);
            when(getJournalEntriesUseCase.execute(any(GetJournalEntriesQuery.class))).thenReturn(result);

            ResponseEntity<GetJournalEntriesResult> response =
                    journalEntryController.findAllPaged(0, 20, null,
                            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("仕訳検索")
    class SearchJournalEntries {

        @Test
        @DisplayName("全条件指定で検索できる")
        void shouldSearchWithAllCriteria() {
            GetJournalEntriesResult result = GetJournalEntriesResult.empty(0, 20);
            when(searchJournalEntriesUseCase.execute(any(SearchJournalEntriesQuery.class))).thenReturn(result);

            ResponseEntity<GetJournalEntriesResult> response =
                    journalEntryController.search(0, 20, List.of("DRAFT"),
                            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                            100, new BigDecimal("1000"), new BigDecimal("5000"), "売上");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("条件なしで検索できる")
        void shouldSearchWithNoCriteria() {
            GetJournalEntriesResult result = GetJournalEntriesResult.empty(0, 20);
            when(searchJournalEntriesUseCase.execute(any(SearchJournalEntriesQuery.class))).thenReturn(result);

            ResponseEntity<GetJournalEntriesResult> response =
                    journalEntryController.search(0, 20, null, null, null, null, null, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("仕訳詳細取得")
    class FindById {

        @Test
        @DisplayName("IDで仕訳詳細を取得できる")
        void shouldReturnJournalEntryById() {
            JournalEntryDetailResult detail = new JournalEntryDetailResult(
                    1, LocalDate.of(2024, 1, 31), "売上計上", "DRAFT", 1,
                    List.of(new JournalEntryDetailResult.JournalEntryLineDetail(
                            1, 100, "1101", "現金", new BigDecimal("1000"), null
                    ))
            );
            when(getJournalEntryUseCase.findById(1)).thenReturn(Optional.of(detail));

            ResponseEntity<JournalEntryResponse> response = journalEntryController.findById(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("存在しない場合は404を返す")
        void shouldReturn404WhenNotFound() {
            when(getJournalEntryUseCase.findById(999)).thenReturn(Optional.empty());

            ResponseEntity<JournalEntryResponse> response = journalEntryController.findById(999);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("仕訳削除")
    class DeleteJournalEntry {

        @Test
        @DisplayName("削除成功時は200を返す")
        void shouldReturnOkWhenDeleteSucceeds() {
            when(deleteJournalEntryUseCase.execute(any(DeleteJournalEntryCommand.class)))
                    .thenReturn(DeleteJournalEntryResult.ofSuccess());

            ResponseEntity<DeleteJournalEntryResponse> response = journalEntryController.delete(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
        }

        @Test
        @DisplayName("仕訳が見つからない場合は404を返す")
        void shouldReturn404WhenNotFound() {
            when(deleteJournalEntryUseCase.execute(any(DeleteJournalEntryCommand.class)))
                    .thenReturn(DeleteJournalEntryResult.ofFailure("仕訳が見つかりません"));

            ResponseEntity<DeleteJournalEntryResponse> response = journalEntryController.delete(999);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("削除不可の場合は400を返す")
        void shouldReturn400WhenDeleteNotAllowed() {
            when(deleteJournalEntryUseCase.execute(any(DeleteJournalEntryCommand.class)))
                    .thenReturn(DeleteJournalEntryResult.ofFailure("下書き状態の仕訳のみ削除可能です"));

            ResponseEntity<DeleteJournalEntryResponse> response = journalEntryController.delete(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
        }
    }

    @Nested
    @DisplayName("仕訳編集 - OptimisticLockException")
    class UpdateJournalEntryOptimisticLock {

        @Test
        @DisplayName("OptimisticLockException発生時は409を返す")
        void shouldReturn409WhenOptimisticLockExceptionThrown() {
            UpdateJournalEntryRequest request = new UpdateJournalEntryRequest(
                    LocalDate.of(2024, 2, 10), "摘要更新",
                    List.of(new UpdateJournalEntryRequest.JournalEntryLineRequest(1, 1,
                            new BigDecimal("2000"), null)),
                    1
            );
            when(updateJournalEntryUseCase.execute(any()))
                    .thenThrow(new OptimisticLockException("他のユーザーが更新済み"));

            ResponseEntity<UpdateJournalEntryResponse> response =
                    journalEntryController.update(10, request, principal("user1"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errorMessage()).isEqualTo("他のユーザーが更新済み");
        }
    }

    @Nested
    @DisplayName("仕訳登録 - ユーザー不在")
    class CreateJournalEntryUserNotFound {

        @Test
        @DisplayName("ユーザーが見つからない場合はBusinessExceptionをスローする")
        void shouldThrowWhenUserNotFound() {
            CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                    LocalDate.of(2024, 1, 31), "売上計上",
                    List.of(new CreateJournalEntryRequest.JournalEntryLineRequest(1, 1,
                            new BigDecimal("1000"), null))
            );
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> journalEntryController.create(request, principal("unknown")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ユーザーが存在しません");
        }
    }

    @Nested
    @DisplayName("仕訳承認申請")
    class SubmitForApproval {

        @Test
        @DisplayName("承認申請が成功した場合は200を返す")
        void shouldReturnOkWhenSubmitSucceeds() {
            SubmitForApprovalResult result = SubmitForApprovalResult.success(1, "PENDING");
            when(submitForApprovalUseCase.execute(any(SubmitForApprovalCommand.class))).thenReturn(result);

            ResponseEntity<SubmitForApprovalResponse> response = journalEntryController.submitForApproval(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().journalEntryId()).isEqualTo(1);
            assertThat(response.getBody().status()).isEqualTo("PENDING");
            assertThat(response.getBody().message()).isEqualTo("仕訳を承認申請しました");
        }

        @Test
        @DisplayName("仕訳が見つからない場合は404を返す")
        void shouldReturn404WhenNotFound() {
            when(submitForApprovalUseCase.execute(any(SubmitForApprovalCommand.class)))
                    .thenReturn(SubmitForApprovalResult.failure("仕訳が見つかりません"));

            ResponseEntity<SubmitForApprovalResponse> response = journalEntryController.submitForApproval(999);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("ステータスエラーの場合は400を返す")
        void shouldReturn400WhenStatusError() {
            when(submitForApprovalUseCase.execute(any(SubmitForApprovalCommand.class)))
                    .thenReturn(SubmitForApprovalResult.failure("下書き状態の仕訳のみ承認申請できます"));

            ResponseEntity<SubmitForApprovalResponse> response = journalEntryController.submitForApproval(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("下書き状態の仕訳のみ承認申請できます");
        }
    }

    @Nested
    @DisplayName("仕訳承認")
    class ApproveJournalEntry {

        @Test
        @DisplayName("承認が成功した場合は200を返す")
        void shouldReturnOkWhenApproveSucceeds() {
            LocalDateTime approvedAt = LocalDateTime.of(2024, 2, 15, 10, 30);
            ApproveJournalEntryResult result = ApproveJournalEntryResult.success(1, "APPROVED", "manager", approvedAt);
            when(approveJournalEntryUseCase.execute(any(ApproveJournalEntryCommand.class))).thenReturn(result);

            ResponseEntity<ApproveJournalEntryResponse> response =
                    journalEntryController.approveJournalEntry(1, userDetails("manager"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().journalEntryId()).isEqualTo(1);
            assertThat(response.getBody().status()).isEqualTo("APPROVED");
            assertThat(response.getBody().approvedBy()).isEqualTo("manager");
            assertThat(response.getBody().approvedAt()).isEqualTo(approvedAt);
            assertThat(response.getBody().message()).isEqualTo("仕訳を承認しました");
        }

        @Test
        @DisplayName("仕訳が見つからない場合は404を返す")
        void shouldReturn404WhenNotFound() {
            when(approveJournalEntryUseCase.execute(any(ApproveJournalEntryCommand.class)))
                    .thenReturn(ApproveJournalEntryResult.failure("仕訳が見つかりません"));

            ResponseEntity<ApproveJournalEntryResponse> response =
                    journalEntryController.approveJournalEntry(999, userDetails("manager"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("ステータスエラーの場合は400を返す")
        void shouldReturn400WhenStatusError() {
            when(approveJournalEntryUseCase.execute(any(ApproveJournalEntryCommand.class)))
                    .thenReturn(ApproveJournalEntryResult.failure("承認待ち状態の仕訳のみ承認できます"));

            ResponseEntity<ApproveJournalEntryResponse> response =
                    journalEntryController.approveJournalEntry(1, userDetails("manager"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("承認待ち状態の仕訳のみ承認できます");
        }
    }

    @Nested
    @DisplayName("仕訳差し戻し")
    class RejectJournalEntry {

        @Test
        @DisplayName("差し戻しが成功した場合は200を返す")
        void shouldReturnOkWhenRejectSucceeds() {
            LocalDateTime rejectedAt = LocalDateTime.of(2024, 2, 20, 14, 0);
            RejectJournalEntryResult result = RejectJournalEntryResult.success(
                    1, "DRAFT", "manager", rejectedAt, "金額に誤りがあります");
            when(rejectJournalEntryUseCase.execute(any(RejectJournalEntryCommand.class))).thenReturn(result);

            RejectJournalEntryRequest request = new RejectJournalEntryRequest("金額に誤りがあります");
            ResponseEntity<RejectJournalEntryResponse> response =
                    journalEntryController.rejectJournalEntry(1, request, userDetails("manager"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().journalEntryId()).isEqualTo(1);
            assertThat(response.getBody().status()).isEqualTo("DRAFT");
            assertThat(response.getBody().rejectedBy()).isEqualTo("manager");
            assertThat(response.getBody().rejectedAt()).isEqualTo(rejectedAt);
            assertThat(response.getBody().rejectionReason()).isEqualTo("金額に誤りがあります");
            assertThat(response.getBody().message()).isEqualTo("仕訳を差し戻しました");
        }

        @Test
        @DisplayName("仕訳が見つからない場合は404を返す")
        void shouldReturn404WhenNotFound() {
            when(rejectJournalEntryUseCase.execute(any(RejectJournalEntryCommand.class)))
                    .thenReturn(RejectJournalEntryResult.failure("仕訳が見つかりません"));

            RejectJournalEntryRequest request = new RejectJournalEntryRequest("理由");
            ResponseEntity<RejectJournalEntryResponse> response =
                    journalEntryController.rejectJournalEntry(999, request, userDetails("manager"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("ステータスエラーの場合は400を返す")
        void shouldReturn400WhenStatusError() {
            when(rejectJournalEntryUseCase.execute(any(RejectJournalEntryCommand.class)))
                    .thenReturn(RejectJournalEntryResult.failure("承認待ち状態の仕訳のみ差し戻し可能です"));

            RejectJournalEntryRequest request = new RejectJournalEntryRequest("理由");
            ResponseEntity<RejectJournalEntryResponse> response =
                    journalEntryController.rejectJournalEntry(1, request, userDetails("manager"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("承認待ち状態の仕訳のみ差し戻し可能です");
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

    private UserDetails userDetails(String username) {
        return new org.springframework.security.core.userdetails.User(
                username,
                "password",
                List.of()
        );
    }
}

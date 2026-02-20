package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateAccountStructureUseCase;
import com.example.accounting.application.port.in.DeleteAccountStructureUseCase;
import com.example.accounting.application.port.in.UpdateAccountStructureUseCase;
import com.example.accounting.application.port.in.command.CreateAccountStructureCommand;
import com.example.accounting.application.port.in.command.DeleteAccountStructureCommand;
import com.example.accounting.application.port.in.command.UpdateAccountStructureCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountStructureRepository;
import com.example.accounting.application.port.out.CreateAccountStructureResult;
import com.example.accounting.application.port.out.DeleteAccountStructureResult;
import com.example.accounting.application.port.out.UpdateAccountStructureResult;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountStructure;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.web.dto.AccountStructureResponse;
import com.example.accounting.infrastructure.web.dto.CreateAccountStructureRequest;
import com.example.accounting.infrastructure.web.dto.CreateAccountStructureResponse;
import com.example.accounting.infrastructure.web.dto.DeleteAccountStructureResponse;
import com.example.accounting.infrastructure.web.dto.UpdateAccountStructureRequest;
import com.example.accounting.infrastructure.web.dto.UpdateAccountStructureResponse;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("勘定科目構成コントローラ")
class AccountStructureControllerTest {

    @Mock
    private CreateAccountStructureUseCase createAccountStructureUseCase;

    @Mock
    private UpdateAccountStructureUseCase updateAccountStructureUseCase;

    @Mock
    private DeleteAccountStructureUseCase deleteAccountStructureUseCase;

    @Mock
    private AccountStructureRepository accountStructureRepository;

    @Mock
    private AccountRepository accountRepository;

    private AccountStructureController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountStructureController(
                createAccountStructureUseCase,
                updateAccountStructureUseCase,
                deleteAccountStructureUseCase,
                accountStructureRepository,
                accountRepository
        );
    }

    @Nested
    @DisplayName("勘定科目構成一覧取得")
    class FindAll {

        @Test
        @DisplayName("勘定科目名付きで一覧を返す")
        void findAllShouldReturnMappedResponses() {
            AccountStructure structure = AccountStructure.reconstruct("1000", "1000", 1, null, 1);
            Account account = Account.reconstruct(AccountId.of(1), AccountCode.of("1000"), "現金", AccountType.ASSET);
            when(accountStructureRepository.findAll()).thenReturn(Try.success(List.of(structure)));
            when(accountRepository.findAll()).thenReturn(Try.success(List.of(account)));

            ResponseEntity<List<AccountStructureResponse>> response = controller.findAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).accountCode()).isEqualTo("1000");
            assertThat(response.getBody().get(0).accountName()).isEqualTo("現金");
            assertThat(response.getBody().get(0).accountPath()).isEqualTo("1000");
            assertThat(response.getBody().get(0).hierarchyLevel()).isEqualTo(1);
            assertThat(response.getBody().get(0).parentAccountCode()).isNull();
            assertThat(response.getBody().get(0).displayOrder()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("勘定科目構成単体取得")
    class FindByCode {

        @Test
        @DisplayName("存在する場合は 200 を返す")
        void findByCodeFoundShouldReturnOk() {
            AccountStructure structure = AccountStructure.reconstruct("1000", "1000", 1, null, 1);
            Account account = Account.reconstruct(AccountId.of(1), AccountCode.of("1000"), "現金", AccountType.ASSET);
            when(accountRepository.findAll()).thenReturn(Try.success(List.of(account)));
            when(accountStructureRepository.findByCode("1000")).thenReturn(Try.success(Optional.of(structure)));

            ResponseEntity<AccountStructureResponse> response = controller.findByCode("1000");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().accountCode()).isEqualTo("1000");
            assertThat(response.getBody().accountName()).isEqualTo("現金");
        }

        @Test
        @DisplayName("存在しない場合は 404 を返す")
        void findByCodeNotFoundShouldReturnNotFound() {
            when(accountRepository.findAll()).thenReturn(Try.success(List.of()));
            when(accountStructureRepository.findByCode("9999")).thenReturn(Try.success(Optional.empty()));

            ResponseEntity<AccountStructureResponse> response = controller.findByCode("9999");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("勘定科目構成登録")
    class Create {

        @Test
        @DisplayName("成功時は 200 を返す")
        void createSuccessShouldReturnOk() {
            CreateAccountStructureRequest request = new CreateAccountStructureRequest("1100", "1000", 2);
            when(createAccountStructureUseCase.execute(any(CreateAccountStructureCommand.class)))
                    .thenReturn(CreateAccountStructureResult.success("1100", "1000~1100", 2, "1000", 2));

            ResponseEntity<CreateAccountStructureResponse> response = controller.create(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().accountCode()).isEqualTo("1100");
            assertThat(response.getBody().accountPath()).isEqualTo("1000~1100");
            assertThat(response.getBody().hierarchyLevel()).isEqualTo(2);
            assertThat(response.getBody().parentAccountCode()).isEqualTo("1000");
            assertThat(response.getBody().displayOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("失敗時は 400 を返す")
        void createFailureShouldReturnBadRequest() {
            CreateAccountStructureRequest request = new CreateAccountStructureRequest("1100", "1000", 2);
            when(createAccountStructureUseCase.execute(any(CreateAccountStructureCommand.class)))
                    .thenReturn(CreateAccountStructureResult.failure("勘定科目構成は既に登録されています"));

            ResponseEntity<CreateAccountStructureResponse> response = controller.create(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("勘定科目構成は既に登録されています");
        }
    }

    @Nested
    @DisplayName("勘定科目構成更新")
    class Update {

        @Test
        @DisplayName("成功時は 200 を返す")
        void updateSuccessShouldReturnOk() {
            UpdateAccountStructureRequest request = new UpdateAccountStructureRequest("1000", 3);
            when(updateAccountStructureUseCase.execute(any(UpdateAccountStructureCommand.class)))
                    .thenReturn(UpdateAccountStructureResult.success(
                            "1100",
                            "1000~1100",
                            2,
                            "1000",
                            3,
                            "勘定科目構成を更新しました"
                    ));

            ResponseEntity<UpdateAccountStructureResponse> response = controller.update("1100", request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().accountCode()).isEqualTo("1100");
            assertThat(response.getBody().message()).isEqualTo("勘定科目構成を更新しました");
        }

        @Test
        @DisplayName("失敗時は 400 を返す")
        void updateFailureShouldReturnBadRequest() {
            UpdateAccountStructureRequest request = new UpdateAccountStructureRequest("1000", 3);
            when(updateAccountStructureUseCase.execute(any(UpdateAccountStructureCommand.class)))
                    .thenReturn(UpdateAccountStructureResult.failure("勘定科目構成が見つかりません"));

            ResponseEntity<UpdateAccountStructureResponse> response = controller.update("9999", request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("勘定科目構成が見つかりません");
        }
    }

    @Nested
    @DisplayName("勘定科目構成削除")
    class Delete {

        @Test
        @DisplayName("成功時は 200 を返す")
        void deleteSuccessShouldReturnOk() {
            when(deleteAccountStructureUseCase.execute(any(DeleteAccountStructureCommand.class)))
                    .thenReturn(DeleteAccountStructureResult.success("1100"));

            ResponseEntity<DeleteAccountStructureResponse> response = controller.delete("1100");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().accountCode()).isEqualTo("1100");
        }

        @Test
        @DisplayName("対象がない場合は 404 を返す")
        void deleteNotFoundShouldReturnNotFound() {
            when(deleteAccountStructureUseCase.execute(any(DeleteAccountStructureCommand.class)))
                    .thenReturn(DeleteAccountStructureResult.failure("勘定科目構成が見つかりません"));

            ResponseEntity<DeleteAccountStructureResponse> response = controller.delete("9999");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
        }

        @Test
        @DisplayName("子階層がある場合は 409 を返す")
        void deleteWithChildrenShouldReturnConflict() {
            when(deleteAccountStructureUseCase.execute(any(DeleteAccountStructureCommand.class)))
                    .thenReturn(DeleteAccountStructureResult.failure("子階層が存在するため削除できません"));

            ResponseEntity<DeleteAccountStructureResponse> response = controller.delete("1000");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
        }
    }
}

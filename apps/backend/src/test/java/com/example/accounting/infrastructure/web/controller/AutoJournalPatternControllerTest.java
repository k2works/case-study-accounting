package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateAutoJournalPatternUseCase;
import com.example.accounting.application.port.in.DeleteAutoJournalPatternUseCase;
import com.example.accounting.application.port.in.UpdateAutoJournalPatternUseCase;
import com.example.accounting.application.port.in.command.CreateAutoJournalPatternCommand;
import com.example.accounting.application.port.in.command.DeleteAutoJournalPatternCommand;
import com.example.accounting.application.port.in.command.UpdateAutoJournalPatternCommand;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.CreateAutoJournalPatternResult;
import com.example.accounting.application.port.out.DeleteAutoJournalPatternResult;
import com.example.accounting.application.port.out.UpdateAutoJournalPatternResult;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import com.example.accounting.infrastructure.web.dto.AutoJournalPatternResponse;
import com.example.accounting.infrastructure.web.dto.CreateAutoJournalPatternRequest;
import com.example.accounting.infrastructure.web.dto.CreateAutoJournalPatternResponse;
import com.example.accounting.infrastructure.web.dto.DeleteAutoJournalPatternResponse;
import com.example.accounting.infrastructure.web.dto.UpdateAutoJournalPatternRequest;
import com.example.accounting.infrastructure.web.dto.UpdateAutoJournalPatternResponse;
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
@DisplayName("自動仕訳パターンコントローラ")
class AutoJournalPatternControllerTest {

    @Mock
    private CreateAutoJournalPatternUseCase createAutoJournalPatternUseCase;

    @Mock
    private UpdateAutoJournalPatternUseCase updateAutoJournalPatternUseCase;

    @Mock
    private DeleteAutoJournalPatternUseCase deleteAutoJournalPatternUseCase;

    @Mock
    private AutoJournalPatternRepository autoJournalPatternRepository;

    private AutoJournalPatternController controller;

    @BeforeEach
    void setUp() {
        controller = new AutoJournalPatternController(
                createAutoJournalPatternUseCase,
                updateAutoJournalPatternUseCase,
                deleteAutoJournalPatternUseCase,
                autoJournalPatternRepository
        );
    }

    @Nested
    @DisplayName("自動仕訳パターン一覧取得")
    class FindAll {

        @Test
        @DisplayName("パターン一覧を返す")
        void findAllShouldReturnPatterns() {
            AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                    AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", "説明", true,
                    List.of(AutoJournalPatternItem.reconstruct(1, "D", "1100", "amount", "売上"))
            );
            when(autoJournalPatternRepository.findAll()).thenReturn(Try.success(List.of(pattern)));

            ResponseEntity<List<AutoJournalPatternResponse>> response = controller.findAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).patternId()).isEqualTo(1L);
            assertThat(response.getBody().get(0).patternCode()).isEqualTo("AP001");
            assertThat(response.getBody().get(0).patternName()).isEqualTo("売上計上");
            assertThat(response.getBody().get(0).sourceTableName()).isEqualTo("sales");
            assertThat(response.getBody().get(0).description()).isEqualTo("説明");
            assertThat(response.getBody().get(0).isActive()).isTrue();
            assertThat(response.getBody().get(0).items()).hasSize(1);
        }

        @Test
        @DisplayName("空リストを返す")
        void findAllShouldReturnEmptyList() {
            when(autoJournalPatternRepository.findAll()).thenReturn(Try.success(List.of()));

            ResponseEntity<List<AutoJournalPatternResponse>> response = controller.findAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("自動仕訳パターン単体取得")
    class FindById {

        @Test
        @DisplayName("存在する場合は 200 を返す")
        void findByIdFoundShouldReturnOk() {
            AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                    AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", null, true, List.of()
            );
            when(autoJournalPatternRepository.findById(AutoJournalPatternId.of(1L)))
                    .thenReturn(Try.success(Optional.of(pattern)));

            ResponseEntity<AutoJournalPatternResponse> response = controller.findById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().patternId()).isEqualTo(1L);
            assertThat(response.getBody().patternCode()).isEqualTo("AP001");
        }

        @Test
        @DisplayName("存在しない場合は 404 を返す")
        void findByIdNotFoundShouldReturnNotFound() {
            when(autoJournalPatternRepository.findById(AutoJournalPatternId.of(999L)))
                    .thenReturn(Try.success(Optional.empty()));

            ResponseEntity<AutoJournalPatternResponse> response = controller.findById(999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("自動仕訳パターン登録")
    class Create {

        @Test
        @DisplayName("成功時は 200 を返す")
        void createSuccessShouldReturnOk() {
            CreateAutoJournalPatternRequest request = new CreateAutoJournalPatternRequest(
                    "AP001", "売上計上", "sales", "説明",
                    List.of(new CreateAutoJournalPatternRequest.PatternItemRequest(1, "D", "1100", "amount", "売上"))
            );
            when(createAutoJournalPatternUseCase.execute(any(CreateAutoJournalPatternCommand.class)))
                    .thenReturn(CreateAutoJournalPatternResult.success(1L, "AP001", "売上計上"));

            ResponseEntity<CreateAutoJournalPatternResponse> response = controller.create(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().patternId()).isEqualTo(1L);
            assertThat(response.getBody().patternCode()).isEqualTo("AP001");
            assertThat(response.getBody().patternName()).isEqualTo("売上計上");
        }

        @Test
        @DisplayName("失敗時は 400 を返す")
        void createFailureShouldReturnBadRequest() {
            CreateAutoJournalPatternRequest request = new CreateAutoJournalPatternRequest(
                    "AP001", "売上計上", "sales", null,
                    List.of(new CreateAutoJournalPatternRequest.PatternItemRequest(1, "D", "1100", "amount", null))
            );
            when(createAutoJournalPatternUseCase.execute(any(CreateAutoJournalPatternCommand.class)))
                    .thenReturn(CreateAutoJournalPatternResult.failure("パターンコードは既に使用されています"));

            ResponseEntity<CreateAutoJournalPatternResponse> response = controller.create(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("パターンコードは既に使用されています");
        }
    }

    @Nested
    @DisplayName("自動仕訳パターン更新")
    class Update {

        @Test
        @DisplayName("成功時は 200 を返す")
        void updateSuccessShouldReturnOk() {
            UpdateAutoJournalPatternRequest request = new UpdateAutoJournalPatternRequest(
                    "売上計上（更新）", "sales_v2", "新説明", true,
                    List.of(new UpdateAutoJournalPatternRequest.PatternItemRequest(1, "D", "1200", "total", "更新後"))
            );
            when(updateAutoJournalPatternUseCase.execute(any(UpdateAutoJournalPatternCommand.class)))
                    .thenReturn(UpdateAutoJournalPatternResult.success(1L, "AP001", "売上計上（更新）", "自動仕訳パターンを更新しました"));

            ResponseEntity<UpdateAutoJournalPatternResponse> response = controller.update(1L, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().patternId()).isEqualTo(1L);
            assertThat(response.getBody().patternCode()).isEqualTo("AP001");
            assertThat(response.getBody().patternName()).isEqualTo("売上計上（更新）");
            assertThat(response.getBody().message()).isEqualTo("自動仕訳パターンを更新しました");
        }

        @Test
        @DisplayName("失敗時は 400 を返す")
        void updateFailureShouldReturnBadRequest() {
            UpdateAutoJournalPatternRequest request = new UpdateAutoJournalPatternRequest(
                    "売上計上", "sales", null, true,
                    List.of(new UpdateAutoJournalPatternRequest.PatternItemRequest(1, "D", "1100", "amount", null))
            );
            when(updateAutoJournalPatternUseCase.execute(any(UpdateAutoJournalPatternCommand.class)))
                    .thenReturn(UpdateAutoJournalPatternResult.failure("自動仕訳パターンが見つかりません"));

            ResponseEntity<UpdateAutoJournalPatternResponse> response = controller.update(999L, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("自動仕訳パターンが見つかりません");
        }
    }

    @Nested
    @DisplayName("自動仕訳パターン削除")
    class Delete {

        @Test
        @DisplayName("成功時は 200 を返す")
        void deleteSuccessShouldReturnOk() {
            when(deleteAutoJournalPatternUseCase.execute(any(DeleteAutoJournalPatternCommand.class)))
                    .thenReturn(DeleteAutoJournalPatternResult.success(1L));

            ResponseEntity<DeleteAutoJournalPatternResponse> response = controller.delete(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().patternId()).isEqualTo(1L);
            assertThat(response.getBody().message()).isEqualTo("自動仕訳パターンを削除しました");
        }

        @Test
        @DisplayName("対象がない場合は 404 を返す")
        void deleteNotFoundShouldReturnNotFound() {
            when(deleteAutoJournalPatternUseCase.execute(any(DeleteAutoJournalPatternCommand.class)))
                    .thenReturn(DeleteAutoJournalPatternResult.failure("自動仕訳パターンが見つかりません"));

            ResponseEntity<DeleteAutoJournalPatternResponse> response = controller.delete(999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
        }

        @Test
        @DisplayName("その他のエラーの場合は 400 を返す")
        void deleteOtherErrorShouldReturnBadRequest() {
            when(deleteAutoJournalPatternUseCase.execute(any(DeleteAutoJournalPatternCommand.class)))
                    .thenReturn(DeleteAutoJournalPatternResult.failure("削除中にエラーが発生しました"));

            ResponseEntity<DeleteAutoJournalPatternResponse> response = controller.delete(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
        }
    }
}

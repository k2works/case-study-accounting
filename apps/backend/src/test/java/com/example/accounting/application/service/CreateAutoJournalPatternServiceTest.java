package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.CreateAutoJournalPatternCommand;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.CreateAutoJournalPatternResult;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("自動仕訳パターン登録サービス")
class CreateAutoJournalPatternServiceTest {

    @Mock
    private AutoJournalPatternRepository autoJournalPatternRepository;

    private CreateAutoJournalPatternService service;

    @BeforeEach
    void setUp() {
        service = new CreateAutoJournalPatternService(autoJournalPatternRepository);
    }

    @Test
    @DisplayName("有効なコマンドでパターンを登録できる")
    void executeWithValidCommandShouldCreatePattern() {
        CreateAutoJournalPatternCommand command = new CreateAutoJournalPatternCommand(
                "AP001", "売上計上", "sales", "売上データから自動仕訳を生成",
                List.of(new CreateAutoJournalPatternCommand.PatternItemCommand(1, "D", "1100", "amount", "売上"))
        );
        AutoJournalPattern saved = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", "売上データから自動仕訳を生成", true,
                List.of(AutoJournalPatternItem.reconstruct(1, "D", "1100", "amount", "売上"))
        );

        when(autoJournalPatternRepository.existsByCode("AP001")).thenReturn(Try.success(false));
        when(autoJournalPatternRepository.save(any(AutoJournalPattern.class))).thenReturn(Try.success(saved));

        CreateAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isTrue();
        assertThat(result.patternId()).isEqualTo(1L);
        assertThat(result.patternCode()).isEqualTo("AP001");
        assertThat(result.patternName()).isEqualTo("売上計上");
        assertThat(result.errorMessage()).isNull();
        verify(autoJournalPatternRepository).save(any(AutoJournalPattern.class));
    }

    @Test
    @DisplayName("明細なしでもパターンを登録できる")
    void executeWithNoItemsShouldCreatePattern() {
        CreateAutoJournalPatternCommand command = new CreateAutoJournalPatternCommand(
                "AP001", "売上計上", "sales", null, List.of()
        );
        AutoJournalPattern saved = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", null, true, List.of()
        );

        when(autoJournalPatternRepository.existsByCode("AP001")).thenReturn(Try.success(false));
        when(autoJournalPatternRepository.save(any(AutoJournalPattern.class))).thenReturn(Try.success(saved));

        CreateAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isTrue();
        assertThat(result.patternId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("重複パターンコードの場合は失敗する")
    void executeWithDuplicateCodeShouldReturnFailure() {
        CreateAutoJournalPatternCommand command = new CreateAutoJournalPatternCommand(
                "AP001", "売上計上", "sales", null, List.of()
        );

        when(autoJournalPatternRepository.existsByCode("AP001")).thenReturn(Try.success(true));

        CreateAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("パターンコードは既に使用されています");
        verify(autoJournalPatternRepository, never()).save(any(AutoJournalPattern.class));
    }

    @Test
    @DisplayName("不正な貸借区分の場合は失敗する")
    void executeWithInvalidDebitCreditTypeShouldReturnFailure() {
        CreateAutoJournalPatternCommand command = new CreateAutoJournalPatternCommand(
                "AP001", "売上計上", "sales", null,
                List.of(new CreateAutoJournalPatternCommand.PatternItemCommand(1, "X", "1100", "amount", null))
        );

        when(autoJournalPatternRepository.existsByCode("AP001")).thenReturn(Try.success(false));

        CreateAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("貸借区分は 'D' または 'C' である必要があります");
        verify(autoJournalPatternRepository, never()).save(any(AutoJournalPattern.class));
    }

    @Test
    @DisplayName("複数明細のパターンを登録できる")
    void executeWithMultipleItemsShouldCreatePattern() {
        CreateAutoJournalPatternCommand command = new CreateAutoJournalPatternCommand(
                "AP001", "売上計上", "sales", "説明",
                List.of(
                        new CreateAutoJournalPatternCommand.PatternItemCommand(1, "D", "1100", "amount", "売上"),
                        new CreateAutoJournalPatternCommand.PatternItemCommand(2, "C", "4000", "amount", "売上")
                )
        );
        AutoJournalPattern saved = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", "説明", true,
                List.of(
                        AutoJournalPatternItem.reconstruct(1, "D", "1100", "amount", "売上"),
                        AutoJournalPatternItem.reconstruct(2, "C", "4000", "amount", "売上")
                )
        );

        when(autoJournalPatternRepository.existsByCode("AP001")).thenReturn(Try.success(false));
        when(autoJournalPatternRepository.save(any(AutoJournalPattern.class))).thenReturn(Try.success(saved));

        CreateAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isTrue();
        assertThat(result.patternId()).isEqualTo(1L);
        assertThat(result.patternCode()).isEqualTo("AP001");
    }
}

package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.CreateAutoJournalPatternCommand;
import com.example.accounting.application.port.in.command.UpdateAutoJournalPatternCommand;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.UpdateAutoJournalPatternResult;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("自動仕訳パターン更新サービス")
class UpdateAutoJournalPatternServiceTest {

    @Mock
    private AutoJournalPatternRepository autoJournalPatternRepository;

    private UpdateAutoJournalPatternService service;

    @BeforeEach
    void setUp() {
        service = new UpdateAutoJournalPatternService(autoJournalPatternRepository);
    }

    @Test
    @DisplayName("有効なコマンドでパターンを更新できる")
    void executeWithValidCommandShouldUpdatePattern() {
        AutoJournalPattern existing = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", "旧説明", true,
                List.of(AutoJournalPatternItem.reconstruct(1, "D", "1100", "amount", "売上"))
        );
        AutoJournalPattern saved = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L), "AP001", "売上計上（更新）", "sales_v2", "新説明", true,
                List.of(AutoJournalPatternItem.reconstruct(1, "D", "1200", "total", "更新後"))
        );
        UpdateAutoJournalPatternCommand command = new UpdateAutoJournalPatternCommand(
                1L, "売上計上（更新）", "sales_v2", "新説明", true,
                List.of(new CreateAutoJournalPatternCommand.PatternItemCommand(1, "D", "1200", "total", "更新後"))
        );

        when(autoJournalPatternRepository.findById(AutoJournalPatternId.of(1L)))
                .thenReturn(Try.success(Optional.of(existing)));
        when(autoJournalPatternRepository.save(any(AutoJournalPattern.class)))
                .thenReturn(Try.success(saved));

        UpdateAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isTrue();
        assertThat(result.patternId()).isEqualTo(1L);
        assertThat(result.patternCode()).isEqualTo("AP001");
        assertThat(result.patternName()).isEqualTo("売上計上（更新）");
        assertThat(result.message()).isEqualTo("自動仕訳パターンを更新しました");
        assertThat(result.errorMessage()).isNull();
        verify(autoJournalPatternRepository).save(any(AutoJournalPattern.class));
    }

    @Test
    @DisplayName("パターンが見つからない場合は失敗する")
    void executeWithNonExistentPatternShouldReturnFailure() {
        UpdateAutoJournalPatternCommand command = new UpdateAutoJournalPatternCommand(
                999L, "売上計上", "sales", null, true, List.of()
        );

        when(autoJournalPatternRepository.findById(AutoJournalPatternId.of(999L)))
                .thenReturn(Try.success(Optional.empty()));

        UpdateAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("自動仕訳パターンが見つかりません");
        verify(autoJournalPatternRepository, never()).save(any(AutoJournalPattern.class));
    }

    @Test
    @DisplayName("無効化して更新できる")
    void executeWithDeactivationShouldUpdatePattern() {
        AutoJournalPattern existing = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", null, true, List.of()
        );
        AutoJournalPattern saved = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", null, false, List.of()
        );
        UpdateAutoJournalPatternCommand command = new UpdateAutoJournalPatternCommand(
                1L, "売上計上", "sales", null, false, List.of()
        );

        when(autoJournalPatternRepository.findById(AutoJournalPatternId.of(1L)))
                .thenReturn(Try.success(Optional.of(existing)));
        when(autoJournalPatternRepository.save(any(AutoJournalPattern.class)))
                .thenReturn(Try.success(saved));

        UpdateAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("不正な貸借区分の場合は失敗する")
    void executeWithInvalidDebitCreditTypeShouldReturnFailure() {
        AutoJournalPattern existing = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", null, true, List.of()
        );
        UpdateAutoJournalPatternCommand command = new UpdateAutoJournalPatternCommand(
                1L, "売上計上", "sales", null, true,
                List.of(new CreateAutoJournalPatternCommand.PatternItemCommand(1, "X", "1100", "amount", null))
        );

        when(autoJournalPatternRepository.findById(AutoJournalPatternId.of(1L)))
                .thenReturn(Try.success(Optional.of(existing)));

        UpdateAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("貸借区分は 'D' または 'C' である必要があります");
        verify(autoJournalPatternRepository, never()).save(any(AutoJournalPattern.class));
    }
}

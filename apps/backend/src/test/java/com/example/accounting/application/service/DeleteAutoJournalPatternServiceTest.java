package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.DeleteAutoJournalPatternCommand;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.DeleteAutoJournalPatternResult;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("自動仕訳パターン削除サービス")
class DeleteAutoJournalPatternServiceTest {

    @Mock
    private AutoJournalPatternRepository autoJournalPatternRepository;

    private DeleteAutoJournalPatternService service;

    @BeforeEach
    void setUp() {
        service = new DeleteAutoJournalPatternService(autoJournalPatternRepository);
    }

    @Test
    @DisplayName("存在するパターンを削除できる")
    void executeWithExistingPatternShouldDelete() {
        AutoJournalPattern existing = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", null, true, List.of()
        );
        DeleteAutoJournalPatternCommand command = new DeleteAutoJournalPatternCommand(1L);

        when(autoJournalPatternRepository.findById(AutoJournalPatternId.of(1L)))
                .thenReturn(Try.success(Optional.of(existing)));
        when(autoJournalPatternRepository.deleteById(AutoJournalPatternId.of(1L)))
                .thenReturn(Try.run(() -> { }));

        DeleteAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isTrue();
        assertThat(result.patternId()).isEqualTo(1L);
        assertThat(result.message()).isEqualTo("自動仕訳パターンを削除しました");
        assertThat(result.errorMessage()).isNull();
        verify(autoJournalPatternRepository).deleteById(AutoJournalPatternId.of(1L));
    }

    @Test
    @DisplayName("存在しないパターンの削除は失敗する")
    void executeWithNonExistentPatternShouldReturnFailure() {
        DeleteAutoJournalPatternCommand command = new DeleteAutoJournalPatternCommand(999L);

        when(autoJournalPatternRepository.findById(AutoJournalPatternId.of(999L)))
                .thenReturn(Try.success(Optional.empty()));

        DeleteAutoJournalPatternResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("自動仕訳パターンが見つかりません");
        verify(autoJournalPatternRepository, never()).deleteById(AutoJournalPatternId.of(999L));
    }
}

package com.example.accounting.infrastructure.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UpdateJournalEntryRequest")
class UpdateJournalEntryRequestTest {

    @Test
    @DisplayName("有効な値でインスタンスを生成できる")
    void shouldCreateWithValidValues() {
        List<UpdateJournalEntryRequest.JournalEntryLineRequest> lines = List.of(
                new UpdateJournalEntryRequest.JournalEntryLineRequest(
                        1,
                        100,
                        new BigDecimal("1000"),
                        null
                )
        );

        UpdateJournalEntryRequest request = new UpdateJournalEntryRequest(
                LocalDate.of(2024, 2, 1),
                "摘要",
                lines,
                1
        );

        assertThat(request.journalDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(request.description()).isEqualTo("摘要");
        assertThat(request.lines()).hasSize(1);
        assertThat(request.version()).isEqualTo(1);
    }

    @Test
    @DisplayName("lines が null の場合は空リストになる")
    void shouldUseEmptyListWhenLinesNull() {
        UpdateJournalEntryRequest request = new UpdateJournalEntryRequest(
                LocalDate.of(2024, 2, 1),
                "摘要",
                null,
                1
        );

        assertThat(request.lines()).isEmpty();
    }

    @Test
    @DisplayName("lines は防御的コピーされる")
    void shouldDefensivelyCopyLines() {
        List<UpdateJournalEntryRequest.JournalEntryLineRequest> lines = new ArrayList<>();
        lines.add(new UpdateJournalEntryRequest.JournalEntryLineRequest(
                1,
                100,
                new BigDecimal("1000"),
                null
        ));

        UpdateJournalEntryRequest request = new UpdateJournalEntryRequest(
                LocalDate.of(2024, 2, 1),
                "摘要",
                lines,
                1
        );

        lines.add(new UpdateJournalEntryRequest.JournalEntryLineRequest(
                2,
                200,
                null,
                new BigDecimal("1000")
        ));

        assertThat(request.lines()).hasSize(1);
        UpdateJournalEntryRequest.JournalEntryLineRequest firstLine = lines.get(0);
        assertThatThrownBy(() -> request.lines().add(firstLine))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}

package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetSubsidiaryLedgerUseCase;
import com.example.accounting.application.port.in.query.GetSubsidiaryLedgerQuery;
import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult;
import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult.SubsidiaryLedgerEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("補助元帳コントローラ")
class SubsidiaryLedgerControllerTest {

    @Mock
    private GetSubsidiaryLedgerUseCase getSubsidiaryLedgerUseCase;

    private SubsidiaryLedgerController controller;

    @BeforeEach
    void setUp() {
        controller = new SubsidiaryLedgerController(getSubsidiaryLedgerUseCase);
    }

    @Test
    @DisplayName("補助元帳を取得できる")
    void shouldGetSubsidiaryLedger() {
        GetSubsidiaryLedgerResult result = new GetSubsidiaryLedgerResult(
                List.of(new SubsidiaryLedgerEntry(
                        10,
                        LocalDate.of(2024, 1, 10),
                        "売上計上",
                        new BigDecimal("500"),
                        BigDecimal.ZERO,
                        new BigDecimal("1500")
                )),
                "1101",
                "現金",
                "S001",
                new BigDecimal("1000"),
                new BigDecimal("500"),
                BigDecimal.ZERO,
                new BigDecimal("1500"),
                0,
                20,
                1L,
                1
        );
        when(getSubsidiaryLedgerUseCase.execute(any(GetSubsidiaryLedgerQuery.class))).thenReturn(result);

        ResponseEntity<GetSubsidiaryLedgerResult> response = controller.getSubsidiaryLedger(
                "1101",
                "S001",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                0,
                20
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        ArgumentCaptor<GetSubsidiaryLedgerQuery> captor = ArgumentCaptor.forClass(GetSubsidiaryLedgerQuery.class);
        verify(getSubsidiaryLedgerUseCase).execute(captor.capture());
        GetSubsidiaryLedgerQuery query = captor.getValue();
        assertThat(query.accountCode()).isEqualTo("1101");
        assertThat(query.subAccountCode()).isEqualTo("S001");
        assertThat(query.dateFrom()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(query.dateTo()).isEqualTo(LocalDate.of(2024, 1, 31));
        assertThat(query.page()).isZero();
        assertThat(query.size()).isEqualTo(20);
    }
}

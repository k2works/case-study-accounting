package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetGeneralLedgerUseCase;
import com.example.accounting.application.port.in.query.GetGeneralLedgerQuery;
import com.example.accounting.application.port.out.GetGeneralLedgerResult;
import com.example.accounting.application.port.out.GetGeneralLedgerResult.GeneralLedgerEntry;
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
@DisplayName("総勘定元帳コントローラ")
class GeneralLedgerControllerTest {

    @Mock
    private GetGeneralLedgerUseCase getGeneralLedgerUseCase;

    private GeneralLedgerController controller;

    @BeforeEach
    void setUp() {
        controller = new GeneralLedgerController(getGeneralLedgerUseCase);
    }

    @Test
    @DisplayName("総勘定元帳を取得できる")
    void shouldGetGeneralLedger() {
        GetGeneralLedgerResult result = new GetGeneralLedgerResult(
                List.of(new GeneralLedgerEntry(
                        10,
                        LocalDate.of(2024, 1, 10),
                        "売上計上",
                        new BigDecimal("500"),
                        BigDecimal.ZERO,
                        new BigDecimal("1500")
                )),
                1,
                "1101",
                "現金",
                new BigDecimal("1000"),
                new BigDecimal("500"),
                BigDecimal.ZERO,
                new BigDecimal("1500"),
                0,
                20,
                1L,
                1
        );
        when(getGeneralLedgerUseCase.execute(any(GetGeneralLedgerQuery.class))).thenReturn(result);

        ResponseEntity<GetGeneralLedgerResult> response = controller.getGeneralLedger(
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                0,
                20
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        ArgumentCaptor<GetGeneralLedgerQuery> captor = ArgumentCaptor.forClass(GetGeneralLedgerQuery.class);
        verify(getGeneralLedgerUseCase).execute(captor.capture());
        GetGeneralLedgerQuery query = captor.getValue();
        assertThat(query.accountId()).isEqualTo(1);
        assertThat(query.dateFrom()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(query.dateTo()).isEqualTo(LocalDate.of(2024, 1, 31));
        assertThat(query.page()).isZero();
        assertThat(query.size()).isEqualTo(20);
    }
}

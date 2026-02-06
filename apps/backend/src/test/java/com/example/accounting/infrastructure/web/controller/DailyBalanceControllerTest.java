package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetDailyBalanceUseCase;
import com.example.accounting.application.port.in.query.GetDailyBalanceQuery;
import com.example.accounting.application.port.out.GetDailyBalanceResult;
import com.example.accounting.application.port.out.GetDailyBalanceResult.DailyBalanceEntry;
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
@DisplayName("日次残高照会コントローラ")
class DailyBalanceControllerTest {

    @Mock
    private GetDailyBalanceUseCase getDailyBalanceUseCase;

    private DailyBalanceController controller;

    @BeforeEach
    void setUp() {
        controller = new DailyBalanceController(getDailyBalanceUseCase);
    }

    @Test
    @DisplayName("日次残高を取得できる")
    void shouldGetDailyBalance() {
        GetDailyBalanceResult result = new GetDailyBalanceResult(
                1,
                "1101",
                "現金",
                new BigDecimal("1000"),
                new BigDecimal("500"),
                BigDecimal.ZERO,
                new BigDecimal("1500"),
                List.of(new DailyBalanceEntry(
                        LocalDate.of(2024, 1, 10),
                        new BigDecimal("500"),
                        BigDecimal.ZERO,
                        new BigDecimal("1500"),
                        1L
                ))
        );
        when(getDailyBalanceUseCase.execute(any(GetDailyBalanceQuery.class))).thenReturn(result);

        ResponseEntity<GetDailyBalanceResult> response = controller.getDailyBalance(
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        ArgumentCaptor<GetDailyBalanceQuery> captor = ArgumentCaptor.forClass(GetDailyBalanceQuery.class);
        verify(getDailyBalanceUseCase).execute(captor.capture());
        GetDailyBalanceQuery query = captor.getValue();
        assertThat(query.accountId()).isEqualTo(1);
        assertThat(query.dateFrom()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(query.dateTo()).isEqualTo(LocalDate.of(2024, 1, 31));
    }
}

package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.query.GetTrialBalanceQuery;
import com.example.accounting.application.port.in.query.GetTrialBalanceUseCase;
import com.example.accounting.application.port.out.GetTrialBalanceResult;
import com.example.accounting.application.port.out.GetTrialBalanceResult.TrialBalanceEntry;
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
@DisplayName("残高試算表コントローラ")
class TrialBalanceControllerTest {

    @Mock
    private GetTrialBalanceUseCase getTrialBalanceUseCase;

    private TrialBalanceController controller;

    @BeforeEach
    void setUp() {
        controller = new TrialBalanceController(getTrialBalanceUseCase);
    }

    @Test
    @DisplayName("指定日の残高試算表を取得できる")
    void shouldGetTrialBalanceWithDate() {
        LocalDate date = LocalDate.of(2024, 6, 30);
        GetTrialBalanceResult result = new GetTrialBalanceResult(
                date,
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                true,
                BigDecimal.ZERO,
                List.of(new TrialBalanceEntry(
                        "1000", "現金", "BS", "ASSET",
                        new BigDecimal("100000"), BigDecimal.ZERO
                )),
                List.of()
        );
        when(getTrialBalanceUseCase.execute(any(GetTrialBalanceQuery.class))).thenReturn(result);

        ResponseEntity<GetTrialBalanceResult> response = controller.getTrialBalance(date);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        ArgumentCaptor<GetTrialBalanceQuery> captor = ArgumentCaptor.forClass(GetTrialBalanceQuery.class);
        verify(getTrialBalanceUseCase).execute(captor.capture());
        assertThat(captor.getValue().date()).isEqualTo(date);
    }

    @Test
    @DisplayName("date が null で全期間の試算表を取得できる")
    void shouldGetTrialBalanceWithoutDate() {
        GetTrialBalanceResult result = new GetTrialBalanceResult(
                null,
                BigDecimal.ZERO, BigDecimal.ZERO,
                true, BigDecimal.ZERO,
                List.of(), List.of()
        );
        when(getTrialBalanceUseCase.execute(any(GetTrialBalanceQuery.class))).thenReturn(result);

        ResponseEntity<GetTrialBalanceResult> response = controller.getTrialBalance(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<GetTrialBalanceQuery> captor = ArgumentCaptor.forClass(GetTrialBalanceQuery.class);
        verify(getTrialBalanceUseCase).execute(captor.capture());
        assertThat(captor.getValue().date()).isNull();
    }
}

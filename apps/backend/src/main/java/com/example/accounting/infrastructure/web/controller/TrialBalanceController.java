package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.query.GetTrialBalanceQuery;
import com.example.accounting.application.port.in.query.GetTrialBalanceUseCase;
import com.example.accounting.application.port.out.GetTrialBalanceResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/trial-balance")
@Tag(name = "残高試算表", description = "残高試算表に関する API")
public class TrialBalanceController {
    private final GetTrialBalanceUseCase getTrialBalanceUseCase;

    public TrialBalanceController(GetTrialBalanceUseCase getTrialBalanceUseCase) {
        this.getTrialBalanceUseCase = getTrialBalanceUseCase;
    }

    @Operation(summary = "残高試算表照会", description = "指定した基準日の残高試算表を照会します")
    @ApiResponse(responseCode = "200", description = "取得成功")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetTrialBalanceResult> getTrialBalance(
            @RequestParam(required = false) LocalDate date
    ) {
        GetTrialBalanceQuery query = new GetTrialBalanceQuery(date);
        GetTrialBalanceResult result = getTrialBalanceUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}

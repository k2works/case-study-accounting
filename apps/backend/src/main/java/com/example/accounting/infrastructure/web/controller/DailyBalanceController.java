package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetDailyBalanceUseCase;
import com.example.accounting.application.port.in.query.GetDailyBalanceQuery;
import com.example.accounting.application.port.out.GetDailyBalanceResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日次残高照会コントローラ
 */
@RestController
@RequestMapping("/api/daily-balance")
@Tag(name = "日次残高", description = "日次残高に関する API")
public class DailyBalanceController {

    private final GetDailyBalanceUseCase getDailyBalanceUseCase;

    public DailyBalanceController(GetDailyBalanceUseCase getDailyBalanceUseCase) {
        this.getDailyBalanceUseCase = getDailyBalanceUseCase;
    }

    /**
     * 日次残高照会
     */
    @Operation(
            summary = "日次残高照会",
            description = "経理担当者以上が日次残高を照会します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "取得成功"
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetDailyBalanceResult> getDailyBalance(
            @RequestParam Integer accountId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        GetDailyBalanceQuery query = new GetDailyBalanceQuery(accountId, dateFrom, dateTo);
        GetDailyBalanceResult result = getDailyBalanceUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}

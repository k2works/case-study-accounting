package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.query.GetMonthlyBalanceQuery;
import com.example.accounting.application.port.out.GetMonthlyBalanceResult;

public interface GetMonthlyBalanceUseCase {
    GetMonthlyBalanceResult execute(GetMonthlyBalanceQuery query);
}

package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.query.GetDailyBalanceQuery;
import com.example.accounting.application.port.out.GetDailyBalanceResult;

public interface GetDailyBalanceUseCase {
    GetDailyBalanceResult execute(GetDailyBalanceQuery query);
}

package com.example.accounting.application.port.in.query;

import com.example.accounting.application.port.out.GetTrialBalanceResult;

/**
 * 残高試算表照会ユースケース
 */
public interface GetTrialBalanceUseCase {
    GetTrialBalanceResult execute(GetTrialBalanceQuery query);
}

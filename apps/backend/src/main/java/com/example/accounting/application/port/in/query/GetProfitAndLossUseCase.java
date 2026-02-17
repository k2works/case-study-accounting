package com.example.accounting.application.port.in.query;

import com.example.accounting.application.port.out.GetProfitAndLossResult;

/**
 * 損益計算書照会ユースケース
 */
public interface GetProfitAndLossUseCase {
    GetProfitAndLossResult execute(GetProfitAndLossQuery query);
}

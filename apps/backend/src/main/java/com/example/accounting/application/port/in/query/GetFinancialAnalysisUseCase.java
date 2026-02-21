package com.example.accounting.application.port.in.query;

import com.example.accounting.application.port.out.GetFinancialAnalysisResult;

/**
 * 財務分析照会ユースケース
 */
public interface GetFinancialAnalysisUseCase {
    GetFinancialAnalysisResult execute(GetFinancialAnalysisQuery query);
}

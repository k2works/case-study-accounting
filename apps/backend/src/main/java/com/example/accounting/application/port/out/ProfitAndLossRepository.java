package com.example.accounting.application.port.out;

import com.example.accounting.infrastructure.persistence.entity.ProfitAndLossEntity;
import io.vavr.control.Try;

import java.time.LocalDate;
import java.util.List;

/**
 * 損益計算書リポジトリ（Output Port）
 */
public interface ProfitAndLossRepository {
    /**
     * 指定期間の損益計算書データを取得します。
     *
     * @param dateFrom 集計開始日
     * @param dateTo   集計終了日
     * @return 取得結果。成功時は損益計算書データ一覧
     */
    Try<List<ProfitAndLossEntity>> findProfitAndLoss(LocalDate dateFrom, LocalDate dateTo);
}

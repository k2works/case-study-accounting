package com.example.accounting.application.port.out;

import com.example.accounting.infrastructure.persistence.entity.TrialBalanceEntity;
import java.time.LocalDate;
import java.util.List;

/**
 * 試算表リポジトリ（Output Port）
 */
public interface TrialBalanceRepository {
    List<TrialBalanceEntity> findTrialBalance(LocalDate date);
}

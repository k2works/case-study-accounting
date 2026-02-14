package com.example.accounting.application.port.out;

import com.example.accounting.infrastructure.persistence.entity.BalanceSheetEntity;
import java.time.LocalDate;
import java.util.List;

/**
 * 貸借対照表リポジトリ（Output Port）
 */
public interface BalanceSheetRepository {
    List<BalanceSheetEntity> findBalanceSheet(LocalDate date);
}

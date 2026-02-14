package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult.SubsidiaryLedgerEntry;
import com.example.accounting.application.port.out.SubsidiaryLedgerRepository;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineWithHeaderEntity;
import com.example.accounting.infrastructure.persistence.mapper.SubsidiaryLedgerMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 補助元帳リポジトリ実装
 */
@Repository
public class SubsidiaryLedgerRepositoryImpl implements SubsidiaryLedgerRepository {

    private final SubsidiaryLedgerMapper subsidiaryLedgerMapper;

    public SubsidiaryLedgerRepositoryImpl(SubsidiaryLedgerMapper subsidiaryLedgerMapper) {
        this.subsidiaryLedgerMapper = subsidiaryLedgerMapper;
    }

    @Override
    public List<SubsidiaryLedgerEntry> findPostedLinesByAccountAndSubAccountAndPeriod(String accountCode,
                                                                                      String subAccountCode,
                                                                                      LocalDate dateFrom,
                                                                                      LocalDate dateTo,
                                                                                      int offset,
                                                                                      int limit) {
        return subsidiaryLedgerMapper.findPostedLinesByAccountAndSubAccountAndPeriod(
                        accountCode, subAccountCode, dateFrom, dateTo, offset, limit)
                .stream()
                .map(this::toSubsidiaryLedgerEntry)
                .toList();
    }

    @Override
    public long countPostedLinesByAccountAndSubAccountAndPeriod(String accountCode,
                                                                String subAccountCode,
                                                                LocalDate dateFrom,
                                                                LocalDate dateTo) {
        return subsidiaryLedgerMapper.countPostedLinesByAccountAndSubAccountAndPeriod(
                accountCode, subAccountCode, dateFrom, dateTo);
    }

    @Override
    public BigDecimal calculateBalanceBeforeDateByAccountAndSubAccount(String accountCode,
                                                                       String subAccountCode,
                                                                       LocalDate date) {
        return subsidiaryLedgerMapper.calculateBalanceBeforeDateByAccountAndSubAccount(
                accountCode, subAccountCode, date);
    }

    private SubsidiaryLedgerEntry toSubsidiaryLedgerEntry(JournalEntryLineWithHeaderEntity entity) {
        return new SubsidiaryLedgerEntry(
                entity.getJournalEntryId(),
                entity.getJournalDate(),
                entity.getDescription(),
                entity.getDebitAmount(),
                entity.getCreditAmount(),
                null
        );
    }
}

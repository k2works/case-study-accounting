package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.GetGeneralLedgerResult.GeneralLedgerEntry;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.application.port.out.JournalEntrySearchCriteria;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.shared.OptimisticLockException;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryEntity;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineEntity;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineWithHeaderEntity;
import com.example.accounting.infrastructure.persistence.mapper.JournalEntryMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 仕訳リポジトリ実装
 */
@Repository
public class JournalEntryRepositoryImpl implements JournalEntryRepository {

    private final JournalEntryMapper journalEntryMapper;

    public JournalEntryRepositoryImpl(JournalEntryMapper journalEntryMapper) {
        this.journalEntryMapper = journalEntryMapper;
    }

    @Override
    public JournalEntry save(JournalEntry journalEntry) {
        JournalEntryEntity entity = JournalEntryEntity.fromDomain(journalEntry);

        if (entity.getId() != null && journalEntryMapper.findById(entity.getId()).isPresent()) {
            int updatedCount = journalEntryMapper.update(entity);
            if (updatedCount == 0) {
                throw new OptimisticLockException("仕訳の更新に失敗しました。再読み込みしてください。");
            }
            journalEntryMapper.deleteLines(entity.getId());
        } else {
            journalEntryMapper.insert(entity);
        }

        List<JournalEntryLineEntity> lineEntities = journalEntry.getLines().stream()
                .map(line -> JournalEntryLineEntity.fromDomain(line, entity.getId()))
                .toList();
        if (!lineEntities.isEmpty()) {
            journalEntryMapper.insertLines(lineEntities);
        }

        return journalEntryMapper.findById(entity.getId())
                .map(JournalEntryEntity::toDomain)
                .orElseThrow(() -> new IllegalStateException("保存後の仕訳取得に失敗しました"));
    }

    @Override
    public Optional<JournalEntry> findById(JournalEntryId id) {
        return journalEntryMapper.findById(id.value())
                .map(JournalEntryEntity::toDomain);
    }

    @Override
    public List<JournalEntry> findAll() {
        return journalEntryMapper.findAll().stream()
                .map(JournalEntryEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(JournalEntryId id) {
        journalEntryMapper.deleteById(id.value());
    }

    @Override
    public List<JournalEntry> findByConditions(List<String> statuses, LocalDate dateFrom, LocalDate dateTo, int offset, int limit) {
        return journalEntryMapper.findByConditions(statuses, dateFrom, dateTo, offset, limit)
                .stream()
                .map(JournalEntryEntity::toDomain)
                .toList();
    }

    @Override
    public long countByConditions(List<String> statuses, LocalDate dateFrom, LocalDate dateTo) {
        return journalEntryMapper.countByConditions(statuses, dateFrom, dateTo);
    }

    @Override
    public List<JournalEntry> searchByConditions(JournalEntrySearchCriteria criteria) {
        return journalEntryMapper.searchByConditions(criteria)
                .stream()
                .map(JournalEntryEntity::toDomain)
                .toList();
    }

    @Override
    public long countBySearchConditions(JournalEntrySearchCriteria criteria) {
        return journalEntryMapper.countBySearchConditions(criteria);
    }

    @Override
    public List<GeneralLedgerEntry> findPostedLinesByAccountAndPeriod(Integer accountId, LocalDate dateFrom,
                                                                      LocalDate dateTo, int offset, int limit) {
        return journalEntryMapper.findPostedLinesByAccountAndPeriod(accountId, dateFrom, dateTo, offset, limit)
                .stream()
                .map(this::toGeneralLedgerEntry)
                .toList();
    }

    @Override
    public long countPostedLinesByAccountAndPeriod(Integer accountId, LocalDate dateFrom, LocalDate dateTo) {
        return journalEntryMapper.countPostedLinesByAccountAndPeriod(accountId, dateFrom, dateTo);
    }

    @Override
    public BigDecimal calculateBalanceBeforeDate(Integer accountId, LocalDate date) {
        return journalEntryMapper.calculateBalanceBeforeDate(accountId, date);
    }

    private GeneralLedgerEntry toGeneralLedgerEntry(JournalEntryLineWithHeaderEntity entity) {
        String description = entity.getLineDescription();
        if (description == null || description.isBlank()) {
            description = entity.getDescription();
        }
        return new GeneralLedgerEntry(
                entity.getJournalEntryId(),
                entity.getJournalDate(),
                description,
                entity.getDebitAmount(),
                entity.getCreditAmount(),
                null
        );
    }
}

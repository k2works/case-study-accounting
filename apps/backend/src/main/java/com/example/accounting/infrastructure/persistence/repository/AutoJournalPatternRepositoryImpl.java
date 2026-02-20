package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.infrastructure.persistence.entity.AutoJournalPatternEntity;
import com.example.accounting.infrastructure.persistence.entity.AutoJournalPatternItemEntity;
import com.example.accounting.infrastructure.persistence.mapper.AutoJournalPatternMapper;
import io.vavr.control.Try;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AutoJournalPatternRepositoryImpl implements AutoJournalPatternRepository {

    private final AutoJournalPatternMapper autoJournalPatternMapper;

    public AutoJournalPatternRepositoryImpl(AutoJournalPatternMapper autoJournalPatternMapper) {
        this.autoJournalPatternMapper = autoJournalPatternMapper;
    }

    @Override
    public Try<AutoJournalPattern> save(AutoJournalPattern pattern) {
        return Try.of(() -> {
            AutoJournalPatternEntity entity = AutoJournalPatternEntity.fromDomain(pattern);

            if (entity.getId() != null && autoJournalPatternMapper.findById(entity.getId()).isPresent()) {
                autoJournalPatternMapper.update(entity);
                autoJournalPatternMapper.deleteItems(entity.getId());
            } else {
                autoJournalPatternMapper.insert(entity);
            }

            List<AutoJournalPatternItemEntity> itemEntities = pattern.getItems().stream()
                    .map(item -> AutoJournalPatternItemEntity.fromDomain(item, entity.getId()))
                    .toList();
            if (!itemEntities.isEmpty()) {
                autoJournalPatternMapper.insertItems(itemEntities);
            }

            return autoJournalPatternMapper.findById(entity.getId())
                    .map(AutoJournalPatternEntity::toDomain)
                    .orElseThrow(() -> new IllegalStateException("保存後の自動仕訳パターン取得に失敗しました"));
        });
    }

    @Override
    public Try<Optional<AutoJournalPattern>> findById(AutoJournalPatternId id) {
        return Try.of(() -> autoJournalPatternMapper.findById(id.value())
                .map(AutoJournalPatternEntity::toDomain));
    }

    @Override
    public Try<List<AutoJournalPattern>> findAll() {
        return Try.of(() -> autoJournalPatternMapper.findAll().stream()
                .map(AutoJournalPatternEntity::toDomain)
                .toList());
    }

    @Override
    public Try<Void> deleteById(AutoJournalPatternId id) {
        return Try.run(() -> autoJournalPatternMapper.deleteById(id.value()));
    }

    @Override
    public Try<Boolean> existsByCode(String patternCode) {
        return Try.of(() -> autoJournalPatternMapper.existsByCode(patternCode));
    }
}

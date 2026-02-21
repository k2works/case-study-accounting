package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.AutoJournalLogRepository;
import com.example.accounting.domain.model.auto_journal.AutoJournalLog;
import com.example.accounting.infrastructure.persistence.entity.AutoJournalLogEntity;
import com.example.accounting.infrastructure.persistence.mapper.AutoJournalLogMapper;
import io.vavr.control.Try;
import org.springframework.stereotype.Repository;

@Repository
public class AutoJournalLogRepositoryImpl implements AutoJournalLogRepository {

    private final AutoJournalLogMapper mapper;

    public AutoJournalLogRepositoryImpl(AutoJournalLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Try<AutoJournalLog> save(AutoJournalLog log) {
        return Try.of(() -> {
            AutoJournalLogEntity entity = AutoJournalLogEntity.fromDomain(log);
            mapper.insert(entity);
            return entity.toDomain();
        });
    }
}

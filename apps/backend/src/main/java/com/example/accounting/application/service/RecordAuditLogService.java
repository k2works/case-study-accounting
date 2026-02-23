package com.example.accounting.application.service;

import com.example.accounting.application.port.in.RecordAuditLogUseCase;
import com.example.accounting.application.port.out.AuditLogRepository;
import com.example.accounting.domain.model.audit.AuditLog;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecordAuditLogService implements RecordAuditLogUseCase {

    private final AuditLogRepository auditLogRepository;

    @Override
    public Either<String, Void> execute(RecordAuditLogCommand command) {
        AuditLog auditLog = AuditLog.create(
                command.userId(),
                command.actionType(),
                command.entityType(),
                command.entityId(),
                command.description(),
                command.ipAddress()
        );

        var result = auditLogRepository.save(auditLog);
        if (result.isFailure()) {
            return Either.left("監査ログの保存に失敗しました: " + result.getCause().getMessage());
        }
        return Either.right(null);
    }
}

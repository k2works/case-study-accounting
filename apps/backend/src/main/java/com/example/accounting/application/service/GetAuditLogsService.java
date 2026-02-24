package com.example.accounting.application.service;

import com.example.accounting.application.port.in.GetAuditLogsUseCase;
import com.example.accounting.application.port.out.AuditLogRepository;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAuditLogsService implements GetAuditLogsUseCase {

    private final AuditLogRepository auditLogRepository;

    @Override
    public Either<String, GetAuditLogsResult> execute(GetAuditLogsQuery query) {
        var logsResult = auditLogRepository.search(
                query.userId(),
                query.actionType(),
                query.dateFrom(),
                query.dateTo(),
                query.page() * query.size(),
                query.size()
        );
        if (logsResult.isFailure()) {
            return Either.left("監査ログの取得に失敗しました: " + logsResult.getCause().getMessage());
        }

        var countResult = auditLogRepository.countByConditions(
                query.userId(),
                query.actionType(),
                query.dateFrom(),
                query.dateTo()
        );
        if (countResult.isFailure()) {
            return Either.left("監査ログの件数取得に失敗しました: " + countResult.getCause().getMessage());
        }

        long totalCount = countResult.get();
        int totalPages = (int) Math.ceil((double) totalCount / query.size());

        return Either.right(new GetAuditLogsResult(
                logsResult.get(),
                totalCount,
                totalPages,
                query.page()
        ));
    }
}

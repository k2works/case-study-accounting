package com.example.accounting.application.service;

import com.example.accounting.application.port.in.GenerateAutoJournalUseCase;
import com.example.accounting.application.port.in.command.GenerateAutoJournalCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AutoJournalLogRepository;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.GenerateAutoJournalResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.auto_journal.AmountFormulaEvaluator;
import com.example.accounting.domain.model.auto_journal.AutoJournalLog;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.Money;
import com.example.accounting.domain.model.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
@SuppressWarnings("PMD.AvoidThrowStatement")
public class GenerateAutoJournalService implements GenerateAutoJournalUseCase {

    private final AutoJournalPatternRepository patternRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AutoJournalLogRepository logRepository;

    public GenerateAutoJournalService(AutoJournalPatternRepository patternRepository,
                                      AccountRepository accountRepository,
                                      JournalEntryRepository journalEntryRepository,
                                      AutoJournalLogRepository logRepository) {
        this.patternRepository = patternRepository;
        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.logRepository = logRepository;
    }

    @Override
    public GenerateAutoJournalResult execute(GenerateAutoJournalCommand command) {
        Long patternId = command.patternId();
        try {
            AutoJournalPattern pattern = findActivePattern(patternId);
            JournalEntry entry = buildJournalEntry(command, pattern);
            entry.validateForSave();
            JournalEntry saved = saveJournalEntry(entry);
            logRepository.save(AutoJournalLog.createSuccess(patternId, "仕訳ID: " + saved.getId().value()));
            return buildSuccessResult(saved);
        } catch (IllegalArgumentException e) {
            logRepository.save(AutoJournalLog.createFailure(patternId, e.getMessage(), null));
            return GenerateAutoJournalResult.failure(e.getMessage());
        }
    }

    private AutoJournalPattern findActivePattern(Long patternId) {
        Optional<AutoJournalPattern> patternOpt = patternRepository
                .findById(AutoJournalPatternId.of(patternId))
                .getOrElseThrow(ex -> new RuntimeException("データアクセスエラー", ex));

        if (patternOpt.isEmpty()) {
            throw new IllegalArgumentException("指定されたパターンが見つかりません");
        }
        AutoJournalPattern pattern = patternOpt.get();
        if (!Boolean.TRUE.equals(pattern.getIsActive())) {
            throw new IllegalArgumentException("指定されたパターンは無効です");
        }
        if (pattern.getItems().isEmpty()) {
            throw new IllegalArgumentException("パターンに明細が定義されていません");
        }
        return pattern;
    }

    private JournalEntry buildJournalEntry(GenerateAutoJournalCommand command, AutoJournalPattern pattern) {
        String description = command.description() != null
                ? command.description()
                : pattern.getPatternName();

        JournalEntry entry = JournalEntry.create(
                command.journalDate(), description, UserId.of(command.createdByUserId()), 0);

        for (AutoJournalPatternItem item : pattern.getItems()) {
            entry = entry.addLine(buildLine(item, command));
        }
        return entry;
    }

    private JournalEntryLine buildLine(AutoJournalPatternItem item, GenerateAutoJournalCommand command) {
        AccountId accountId = resolveAccount(item.getAccountCode());
        BigDecimal amount = AmountFormulaEvaluator.evaluate(item.getAmountFormula(), command.amounts());
        Money debitAmount = "D".equals(item.getDebitCreditType()) ? Money.of(amount) : null;
        Money creditAmount = "C".equals(item.getDebitCreditType()) ? Money.of(amount) : null;
        return JournalEntryLine.of(item.getLineNumber(), accountId, debitAmount, creditAmount);
    }

    private AccountId resolveAccount(String accountCode) {
        Optional<Account> accountOpt = accountRepository
                .findByCode(accountCode)
                .getOrElseThrow(ex -> new RuntimeException("データアクセスエラー", ex));
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("勘定科目コード '" + accountCode + "' が見つかりません");
        }
        return accountOpt.get().getId();
    }

    private JournalEntry saveJournalEntry(JournalEntry entry) {
        return journalEntryRepository.save(entry)
                .getOrElseThrow(ex -> new RuntimeException("データアクセスエラー", ex));
    }

    private GenerateAutoJournalResult buildSuccessResult(JournalEntry saved) {
        return GenerateAutoJournalResult.success(
                saved.getId().value(), saved.getJournalDate(),
                saved.getDescription(), saved.getStatus().name());
    }
}

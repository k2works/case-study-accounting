package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import com.example.accounting.domain.model.journal.Money;
import com.example.accounting.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JournalEntryEntity 変換")
class JournalEntryEntityTest {

    private static final LocalDate JOURNAL_DATE = LocalDate.of(2024, 1, 31);
    private static final UserId CREATED_BY = UserId.of("user-1");

    @Nested
    @DisplayName("fromDomain")
    class FromDomain {

        @Test
        @DisplayName("ID ありのドメインモデルからエンティティに変換できる")
        void shouldConvertFromDomainWithId() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            JournalEntryLine debitLine = JournalEntryLine.of(
                    1, AccountId.of(100), Money.of(new BigDecimal("1000")), null);
            JournalEntryLine creditLine = JournalEntryLine.of(
                    2, AccountId.of(200), null, Money.of(new BigDecimal("1000")));

            JournalEntry entry = JournalEntry.reconstruct(
                    JournalEntryId.of(1),
                    JOURNAL_DATE,
                    "売上計上",
                    JournalEntryStatus.DRAFT,
                    1,
                    List.of(debitLine, creditLine),
                    CREATED_BY,
                    UserId.of("approver-1"),
                    now,
                    now,
                    now
            );

            JournalEntryEntity entity = JournalEntryEntity.fromDomain(entry);

            assertThat(entity.getId()).isEqualTo(1);
            assertThat(entity.getJournalDate()).isEqualTo(JOURNAL_DATE);
            assertThat(entity.getDescription()).isEqualTo("売上計上");
            assertThat(entity.getStatus()).isEqualTo("DRAFT");
            assertThat(entity.getVersion()).isEqualTo(1);
            assertThat(entity.getCreatedBy()).isEqualTo("user-1");
            assertThat(entity.getApprovedBy()).isEqualTo("approver-1");
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getApprovedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isNotNull();
            assertThat(entity.getLines()).hasSize(2);
        }

        @Test
        @DisplayName("ID なしのドメインモデルからエンティティに変換できる")
        void shouldConvertFromDomainWithoutId() {
            JournalEntry entry = JournalEntry.create(
                    JOURNAL_DATE, "新規仕訳", CREATED_BY, 0);

            JournalEntryEntity entity = JournalEntryEntity.fromDomain(entry);

            assertThat(entity.getId()).isNull();
            assertThat(entity.getJournalDate()).isEqualTo(JOURNAL_DATE);
            assertThat(entity.getDescription()).isEqualTo("新規仕訳");
            assertThat(entity.getStatus()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("createdBy が null の場合も変換できる")
        void shouldConvertFromDomainWithNullCreatedBy() {
            JournalEntry entry = JournalEntry.reconstruct(
                    JournalEntryId.of(1),
                    JOURNAL_DATE,
                    "摘要",
                    JournalEntryStatus.DRAFT,
                    1,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null
            );

            JournalEntryEntity entity = JournalEntryEntity.fromDomain(entry);

            assertThat(entity.getCreatedBy()).isNull();
            assertThat(entity.getApprovedBy()).isNull();
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getApprovedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("エンティティからドメインモデルに変換できる")
        void shouldConvertToDomain() {
            JournalEntryEntity entity = new JournalEntryEntity();
            entity.setId(1);
            entity.setJournalDate(JOURNAL_DATE);
            entity.setDescription("仕訳復元");
            entity.setStatus("APPROVED");
            entity.setVersion(2);
            entity.setCreatedBy("user-1");
            entity.setApprovedBy("approver-1");
            entity.setCreatedAt(java.time.OffsetDateTime.now());
            entity.setApprovedAt(java.time.OffsetDateTime.now());
            entity.setUpdatedAt(java.time.OffsetDateTime.now());

            JournalEntry entry = entity.toDomain();

            assertThat(entry.getId()).isEqualTo(JournalEntryId.of(1));
            assertThat(entry.getJournalDate()).isEqualTo(JOURNAL_DATE);
            assertThat(entry.getDescription()).isEqualTo("仕訳復元");
            assertThat(entry.getStatus()).isEqualTo(JournalEntryStatus.APPROVED);
            assertThat(entry.getVersion()).isEqualTo(2);
            assertThat(entry.getCreatedBy()).isEqualTo(CREATED_BY);
            assertThat(entry.getApprovedBy()).isEqualTo(UserId.of("approver-1"));
            assertThat(entry.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("createdBy が null の場合は null で復元される")
        void shouldConvertToDomainWithNullCreatedBy() {
            JournalEntryEntity entity = new JournalEntryEntity();
            entity.setId(1);
            entity.setJournalDate(JOURNAL_DATE);
            entity.setDescription("摘要");
            entity.setStatus("DRAFT");
            entity.setVersion(0);
            entity.setCreatedBy(null);
            entity.setApprovedBy(null);
            entity.setCreatedAt(null);
            entity.setApprovedAt(null);
            entity.setUpdatedAt(null);

            JournalEntry entry = entity.toDomain();

            assertThat(entry.getCreatedBy()).isNull();
            assertThat(entry.getApprovedBy()).isNull();
            assertThat(entry.getCreatedAt()).isNull();
            assertThat(entry.getApprovedAt()).isNull();
            assertThat(entry.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("lines が null の場合は空リストで復元される")
        void shouldConvertToDomainWithNullLines() {
            JournalEntryEntity entity = new JournalEntryEntity();
            entity.setId(1);
            entity.setJournalDate(JOURNAL_DATE);
            entity.setDescription("摘要");
            entity.setStatus("DRAFT");
            entity.setVersion(0);
            entity.setLines(null);

            JournalEntry entry = entity.toDomain();

            assertThat(entry.getLines()).isEmpty();
        }
    }

    @Nested
    @DisplayName("追加フィールドの Getter/Setter")
    class AdditionalFields {

        @Test
        @DisplayName("設計書で追加されたカラムを設定・取得できる")
        void shouldSetAndGetAdditionalFields() {
            JournalEntryEntity entity = new JournalEntryEntity();
            LocalDate inputDate = LocalDate.of(2024, 2, 1);

            entity.setVoucherNumber("VN-001");
            entity.setInputDate(inputDate);
            entity.setClosingEntryFlag(1);
            entity.setSingleEntryFlag(0);
            entity.setVoucherType(1);
            entity.setRecurringFlag(0);
            entity.setEmployeeCode("E001");
            entity.setDepartmentCode("D001");
            entity.setRedSlipFlag(0);
            entity.setRedBlackVoucherNumber("RB-001");

            assertThat(entity.getVoucherNumber()).isEqualTo("VN-001");
            assertThat(entity.getInputDate()).isEqualTo(inputDate);
            assertThat(entity.getClosingEntryFlag()).isEqualTo(1);
            assertThat(entity.getSingleEntryFlag()).isZero();
            assertThat(entity.getVoucherType()).isEqualTo(1);
            assertThat(entity.getRecurringFlag()).isZero();
            assertThat(entity.getEmployeeCode()).isEqualTo("E001");
            assertThat(entity.getDepartmentCode()).isEqualTo("D001");
            assertThat(entity.getRedSlipFlag()).isZero();
            assertThat(entity.getRedBlackVoucherNumber()).isEqualTo("RB-001");
        }

        @Test
        @DisplayName("初期状態で追加フィールドが null")
        void shouldHaveNullAdditionalFieldsByDefault() {
            JournalEntryEntity entity = new JournalEntryEntity();

            assertThat(entity.getVoucherNumber()).isNull();
            assertThat(entity.getInputDate()).isNull();
            assertThat(entity.getClosingEntryFlag()).isNull();
            assertThat(entity.getSingleEntryFlag()).isNull();
            assertThat(entity.getVoucherType()).isNull();
            assertThat(entity.getRecurringFlag()).isNull();
            assertThat(entity.getEmployeeCode()).isNull();
            assertThat(entity.getDepartmentCode()).isNull();
            assertThat(entity.getRedSlipFlag()).isNull();
            assertThat(entity.getRedBlackVoucherNumber()).isNull();
        }
    }
}

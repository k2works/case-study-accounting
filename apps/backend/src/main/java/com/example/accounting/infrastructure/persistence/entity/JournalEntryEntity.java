package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import com.example.accounting.domain.model.user.UserId;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 仕訳エンティティ（永続化用）
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyFields"})
public class JournalEntryEntity {

    private Integer id;
    private LocalDate journalDate;
    private String description;
    private String status;
    private Integer version;
    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    // MyBatisがcollectionマッピングで要素を追加できるように可変リストで初期化
    private List<JournalEntryLineEntity> lines = new ArrayList<>();

    // 設計書で追加されたカラム
    private String voucherNumber;
    private LocalDate inputDate;
    private Integer closingEntryFlag;
    private Integer singleEntryFlag;
    private Integer voucherType;
    private Integer recurringFlag;
    private String employeeCode;
    private String departmentCode;
    private Integer redSlipFlag;
    private String redBlackVoucherNumber;

    /**
     * ドメインモデルからエンティティを生成する
     */
    public static JournalEntryEntity fromDomain(JournalEntry journalEntry) {
        JournalEntryEntity entity = new JournalEntryEntity();
        if (journalEntry.getId() != null) {
            entity.setId(journalEntry.getId().value());
        }
        entity.setJournalDate(journalEntry.getJournalDate());
        entity.setDescription(journalEntry.getDescription());
        entity.setStatus(journalEntry.getStatus().name());
        entity.setVersion(journalEntry.getVersion());
        if (journalEntry.getCreatedBy() != null) {
            entity.setCreatedBy(journalEntry.getCreatedBy().value());
        }
        entity.setCreatedAt(toOffsetDateTime(journalEntry.getCreatedAt()));
        entity.setUpdatedAt(toOffsetDateTime(journalEntry.getUpdatedAt()));
        entity.setLines(journalEntry.getLines().stream()
                .map(line -> JournalEntryLineEntity.fromDomain(line, entity.getId()))
                .toList());
        return entity;
    }

    /**
     * エンティティからドメインモデルを再構築する
     */
    public JournalEntry toDomain() {
        List<JournalEntryLine> domainLines = lines == null
                ? List.of()
                : lines.stream().map(JournalEntryLineEntity::toDomain).toList();
        return JournalEntry.reconstruct(
                JournalEntryId.of(id),
                journalDate,
                description,
                JournalEntryStatus.fromCode(status),
                version,
                domainLines,
                createdBy == null ? null : UserId.of(createdBy),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime()
        );
    }

    private static OffsetDateTime toOffsetDateTime(java.time.LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getJournalDate() {
        return journalDate;
    }

    public void setJournalDate(LocalDate journalDate) {
        this.journalDate = journalDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP",
            justification = "MyBatisのcollectionマッピングが直接このリストに追加するため、可変リストを返す必要がある")
    public List<JournalEntryLineEntity> getLines() {
        return lines;
    }

    public void setLines(List<JournalEntryLineEntity> lines) {
        // MyBatisがcollectionマッピングで要素を追加できるようにArrayListを使用
        this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
    }

    // 設計書で追加されたカラムのGetter/Setter
    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public LocalDate getInputDate() {
        return inputDate;
    }

    public void setInputDate(LocalDate inputDate) {
        this.inputDate = inputDate;
    }

    public Integer getClosingEntryFlag() {
        return closingEntryFlag;
    }

    public void setClosingEntryFlag(Integer closingEntryFlag) {
        this.closingEntryFlag = closingEntryFlag;
    }

    public Integer getSingleEntryFlag() {
        return singleEntryFlag;
    }

    public void setSingleEntryFlag(Integer singleEntryFlag) {
        this.singleEntryFlag = singleEntryFlag;
    }

    public Integer getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(Integer voucherType) {
        this.voucherType = voucherType;
    }

    public Integer getRecurringFlag() {
        return recurringFlag;
    }

    public void setRecurringFlag(Integer recurringFlag) {
        this.recurringFlag = recurringFlag;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public Integer getRedSlipFlag() {
        return redSlipFlag;
    }

    public void setRedSlipFlag(Integer redSlipFlag) {
        this.redSlipFlag = redSlipFlag;
    }

    public String getRedBlackVoucherNumber() {
        return redBlackVoucherNumber;
    }

    public void setRedBlackVoucherNumber(String redBlackVoucherNumber) {
        this.redBlackVoucherNumber = redBlackVoucherNumber;
    }
}

package com.example.accounting.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 仕訳差し戻しリクエスト
 */
public record RejectJournalEntryRequest(
        @NotBlank(message = "差し戻し理由は必須です")
        @Size(max = 500, message = "差し戻し理由は500文字以内で入力してください")
        String rejectionReason
) {
}

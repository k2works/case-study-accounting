package com.example.accounting.infrastructure.web.dto;

import com.example.accounting.application.port.out.DeleteAccountStructureResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "勘定科目構成削除レスポンス")
public record DeleteAccountStructureResponse(
        @Schema(description = "削除成功フラグ", example = "true")
        boolean success,

        @Schema(description = "勘定科目コード", example = "1100")
        String accountCode,

        @Schema(description = "メッセージ", example = "勘定科目構成を削除しました")
        String message,

        @Schema(description = "エラーメッセージ", example = "子階層が存在するため削除できません")
        String errorMessage
) {
    public static DeleteAccountStructureResponse from(DeleteAccountStructureResult result) {
        return new DeleteAccountStructureResponse(
                result.success(),
                result.accountCode(),
                result.message(),
                result.errorMessage()
        );
    }
}

package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.SubmitForApprovalCommand;
import com.example.accounting.application.port.out.SubmitForApprovalResult;

/**
 * 仕訳承認申請ユースケース
 */
public interface SubmitForApprovalUseCase {
    SubmitForApprovalResult execute(SubmitForApprovalCommand command);
}

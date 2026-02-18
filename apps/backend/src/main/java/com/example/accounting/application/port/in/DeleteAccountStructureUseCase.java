package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.DeleteAccountStructureCommand;
import com.example.accounting.application.port.out.DeleteAccountStructureResult;

public interface DeleteAccountStructureUseCase {
    DeleteAccountStructureResult execute(DeleteAccountStructureCommand command);
}

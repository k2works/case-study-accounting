package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.UpdateAccountStructureCommand;
import com.example.accounting.application.port.out.UpdateAccountStructureResult;

public interface UpdateAccountStructureUseCase {
    UpdateAccountStructureResult execute(UpdateAccountStructureCommand command);
}

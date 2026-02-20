package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.CreateAccountStructureCommand;
import com.example.accounting.application.port.out.CreateAccountStructureResult;

public interface CreateAccountStructureUseCase {
    CreateAccountStructureResult execute(CreateAccountStructureCommand command);
}

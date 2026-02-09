package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.DeleteUserCommand;
import com.example.accounting.application.port.out.DeleteUserResult;

public interface DeleteUserUseCase {
    DeleteUserResult execute(DeleteUserCommand command);
}

package com.manager.cli_password_manager.core.service.command.usecase.check;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.checker.CheckerResult;
import com.manager.cli_password_manager.core.entity.enums.CheckingApi;
import com.manager.cli_password_manager.core.exception.checker.CheckerException;
import com.manager.cli_password_manager.core.progressReporter.ProgressReporter;

import java.util.List;

public interface Checker {
    boolean isPwned(String password) throws CheckerException;
    CheckingApi getType();
}

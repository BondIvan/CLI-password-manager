package com.manager.cli_password_manager.core.service.command.usecase.replace;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.replacer.ReplacementResult;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;

public interface Replacement {
    ReplacementResult replace(Note replacingNote, String newValue);
    ReplaceType getReplaceType();
}

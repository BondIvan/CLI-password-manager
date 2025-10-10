package com.manager.cli_password_manager.core.service.command.usecase.replace;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;

public interface Replacement {
    Note replace(Note replacingNote, String newValue);
    ReplaceType getReplaceType();
}

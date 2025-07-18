package com.manager.cli_password_manager.core.service.command.usecase.getall;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.enums.SortType;

public interface NoteGrouper {
    String getGroupingKey(Note note);
    SortType getSortType();
}

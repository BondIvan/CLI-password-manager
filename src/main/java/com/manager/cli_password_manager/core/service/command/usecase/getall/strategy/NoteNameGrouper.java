package com.manager.cli_password_manager.core.service.command.usecase.getall.strategy;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.enums.SortType;
import com.manager.cli_password_manager.core.service.command.usecase.getall.NoteGrouper;
import org.springframework.stereotype.Component;

@Component
public class NoteNameGrouper implements NoteGrouper {
    @Override
    public String getGroupingKey(Note note) {
        return String.valueOf(note.getName().charAt(0)).toUpperCase();
    }

    @Override
    public SortType getSortType() {
        return SortType.NAME;
    }
}

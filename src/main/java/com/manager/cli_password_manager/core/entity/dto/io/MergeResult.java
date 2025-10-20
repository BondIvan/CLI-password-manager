package com.manager.cli_password_manager.core.entity.dto.io;

import com.manager.cli_password_manager.core.entity.Note;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record MergeResult(
        Map<String, List<Note>> merged,
        Set<String> replacedNoteIds
) { }

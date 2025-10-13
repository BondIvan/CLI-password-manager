package com.manager.cli_password_manager.core.export;

import com.manager.cli_password_manager.core.entity.Note;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record ExportContext(
        Map<String, List<Note>> notes,
        char[] password
) { }

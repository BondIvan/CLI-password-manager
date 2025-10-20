package com.manager.cli_password_manager.core.service.command.usecase.getall;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.entity.enums.SortType;
import com.manager.cli_password_manager.core.entity.mapper.NoteMapper;
import com.manager.cli_password_manager.core.exception.command.GetAllCommandException;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllCommand {
    private final InMemoryNotesRepository notesRepository;
    private final NoteMapper noteMapper;
    private final Map<SortType, NoteGrouper> grouperManager;

    public Map<String, List<NoteNamePlusLoginDTO>> execute(String sortBy) {
        SortType type = SortType.fromString(sortBy);

        NoteGrouper grouper = grouperManager.get(type);

        if(grouper == null)
            throw new GetAllCommandException("Sorting by this value [" + sortBy + "] doesnt support");

        List<Note> allNotes = notesRepository.getAllNotes().values().stream()
                .flatMap(List::stream)
                .toList();

        return allNotes.stream()
                .collect(Collectors.groupingBy(
                        grouper::getGroupingKey,
                        Collectors.mapping(noteMapper::toNameLoginDto,
                                Collectors.toList())
                ));
    }
}

package com.manager.cli_password_manager.core.service.command.usecase.check;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.checker.CheckerResult;
import com.manager.cli_password_manager.core.entity.dto.command.InputCheckDTO;
import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import com.manager.cli_password_manager.core.entity.enums.CheckingApi;
import com.manager.cli_password_manager.core.entity.mapper.NoteMapper;
import com.manager.cli_password_manager.core.exception.checker.CheckerException;
import com.manager.cli_password_manager.core.exception.checker.HIBPCheckerException;
import com.manager.cli_password_manager.core.exception.command.CheckCommandException;
import com.manager.cli_password_manager.core.progressReporter.ProgressReporter;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckCommand {
    private final InMemoryNotesRepository notesRepository;
    private final Map<CheckingApi, Checker> checkers;
    private final NoteMapper noteMapper;

    public List<CheckerResult> execute(InputCheckDTO inputCheckDTO, ProgressReporter reporter) {
        Checker checker = checkers.get(inputCheckDTO.api());
        if(checker == null)
            throw new CheckCommandException("Checking to this api [" + inputCheckDTO.api().getName() + "] not supported");

        Note checkingNote = null;
        if(inputCheckDTO.serviceName().isPresent())
            checkingNote = findCheckingNote(inputCheckDTO.serviceName().get(), inputCheckDTO.login());

        List<Note> checkIt;
        if(checkingNote != null)
            checkIt = List.of(checkingNote);
        else
            checkIt = notesRepository.getAllNotes().values().stream().flatMap(List::stream).toList();

        List<CheckerResult> results = new ArrayList<>();
        int totalItems = checkIt.size();
        for(int i = 0; i < checkIt.size(); i++) {
            Note note = checkIt.get(i);
            reporter.report(i * 100 / totalItems, " " + note.getName() + " -> " + note.getLogin());

            try {
                String password = noteMapper.toDecryptedDto(note).password();
                boolean isPwned = checker.isPwned(password);

                results.add(new CheckerResult(
                        new NoteNamePlusLoginDTO(note.getName(), note.getLogin()),
                        isPwned,
                        Optional.empty()
                ));
            } catch (Exception e) {
                if(e.getCause() instanceof InterruptedException) {
                    log.warn("Checking was interrupted...");
                    reporter.error("Interrupted");
                    return Collections.emptyList();
                }

                log.error("Error during check: {}", e.getMessage());
                results.add(new CheckerResult(
                        new NoteNamePlusLoginDTO(note.getName(), note.getLogin()),
                        null,
                        Optional.of("Ошибка проверки")
                ));
            }
        }

        reporter.complete("Complete");
        return results;
    }

    private Note findCheckingNote(String name, Optional<String> optionalLogin) {
        Optional<List<Note>> optionalNotes = notesRepository.findNotesByServiceName(name);

        if(optionalNotes.isEmpty())
            throw new CheckCommandException("Note with such name not found");

        List<Note> searchedNotes = optionalNotes.get();

        if(searchedNotes.size() == 1) {
            return searchedNotes.getFirst();
        }

        if(optionalLogin.isEmpty())
            throw new CheckCommandException("You have several services with such name. " +
                    "You can find out more details using the 'get' command");

        String login = optionalLogin.get();
        return searchedNotes.stream()
                .filter(nt -> login.equalsIgnoreCase(nt.getLogin()))
                .findFirst()
                .orElseThrow(() -> new CheckCommandException("Cannot find note with such name and login"));
    }

}

package com.manager.cli_password_manager.core.provider;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.repository.InMemoryNotesRepository;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ServiceNameValueProvider implements ValueProvider {
    private final InMemoryNotesRepository notesRepository;

    public ServiceNameValueProvider(InMemoryNotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        String input = completionContext.currentWord();

        if(input.isBlank())
            return new ArrayList<>();

        return notesRepository.getAllNotes().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(input))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(Note::getName)
                .map(name -> new CompletionProposal(name)
                        .displayText(name)
                        .complete(true))
                .collect(Collectors.toList());
    }

}

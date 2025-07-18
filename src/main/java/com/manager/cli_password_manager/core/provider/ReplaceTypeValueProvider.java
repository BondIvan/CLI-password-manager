package com.manager.cli_password_manager.core.provider;

import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ReplaceTypeValueProvider implements ValueProvider {
    private final Set<String> replaceNames = ReplaceType.titleNames();

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        String input = completionContext.currentWord();

        return replaceNames.stream()
                .filter(type -> type.startsWith(input))
                .map(type -> new CompletionProposal(type)
                        .displayText(type)
                        .complete(true))
                .toList();
    }
}

package com.manager.cli_password_manager.core.provider;

import com.manager.cli_password_manager.core.entity.enums.Category;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CategoryValueProvider implements ValueProvider { //TODO Придумать что-то с дополнением в консоли (чтобы не показывались другие команды)
    private final Set<String> categories = Category.titleNames();

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        String input = completionContext.currentWord();

        return categories.stream()
                .filter(category -> category.startsWith(input))
                .map(catName -> new CompletionProposal(catName)
                        .displayText(catName)
                        .complete(true))
                .collect(Collectors.toList()); //TODO .toList()
    }
}

package com.manager.cli_password_manager.core.service.command.usecase.replace.strategy;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.converter.StringCategoryConverter;
import com.manager.cli_password_manager.core.entity.enums.Category;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.exception.command.ReplaceValidationException;
import com.manager.cli_password_manager.core.service.command.usecase.replace.Replacement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryReplacer implements Replacement {
    private final StringCategoryConverter categoryConverter;

    @Override
    public Note replace(Note replacingNote, String newCategory) {
        try {
            Category category = categoryConverter.toCategory(newCategory);
            return replacingNote.withCategory(category);
        } catch (IllegalArgumentException e) {
            throw new ReplaceValidationException(e.getMessage(), e);
        }
    }

    @Override
    public ReplaceType getReplaceType() {
        return ReplaceType.CATEGORY;
    }
}

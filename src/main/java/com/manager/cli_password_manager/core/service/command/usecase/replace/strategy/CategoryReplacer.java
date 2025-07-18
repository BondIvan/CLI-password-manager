package com.manager.cli_password_manager.core.service.command.usecase.replace.strategy;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.converter.StringCategoryConverter;
import com.manager.cli_password_manager.core.entity.dto.replacer.ReplacementResult;
import com.manager.cli_password_manager.core.entity.enums.Category;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.service.command.usecase.replace.Replacement;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CategoryReplacer implements Replacement {
    private final StringCategoryConverter categoryConverter;

    public CategoryReplacer(StringCategoryConverter categoryConverter) {
        this.categoryConverter = categoryConverter;
    }

    @Override
    public ReplacementResult replace(Note replacingNote, String newCategory) {
        Category category = categoryConverter.toCategory(newCategory);
        return new ReplacementResult(
                replacingNote.withCategory(category),
                Optional.empty()
        );
    }

    @Override
    public ReplaceType getReplaceType() {
        return ReplaceType.CATEGORY;
    }
}

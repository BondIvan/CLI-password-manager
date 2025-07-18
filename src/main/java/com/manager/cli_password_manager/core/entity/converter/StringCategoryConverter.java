package com.manager.cli_password_manager.core.entity.converter;

import com.manager.cli_password_manager.core.entity.enums.Category;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StringCategoryConverter {
    public Category toCategory(String strCategory) {
        if(strCategory == null)
            return Category.NO_CATEGORY;

        String lowerCaseStrCategory = strCategory.toLowerCase();
        for(Category category: Category.values()) {
            if(category.getTitle().equals(lowerCaseStrCategory))
                return category;
        }

        throw new IllegalArgumentException("This category [" + strCategory + "] does not exits");
    }

    public String toUpperCaseString(Category category) {
        Objects.requireNonNull(category);
        return category.name();
    }

    public String toLowerCaseString(Category category) {
        Objects.requireNonNull(category);
        return category.getTitle();
    }
}

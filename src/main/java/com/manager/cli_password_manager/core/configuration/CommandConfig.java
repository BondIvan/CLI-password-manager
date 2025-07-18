package com.manager.cli_password_manager.core.configuration;

import com.manager.cli_password_manager.core.entity.enums.CheckingApi;
import com.manager.cli_password_manager.core.entity.enums.ReplaceType;
import com.manager.cli_password_manager.core.entity.enums.SortType;
import com.manager.cli_password_manager.core.service.command.usecase.check.Checker;
import com.manager.cli_password_manager.core.service.command.usecase.getall.NoteGrouper;
import com.manager.cli_password_manager.core.service.command.usecase.replace.Replacement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class CommandConfig {
    @Bean
    public Map<SortType, NoteGrouper> sortTypeWithNoteGrouperMap(List<NoteGrouper> groupers) {
        return groupers.stream()
                .collect(Collectors.toMap(
                        NoteGrouper::getSortType,
                        Function.identity(),
                        (g1, g2) -> g1, () -> new EnumMap<>(SortType.class)
                ));
    }

    @Bean
    public Map<ReplaceType, Replacement> replaceTypeWithReplacementMap(List<Replacement> replacements) {
        return replacements.stream()
                .collect(Collectors.toMap(
                        Replacement::getReplaceType,
                        Function.identity(),
                        (r1, r2) -> r1, () -> new EnumMap<>(ReplaceType.class)
                ));
    }

    @Bean
    public Map<CheckingApi, Checker> checkingApiWithCheckerMap(List<Checker> checkers) {
        return checkers.stream()
                .collect(Collectors.toMap(
                        Checker::getType,
                        Function.identity(),
                        (c1, c2) -> c1, () -> new EnumMap<>(CheckingApi.class)
                ));
    }
}

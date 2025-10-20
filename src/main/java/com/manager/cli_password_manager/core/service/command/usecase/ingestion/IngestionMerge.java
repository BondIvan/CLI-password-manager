package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.entity.dto.io.MergeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@Slf4j
@Component
public class IngestionMerge {

    // equals notes will be replaced with new ones
    public MergeResult merge(Map<String, List<Note>> original, Map<String, List<Note>> toImport) {
        if(toImport == null || toImport.isEmpty())
            return new MergeResult(original, Collections.emptySet());

        if(original.isEmpty()) {
            original.putAll(toImport);

            int afterMergedCount = toImport.values().stream().mapToInt(List::size).sum();
            log.info("New notes - {}", afterMergedCount);

            return new MergeResult(original, Collections.emptySet());
        }

        int beforeMergedCount = original.values().stream().mapToInt(List::size).sum();

        Set<String> replacedNoteIds = new HashSet<>();
        BiFunction<List<Note>, List<Note>, List<Note>> remappingFunction = (orig, imprt) -> {
            Set<Note> set = new HashSet<>(imprt);

            orig.removeIf(note -> {
                if(set.contains(note)) {
                    replacedNoteIds.add(note.getId());
                    return true;
                }

                return false;
            });

            orig.addAll(imprt);
            return orig;
        };

        toImport.forEach((keyName, list) -> {
            if(!original.containsKey(keyName))
                original.put(keyName, list);
            else
                original.merge(keyName, list, remappingFunction);
        });

        int afterMergedCount = original.values().stream().mapToInt(List::size).sum();
        log.info("New notes - {}", (afterMergedCount - beforeMergedCount));

        return new MergeResult(original, replacedNoteIds);
    }
}

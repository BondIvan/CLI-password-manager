package com.manager.cli_password_manager.core.service.command.usecase.ingestion;

import com.manager.cli_password_manager.core.entity.Note;
import com.manager.cli_password_manager.core.repository.InMemoryVaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class IngestionMerge {
    private final InMemoryVaultRepository vaultRepository;

    // equals notes will be replaced with new ones
    // returns the number of new notes
    public int merge(Map<String, List<Note>> original, Map<String, List<Note>> toImport) {
        if(toImport == null)
            return 0;

        if(original.isEmpty()) {
            original.putAll(toImport);
            return toImport.size();
        }

        AtomicInteger countNew = new AtomicInteger();

        BiFunction<List<Note>, List<Note>, List<Note>> biFunction2 = (orig, imprt) -> {
            int initSizeOrig = orig.size();
            Set<Note> set = new HashSet<>(imprt);

            orig.removeIf(note -> {
                if(set.contains(note)) {
                    vaultRepository.deleteKey(note.getId());
                    return true;
                }

                return false;
            });

            orig.addAll(imprt);

            countNew.addAndGet(orig.size() - initSizeOrig);

            return orig;
        };

        toImport.forEach((key, list) -> {
        if(!original.containsKey(key)) {
            original.put(key, list);
            countNew.addAndGet(list.size());
        }
        else
            original.merge(key, list, biFunction2);
        });

        return countNew.get();
    }
}

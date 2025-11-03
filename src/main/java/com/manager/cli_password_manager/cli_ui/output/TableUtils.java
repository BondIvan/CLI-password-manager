package com.manager.cli_password_manager.cli_ui.output;

import com.manager.cli_password_manager.core.entity.dto.command.NoteNamePlusLoginDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Component
public class TableUtils {
    public List<String[]> preparedDataForTable(Map<String, List<NoteNamePlusLoginDTO>> data,
                                                Function<NoteNamePlusLoginDTO, String> formatter,
                                                UnaryOperator<List<NoteNamePlusLoginDTO>> unaryOperator) {
        List<String[]> tableRows = new ArrayList<>();
        for(Map.Entry<String, List<NoteNamePlusLoginDTO>> entry: data.entrySet()) {
            String key_sortedBy = entry.getKey();
            List<NoteNamePlusLoginDTO> value_sortedBy = unaryOperator.apply(entry.getValue());

            tableRows.add(new String[]{key_sortedBy + ":"});
            value_sortedBy.forEach(item -> {
                String formattedStr = "\u00A0\u00A0 • " + formatter.apply(item);
                tableRows.add(new String[] {formattedStr});
            });
        }

        return tableRows;
    }
}

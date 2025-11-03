package com.manager.cli_password_manager.cli_ui.output;

import lombok.RequiredArgsConstructor;
import org.jline.reader.LineReader;

@RequiredArgsConstructor
public class ShellInputHelper {
    public static final Character DEFAULT_MASK = '*';

    private final LineReader lineReader;

    public String readInput(String queryMessage, boolean mask) {
        return mask ?
                lineReader.readLine(queryMessage, DEFAULT_MASK)
                : lineReader.readLine(queryMessage);
    }
}

package com.manager.cli_password_manager.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.cli_password_manager.cli_ui.output.ShellOutputHelper;
import com.manager.cli_password_manager.cli_ui.output.ShellInputHelper;
import org.jline.reader.Completer;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.security.SecureRandom;

@Configuration
public class AppConfig {

    @Bean
    public ShellInputHelper inputReader(
            @Lazy Terminal terminal,
            @Lazy Parser parser,
            @Lazy Completer completer,
            @Lazy History history
    ) {
        LineReaderBuilder lineReaderBuilder = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .history(history)
                .highlighter(new DefaultHighlighter() {
                    @Override
                    public AttributedString highlight(LineReader reader, String buffer) {
                        return new AttributedString(buffer, AttributedStyle.BOLD.foreground(AttributedStyle.WHITE));
                    }
                })
                .parser(parser);

        LineReader lineReader = lineReaderBuilder.build();
        lineReader.unsetOpt(LineReader.Option.INSERT_TAB);

        return new ShellInputHelper(lineReader);
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean
    public ShellOutputHelper shellHelper(@Lazy Terminal terminal) {
        return new ShellOutputHelper(terminal);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

package com.manager.cli_password_manager.cli_ui.output;

import com.manager.cli_password_manager.core.entity.enums.MessageColor;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Value;

public class ShellHelper {
    @Value("${shell.out.info}")
    public String infoColor;

    @Value("${shell.out.success}")
    public String successColor;

    @Value("${shell.out.error}")
    public String errorColor;

    @Value("${shell.out.warning}")
    public String warningColor;

    private final Terminal terminal;

    public ShellHelper(Terminal terminal) {
        this.terminal = terminal;
    }

    public String getColored(String message, MessageColor color) {
        return new AttributedStringBuilder()
                .append(message, AttributedStyle.DEFAULT.foreground(color.getColorValue()))
                .toAnsi();
    }

    public String getInfoMessage(String message) {
        return getColored(message, MessageColor.valueOf(infoColor));
    }

    public String getSuccessMessage(String message) {
        return getColored(message, MessageColor.valueOf(successColor));
    }

    public String getErrorMessage(String message) {
        return getColored(message, MessageColor.valueOf(errorColor));
    }

    public String getWarningMessage(String message) {
        return getColored(message, MessageColor.valueOf(warningColor));
    }

    public void print(String message, MessageColor color) {
        String outputLine = message;
        if(color != null) {
            outputLine = getColored(message, color);
        }

        terminal.writer().println(outputLine);
        terminal.flush();
    }

    public void print(String message) {
        print(message, null);
    }

    public void printWithoutLineBreak(String message, MessageColor color) {
        String outputLine = message;
        if(color != null) {
            outputLine = getColored(message, color);
        }

        terminal.writer().print(outputLine);
        terminal.flush();
    }

    public void printWithoutLineBreak(String message) {
        printWithoutLineBreak(message, null);
    }

    public void printInfo(String message) {
        print(message, MessageColor.valueOf(infoColor));
    }

    public void printSuccess(String message) {
        print(message, MessageColor.valueOf(successColor));
    }

    public void printError(String message) {
        print(message, MessageColor.valueOf(errorColor));
    }

    public void printWarning(String message) {
        print(message, MessageColor.valueOf(warningColor));
    }
}

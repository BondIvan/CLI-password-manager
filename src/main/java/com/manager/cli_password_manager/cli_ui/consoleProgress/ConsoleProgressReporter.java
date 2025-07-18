package com.manager.cli_password_manager.cli_ui.consoleProgress;

import com.manager.cli_password_manager.cli_ui.output.ShellHelper;
import com.manager.cli_password_manager.core.progressReporter.ProgressReporter;
import org.springframework.stereotype.Component;

@Component
public class ConsoleProgressReporter implements ProgressReporter {
    private static final int WIDTH = 40;
    private int lastLineLength = 0;
    private final ShellHelper shellHelper;

    public ConsoleProgressReporter(ShellHelper shellHelper) {
        this.shellHelper = shellHelper;
    }

    @Override
    public void report(int percent, String message) {
        // \r - возврат каретки в начало строки
        String progressLine = String.format("\r[%-20s] %3d%% %s", generateProgressBar(percent), percent, message);
        if (progressLine.length() < lastLineLength) {
            progressLine += " ".repeat(lastLineLength - progressLine.length());
        }
        shellHelper.printWithoutLineBreak(progressLine);
        lastLineLength = progressLine.length(); // Сохраняем длину, чтобы правильно очистить в следующий раз
    }

    @Override
    public void complete(String finalMessage) {
        clearLine();
        shellHelper.printSuccess("✓ " + finalMessage);
        lastLineLength = 0;
    }

    @Override
    public void error(String errorMessage) {
        clearLine();
        shellHelper.printError("✗ ERROR: " + errorMessage);
        lastLineLength = 0;
    }

    @Override
    public void indeterminate(String indeterminateMessage) {

    }

    private void clearLine() {
        if (lastLineLength > 0) {
            shellHelper.print("\r" + " ".repeat(lastLineLength) + "\r");
        }
    }

    private String generateProgressBar(int percentage) {
        int filledCount = (int) (WIDTH * (percentage / 100.0));
        int emptyCount = WIDTH - filledCount;
        return "#".repeat(Math.max(0, filledCount)) +
                "-".repeat(Math.max(0, emptyCount));
    }
}

package com.manager.cli_password_manager.cli_ui.output.command;

import com.manager.cli_password_manager.cli_ui.output.ShellOutputHelper;
import com.manager.cli_password_manager.core.entity.enums.MessageColor;
import com.manager.cli_password_manager.core.progressReporter.ProgressReporter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsoleTimerReporter implements ProgressReporter {
    private final ShellOutputHelper shellOutputHelper;

    @Getter
    private int time = 5;

    private int step = time / 3;

    @Override
    public void report(int remainingSeconds, String units) {
        clearLine();

        int redZone = step;
        int yellowZone = step * 2;
        int greenZone = step * 3;

        MessageColor timerColor = MessageColor.GREEN;
        if(remainingSeconds <= redZone)
            timerColor = MessageColor.RED;
        else if(remainingSeconds <= yellowZone)
            timerColor = MessageColor.YELLOW;

        shellOutputHelper.printWithoutLineBreak(remainingSeconds + " ", timerColor);
        shellOutputHelper.printWithoutLineBreak(units);
    }

    @Override
    public void complete(String units) {
        clearLine();
        shellOutputHelper.printWithoutLineBreak("0 ", MessageColor.RED);
        shellOutputHelper.printWithoutLineBreak(units);
    }

    @Override
    public void error(String errorMessage) {
        clearLine();
        shellOutputHelper.printError("✗ " + errorMessage);
    }

    @Override
    public void indeterminate(String indeterminateMessage) {

    }

    public void setTime(int time) {
        if(time < 3)
            throw new IllegalArgumentException("Timer cannot be less then 3 seconds");
        if(time > 30)
            throw new IllegalArgumentException("Timer cannot be more then 30 seconds");

        this.time = time;
        this.step = time / 3;
    }

    private void clearLine() {
        shellOutputHelper.printWithoutLineBreak("\r");
        shellOutputHelper.printWithoutLineBreak("\033[K");
    }
}

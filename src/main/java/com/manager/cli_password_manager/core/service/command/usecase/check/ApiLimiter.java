package com.manager.cli_password_manager.core.service.command.usecase.check;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class ApiLimiter {
    private Instant lastCheck;
    private final long delay;

    public ApiLimiter(long delay) {
        this.delay = delay;
        lastCheck = Instant.now().minusMillis(delay);
    }

    public void doDelay() throws InterruptedException {
        Instant now = Instant.now();
        long difference = Duration.between(lastCheck, now).toMillis();

        if(difference < delay) {
            TimeUnit.MILLISECONDS.sleep(delay - difference);
        }

        lastCheck = Instant.now();
    }
}

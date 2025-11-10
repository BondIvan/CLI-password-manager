package com.manager.cli_password_manager.core.service.clipboard;

import com.manager.cli_password_manager.core.exception.clipboard.ClipboardException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ClipboardService {
    @Value("${clipboard.clearAfterSeconds}")
    private long removeAfterSeconds;

    private final Clipboard clipboard;
    private final ScheduledExecutorService scheduledExecutorService;

    private Optional<ScheduledFuture<?>> activeCleanupTask = Optional.empty();

    public ClipboardService() {
        this.clipboard = getSystemClipboardSafely();
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread cleanupThread = new Thread(runnable, "CB-Cleanup-th");
            cleanupThread.setDaemon(true);
            log.info("Create new clipboard cleanup thread");
            return cleanupThread;
        });
    }

    public boolean isClipboardAvailable() {
        return clipboard != null;
    }

    public void copyToClipboard(String value) {
        if(clipboard == null) {
            log.warn("Clipboard is not available. Value not copied.");
            throw new ClipboardException("Clipboard is not available. Value not copied.");
        }

        StringSelection stringSelection = new StringSelection(value);
        clipboard.setContents(stringSelection, null);

        addScheduleCleanupClipboardTask();
    }

    public void clearClipboardBeforeShutdown() {
        if(clipboard == null)
            return;

        activeCleanupTask.ifPresent(task -> {
            task.cancel(false);
            clipboard.setContents(new StringSelection(""), null);
            log.info("Copied value was cleared before shutdown");
        });
    }

    private void addScheduleCleanupClipboardTask() {
        activeCleanupTask.ifPresent(task -> {
            task.cancel(true);
            activeCleanupTask = Optional.empty();
        });

        ScheduledFuture<?> cleanupTask = scheduledExecutorService.schedule(() -> {
            clipboard.setContents(new StringSelection(""), null);
            activeCleanupTask = Optional.empty();
            log.info("Copied value was cleared");
        }, removeAfterSeconds, TimeUnit.SECONDS);

        activeCleanupTask = Optional.of(cleanupTask);
    }

    private Clipboard getSystemClipboardSafely() {
        log.info("ClipboardService: is headless - {}", GraphicsEnvironment.isHeadless());
        if(GraphicsEnvironment.isHeadless()) {
            log.warn("Application is in headless mode. Clipboard not available.");
            return null;
        }

        try {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (Throwable e) {
            log.warn("Clipboard is not available. Reason: {}", e.getMessage());
            return null;
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        log.info("PreDestroy clipboard service called");
        log.info("Start shutdown schedule executor service...");
        scheduledExecutorService.shutdown();
        try {
            if(!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                log.error("Executor did not terminate in 5 seconds... forcing shutdown");
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("Clipboard shutdown complete.");
    }
}

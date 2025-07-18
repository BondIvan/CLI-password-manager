package com.manager.cli_password_manager.core.progressReporter;

public interface ProgressReporter {
    /**
     * Сообщает о текущем прогрессе.
     * @param percent Процент выполнения (0-100).
     * @param message Текущий статус или сообщение.
     */
    void report(int percent, String message);

    /**
     * Сообщает о завершении операции.
     * @param finalMessage Сообщение о завершении.
     */
    void complete(String finalMessage);

    /**
     * Сообщает об ошибке.
     * @param errorMessage Сообщение об ошибке.
     */
    void error(String errorMessage);

    /**
     * Сообщает о неожиданном завержении.
     * @param indeterminateMessage Сообщение о прерывании.
     */
    void indeterminate(String indeterminateMessage);
}

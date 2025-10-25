package com.manager.cli_password_manager.core.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileTransaction {
    String name();

    RollbackFailureAction rollbackFailureAction() default RollbackFailureAction.LOG_WARNING;

    Class<? extends Throwable>[] rollbackFor() default {};

    Class<? extends Throwable>[] noRollbackFor() default {};
}

enum RollbackFailureAction {
    THROW_EXCEPTION,
    LOG_WARNING
}
package com.manager.cli_password_manager.core.configuration;

import com.manager.cli_password_manager.core.service.command.usecase.check.ApiLimiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CheckerConfig {
    @Bean
    @Qualifier("HIBPChecker")
    public ApiLimiter hibpApiLimiter(@Value("${api.hibp.delay}") long delay) {
        return new ApiLimiter(delay);
    }
}

package com.manager.cli_password_manager.core.entity.converter;

import com.manager.cli_password_manager.core.entity.enums.CheckingApi;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StringCheckingApiConverter {
    public CheckingApi toCheckingApi(String str) {
        if(str == null)
            return null;

        String lowerCaseStrApi = str.toLowerCase();
        for(CheckingApi api: CheckingApi.values()) {
            if(api.getName().equals(lowerCaseStrApi))
                return api;
        }

        throw new IllegalArgumentException("This category [" + str + "] does not exits");
    }

    public String toUpperCaseString(CheckingApi api) {
        Objects.requireNonNull(api);
        return api.name();
    }

    public String toLowerCaseString(CheckingApi api) {
        Objects.requireNonNull(api);
        return api.getName();
    }
}

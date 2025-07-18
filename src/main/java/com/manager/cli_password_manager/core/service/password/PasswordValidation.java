package com.manager.cli_password_manager.core.service.password;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class PasswordValidation {
    private String reason;

    public boolean isValid(String password) {
        if(password == null) {
            setReason("Password cannot be null");
            return false;
        }

        if(password.length() < 10) {
            setReason("Password length cannot be less than 10");
            return false;
        }

        Map<Pattern, String> patternsAndMessages = new HashMap<>();
        patternsAndMessages.put(Pattern.compile(".*\\p{Lu}.*"), "Password should contain upper case characters"); // Any language
        patternsAndMessages.put(Pattern.compile(".*\\p{Ll}.*"), "Password should contain lower case characters"); // Any language
        patternsAndMessages.put(Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};:\"\\\\|,.<>/?].*"), "Password should contain special symbols");
        patternsAndMessages.put(Pattern.compile(".*\\d.*"), "Password should contain digits");

        List<String> errors = new ArrayList<>();
        for(Map.Entry<Pattern, String> entry: patternsAndMessages.entrySet()) {
            if(!entry.getKey().matcher(password).matches())
                errors.add(entry.getValue());
        }

        if(!errors.isEmpty()) {
            setReason(String.join("\n", errors));
            return false;
        }

        return true;
    }

    private void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}

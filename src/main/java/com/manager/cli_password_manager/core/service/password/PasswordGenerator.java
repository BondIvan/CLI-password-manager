package com.manager.cli_password_manager.core.service.password;

import org.springframework.stereotype.Component;

import java.security.DrbgParameters;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.*;

@Component
public class PasswordGenerator {
    private static final char[] lower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] upper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char[] numbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final char[] special = {'!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
            ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~'};
    private static final char[][] all_signs = {lower, upper, numbers, special};
    private final int length = 15; // Length of password less than 12 is not safety
    private static final SecureRandom SECURE_RANDOM;

    static {
        try {
            Security.setProperty("securerandom.drbg.config", "Hash_DRBG, SHA-512");
            SECURE_RANDOM = SecureRandom.getInstance("DRBG",
                    DrbgParameters.instantiation(256, DrbgParameters.Capability.PR_AND_RESEED, null));
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Failed to initialize SecureRandom", e);
        }
    }

    protected SecureRandom getSecureRandom() {
        return SECURE_RANDOM;
    }

    public String generate() {
        StringBuilder randomString = new StringBuilder();
        for(int i = 0; i < length; i++) {
            int bracket_index = i % 4;
            int index = getSecureRandom().nextInt(all_signs[bracket_index].length);
            randomString.append(all_signs[bracket_index][index]);
        }

        // Shuffling symbols
        return shuffleCharacters(randomString).toString();
    }

    public String generate(char ... excludeSymbols) {
        Set<Character> excludeSet = new HashSet<>();
        for(char c: excludeSymbols)
            excludeSet.add(c);

        StringBuilder randomString = new StringBuilder();
        for(int i = 0; i < length; i++) {
            int bracket_index = i % 4;
            int index = getSecureRandom().nextInt(all_signs[bracket_index].length);

            // Excluding characters
            if(excludeSet.contains(all_signs[bracket_index][index]))
                continue;

            randomString.append(all_signs[bracket_index][index]);
        }

        // Shuffling symbols
        return shuffleCharacters(randomString).toString();
    }

    private StringBuilder shuffleCharacters(StringBuilder str) {
        List<Character> list = new ArrayList<>();
        for(char c: str.toString().toCharArray())
            list.add(c);

        Collections.shuffle(list, getSecureRandom());

        StringBuilder result = new StringBuilder();
        for(char c: list)
            result.append(c);

        return result;
    }
}

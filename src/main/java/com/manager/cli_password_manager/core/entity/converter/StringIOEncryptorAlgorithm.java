package com.manager.cli_password_manager.core.entity.converter;

import com.manager.cli_password_manager.core.entity.enums.IOEncryptorAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class StringIOEncryptorAlgorithm {
    public IOEncryptorAlgorithm toIOAlgorithmFormat(String str) {
        if(str == null || str.isEmpty())
            return null;

        String lowercaseStr = str.toLowerCase();
        for(IOEncryptorAlgorithm algorithm: IOEncryptorAlgorithm.values()) {
            if(algorithm.name().toLowerCase().equals(lowercaseStr))
                return algorithm;
        }

        throw new IllegalArgumentException("Unknown algorithm - [" + str + "]");
    }
}

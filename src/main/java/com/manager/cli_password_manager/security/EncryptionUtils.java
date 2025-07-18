package com.manager.cli_password_manager.security;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class EncryptionUtils {
    public static void clearData(byte[]... data) {
        if(data == null)
            return;

        for(byte[] value: data) {
            if(value == null)
                continue;

            Arrays.fill(value, (byte) '\0');
        }
    }

    public static void clearData(char[]... data) {
        if(data == null)
            return;

        for(char[] value: data) {
            if(value == null)
                continue;

            Arrays.fill(value,'\0');
        }
    }
}

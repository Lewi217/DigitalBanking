package com.mycompany.app.utilitis.secreteGeneratorAcount;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AccountNumberGenerator {
    private static final String ACCOUNT_PREFIX = "ACC";
    private static final int ACCOUNT_NUMBER_LENGTH = 10;
    private final Random random = new Random();

    public String generate() {
        StringBuilder builder = new StringBuilder(ACCOUNT_PREFIX);
        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
}

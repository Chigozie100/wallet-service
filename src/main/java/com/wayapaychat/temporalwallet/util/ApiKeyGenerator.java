package com.wayapaychat.temporalwallet.util;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGenerator {

    private static final int API_KEY_LENGTH = 64; // Length in bytes

    /**
     * Generate a secure API key.
     *
     * @return a Base64-encoded API key string
     */
    public static String generateApiKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] apiKeyBytes = new byte[API_KEY_LENGTH];
        secureRandom.nextBytes(apiKeyBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(apiKeyBytes);
    }

    public static void main(String[] args) {
        String apiKey = generateApiKey();
        System.out.println("Generated API Key: " + apiKey);
    }
}


package com.wayapaychat.temporalwallet.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import java.security.Key;
import java.util.Base64;

public class SecurityConstants {

    public static final long EXPIRATION_TIME = 31557600000l; //1 week
    public static final long PASSWORD_RESET_EXPIRATION_TIME = 3600000;
    public static final String TOKEN_PREFIX = "serial ";
    public static final String HEADER_STRING = "Authorization";
//    public static final String HEADER_STRING ="x-auth-data";
    public static final String LOGIN_URL = "/api/users/login";


    private static final Key secret = MacProvider.generateKey(SignatureAlgorithm.HS256);
    private static final byte[] secretBytes = secret.getEncoded();
    private static final String base64SecretBytes = Base64.getEncoder().encodeToString(secretBytes);

    public static String getSecret() {
        return base64SecretBytes;
    }
}

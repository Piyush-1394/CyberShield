package com.cybershieldai.api.auth;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

@Component
public class TokenHasher {
    private final SecureRandom random = new SecureRandom();

    public String randomToken() {
        byte[] buf = new byte[32];
        random.nextBytes(buf);
        return HexFormat.of().formatHex(buf);
    }

    public String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

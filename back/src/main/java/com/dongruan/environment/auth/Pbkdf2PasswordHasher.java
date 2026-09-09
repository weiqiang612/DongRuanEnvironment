package com.dongruan.environment.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class Pbkdf2PasswordHasher {

    private static final String PREFIX = "pbkdf2";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(final String password) {
        final byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        final byte[] derivedKey = derive(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        return PREFIX + "$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derivedKey);
    }

    public boolean matches(final String password, final String storedValue) {
        if (!isHash(storedValue)) {
            return constantTimeEquals(password.getBytes(StandardCharsets.UTF_8), storedValue.getBytes(StandardCharsets.UTF_8));
        }
        try {
            final String[] parts = storedValue.split("\\$", -1);
            final int iterations = Integer.parseInt(parts[1]);
            final byte[] salt = Base64.getDecoder().decode(parts[2]);
            final byte[] expected = Base64.getDecoder().decode(parts[3]);
            final byte[] actual = derive(password.toCharArray(), salt, iterations, expected.length * Byte.SIZE);
            return constantTimeEquals(actual, expected);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean isHash(final String value) {
        return value != null && value.startsWith(PREFIX + "$");
    }

    private byte[] derive(final char[] password, final byte[] salt, final int iterations, final int keyLengthBits) {
        final PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("密码服务不可用", exception);
        } finally {
            spec.clearPassword();
        }
    }

    private boolean constantTimeEquals(final byte[] left, final byte[] right) {
        return MessageDigest.isEqual(left, right);
    }
}

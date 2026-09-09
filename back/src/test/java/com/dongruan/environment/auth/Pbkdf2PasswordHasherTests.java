package com.dongruan.environment.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Pbkdf2PasswordHasherTests {

    private final Pbkdf2PasswordHasher passwordHasher = new Pbkdf2PasswordHasher();

    @Test
    void hashesAndMatchesPasswordsWithCurrentWorkFactor() {
        final String password = "test-password";
        final String hash = passwordHasher.hash(password);

        assertTrue(hash.startsWith("pbkdf2$600000$"));
        assertTrue(passwordHasher.matches(password, hash));
        assertFalse(passwordHasher.matches("wrong-password", hash));
    }
}

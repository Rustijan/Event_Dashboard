package com.epam.eventhub.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderTest {
    private final PasswordEncoder encoder = new Pbkdf2PasswordEncoder();

    @Test
    void encodedPasswordCanBeVerified() {
        String encoded = encoder.encode("Password123!");

        assertTrue(encoder.matches("Password123!", encoded));
        assertFalse(encoder.matches("wrong-password", encoded));
    }

    @Test
    void randomSaltProducesDifferentHashes() {
        String first = encoder.encode("Password123!");
        String second = encoder.encode("Password123!");

        assertNotEquals(first, second);
    }

    @Test
    void malformedHashIsRejected() {
        assertFalse(encoder.matches("Password123!", "not-a-valid-hash"));
    }

    @Test
    void nullValuesAreRejectedDuringMatching() {
        assertFalse(encoder.matches(null, "encoded"));
        assertFalse(encoder.matches("Password123!", null));
    }
}

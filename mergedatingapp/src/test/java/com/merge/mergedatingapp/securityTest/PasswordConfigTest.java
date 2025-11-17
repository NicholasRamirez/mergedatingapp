package com.merge.mergedatingapp.securityTest;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.merge.mergedatingapp.security.PasswordConfig;

import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {

    @Test
    void passwordEncoder_returnsBCryptAndCanEncode() {
        PasswordConfig cfg = new PasswordConfig();

        PasswordEncoder encoder = cfg.passwordEncoder();

        assertNotNull(encoder, "PasswordEncoder bean should not be null");
        assertTrue(encoder instanceof BCryptPasswordEncoder,
                "PasswordEncoder should be a BCryptPasswordEncoder");

        String raw = "password-hello";
        String hash = encoder.encode(raw);

        assertNotNull(hash);
        assertNotEquals(raw, hash, "Encoded password should not equal raw password");
        assertTrue(encoder.matches(raw, hash), "Encoded password should match raw password");
    }
}

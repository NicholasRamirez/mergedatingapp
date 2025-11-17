package com.merge.mergedatingapp.profilesTest;

import com.merge.mergedatingapp.profiles.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository repo;

    @Test
    void findByUserId_returnsProfileWhenExists() {
        UUID userId = UUID.randomUUID();

        Profile p = Profile.builder()
                .userId(userId)
                .name("Kyle")
                .birthday(LocalDate.of(2000, 1, 1))
                .city("City")
                .lastUpdated(Instant.now())
                .build();

        repo.save(p);

        Optional<Profile> found = repo.findByUserId(userId);

        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getUserId());
    }

    @Test
    void existsByUserId_returnsTrueWhenProfileExists() {
        UUID userId = UUID.randomUUID();

        Profile p = Profile.builder()
                .userId(userId)
                .lastUpdated(Instant.now())
                .build();

        repo.save(p);

        assertTrue(repo.existsByUserId(userId));
    }

    @Test
    void existsByUserId_returnsFalseWhenProfileMissing() {
        assertFalse(repo.existsByUserId(UUID.randomUUID()));
    }
}

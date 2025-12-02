package com.merge.mergedatingapp.usersTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.merge.mergedatingapp.users.User;
import com.merge.mergedatingapp.users.UserRepository;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByUsername_returnsUserWhenPresent() {
        User u = User.builder()
                .username("TestUser")
                .passwordHash("test123")
                .build();

        userRepository.save(u);

        Optional<User> found = userRepository.findByUsername("TestUser");

        assertTrue(found.isPresent(), "User should be found by username");
        assertEquals("TestUser", found.get().getUsername());
        assertEquals("test123", found.get().getPasswordHash());
        assertNotNull(found.get().getId(), "Saved user should have generated id");
    }

    @Test
    void findByUsername_returnsEmptyWhenNotPresent() {
        Optional<User> found = userRepository.findByUsername("missingUser");
        assertTrue(found.isEmpty(), "No user should be found for a non-existing username");
    }

    @Test
    void existsByUsername_returnsTrueWhenUserExists() {
        User u = User.builder()
                .username("existsUser")
                .passwordHash("test123")
                .build();

        userRepository.save(u);

        assertTrue(userRepository.existsByUsername("existsUser"));
    }

    @Test
    void existsByUsername_returnsFalseWhenUserDoesNotExist() {
        assertFalse(userRepository.existsByUsername("nopeUser"));
    }
}

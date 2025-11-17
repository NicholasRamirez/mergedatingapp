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
    void saveAndFindByEmail_returnsUserWhenPresent() {
        User u = User.builder()
                .email("test@gmail.com")
                .passwordHash("test123")
                .build();

        userRepository.save(u);

        Optional<User> found = userRepository.findByEmail("test@gmail.com");

        assertTrue(found.isPresent(), "User should be found by email");
        assertEquals("test@gmail.com", found.get().getEmail());
        assertEquals("test123", found.get().getPasswordHash());
        assertNotNull(found.get().getId(), "Saved user should have generated id");
    }

    @Test
    void findByEmail_returnsEmptyWhenNotPresent() {
        Optional<User> found = userRepository.findByEmail("missing@gmail.com");
        assertTrue(found.isEmpty(), "No user should be found for a non-existing email");
    }

    @Test
    void existsByEmail_returnsTrueWhenUserExists() {
        User u = User.builder()
                .email("exists@gmail.com")
                .passwordHash("test123")
                .build();

        userRepository.save(u);

        assertTrue(userRepository.existsByEmail("exists@gmail.com"));
    }

    @Test
    void existsByEmail_returnsFalseWhenUserDoesNotExist() {
        assertFalse(userRepository.existsByEmail("nope@gmail.com"));
    }
}

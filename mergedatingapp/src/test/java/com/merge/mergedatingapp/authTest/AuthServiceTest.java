package com.merge.mergedatingapp.authTest;

import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.dto.LoginRequest;
import com.merge.mergedatingapp.auth.dto.userResponse;
import com.merge.mergedatingapp.auth.dto.RegisterRequest;
import com.merge.mergedatingapp.auth.dto.TokenResponse;
import com.merge.mergedatingapp.users.User;
import com.merge.mergedatingapp.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository users;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Test
    void register_savesNewUser_whenEmailNotTaken() {
        RegisterRequest req = new RegisterRequest("Test@gmail.com", "secret");

        when(users.existsByEmail("Test@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("ENCODED");

        authService.register(req);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(users).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@gmail.com");  // lowercased
        assertThat(saved.getPasswordHash()).isEqualTo("ENCODED");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest("test@gmail.com", "secret");

        when(users.existsByEmail("test@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).contains("Email already registered");
                });

        verify(users, never()).save(any());
    }

    @Test
    void login_returnsDevToken_whenCredentialsValid() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@gmail.com")
                .passwordHash("HASH")
                .createdAt(Instant.now())
                .build();

        LoginRequest req = new LoginRequest("test@gmail.com", "secret");

        when(users.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "HASH")).thenReturn(true);

        TokenResponse resp = authService.login(req);

        assertThat(resp.accessToken()).isEqualTo("dev-" + userId);
    }

    @Test
    void login_throwsUnauthorized_whenEmailNotFound() {
        LoginRequest req = new LoginRequest("nope@gmail.com", "secret");

        when(users.findByEmail("nope@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Invalid credentials");
                });
    }

    @Test
    void login_throwsUnauthorized_whenPasswordDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@gmail.com")
                .passwordHash("HASH")
                .createdAt(Instant.now())
                .build();

        LoginRequest req = new LoginRequest("test@gmail.com", "wrong");

        when(users.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Invalid credentials");
                });
    }

    @Test
    void meFromDevToken_returnsMeResponse_whenHeaderValid() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@gmail.com")
                .passwordHash("HASH")
                .createdAt(Instant.now())
                .build();

        String header = "Bearer dev-" + userId;

        when(users.findById(userId)).thenReturn(Optional.of(user));

        userResponse me = authService.getUserDevToken(header);

        assertThat(me.userId()).isEqualTo(userId);
        assertThat(me.email()).isEqualTo("test@gmail.com");
    }

    @Test
    void meFromDevToken_throwsUnauthorized_whenHeaderMissing() {
        assertThatThrownBy(() -> authService.getUserDevToken(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Missing or invalid token");
                });
    }

    @Test
    void meFromDevToken_throwsUnauthorized_whenNoBearerPrefix() {
        String header = "dev-1234";

        assertThatThrownBy(() -> authService.getUserDevToken(header))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Missing or invalid token");
                });
    }

    @Test
    void meFromDevToken_throwsUnauthorized_whenNoDevPrefix() {
        String header = "Bearer uhhh-uhh";

        assertThatThrownBy(() -> authService.getUserDevToken(header))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Invalid token format");
                });
    }

    @Test
    void meFromDevToken_throwsUnauthorized_whenInvalidUuid() {
        String header = "Bearer dev-uhhh-uhhh";

        assertThatThrownBy(() -> authService.getUserDevToken(header))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Invalid token UUID");
                });
    }

    @Test
    void meFromDevToken_throwsUnauthorized_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        String header = "Bearer dev-" + userId;

        when(users.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserDevToken(header))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("User not found");
                });
    }
}

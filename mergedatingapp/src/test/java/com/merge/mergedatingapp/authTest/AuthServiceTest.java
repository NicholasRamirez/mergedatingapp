package com.merge.mergedatingapp.authTest;

import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.AuthToken;
import com.merge.mergedatingapp.auth.AuthTokenRepository;
import com.merge.mergedatingapp.auth.dto.LoginRequest;
import com.merge.mergedatingapp.auth.dto.UserResponse;
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
import java.time.temporal.ChronoUnit;
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

    @Mock
    AuthTokenRepository tokens;

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
    void login_returnsTokenUuid_whenCredentialsValid() {
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

        UUID tokenId = UUID.randomUUID();
        Instant now = Instant.now();
        AuthToken savedToken = AuthToken.builder()
                .id(tokenId)
                .userId(userId)
                .createdAt(now)
                .expiresAt(now.plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        when(tokens.save(any(AuthToken.class))).thenReturn(savedToken);

        TokenResponse resp = authService.login(req);

        assertThat(resp.accessToken()).isEqualTo(tokenId.toString());
        verify(tokens).save(any(AuthToken.class));
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
    void getUserFromToken_returnsUserResponse_whenHeaderValid() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email("test@gmail.com")
                .passwordHash("HASH")
                .createdAt(Instant.now())
                .build();

        Instant now = Instant.now();
        AuthToken token = AuthToken.builder()
                .id(tokenId)
                .userId(userId)
                .createdAt(now.minus(1, ChronoUnit.HOURS))
                .expiresAt(now.plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        String header = "Bearer " + tokenId;

        when(tokens.findByIdAndRevokedFalse(tokenId)).thenReturn(Optional.of(token));
        when(users.findById(userId)).thenReturn(Optional.of(user));

        UserResponse me = authService.getUserFromToken(header);

        assertThat(me.userId()).isEqualTo(userId);
        assertThat(me.email()).isEqualTo("test@gmail.com");
    }

    @Test
    void getUserFromToken_throwsUnauthorized_whenHeaderMissing() {
        assertThatThrownBy(() -> authService.getUserFromToken(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Missing or invalid token");
                });
    }

    @Test
    void getUserFromToken_throwsUnauthorized_whenNoBearerPrefix() {
        String header = "1234-NotBearer";

        assertThatThrownBy(() -> authService.getUserFromToken(header))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Missing or invalid token");
                });
    }

    @Test
    void getUserFromToken_throwsUnauthorized_whenInvalidUuidFormat() {
        String header = "Bearer not-a-uuid";

        assertThatThrownBy(() -> authService.getUserFromToken(header))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Invalid token format");
                });
    }

    @Test
    void getUserFromToken_throwsUnauthorized_whenTokenNotFound() {
        UUID tokenId = UUID.randomUUID();
        String header = "Bearer " + tokenId;

        when(tokens.findByIdAndRevokedFalse(tokenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserFromToken(header))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Token not found");
                });
    }

    @Test
    void getUserFromToken_throwsUnauthorized_whenTokenExpired() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        Instant now = Instant.now();
        AuthToken token = AuthToken.builder()
                .id(tokenId)
                .userId(userId)
                .createdAt(now.minus(10, ChronoUnit.DAYS))
                .expiresAt(now.minus(1, ChronoUnit.MINUTES))  // already expired
                .revoked(false)
                .build();

        String header = "Bearer " + tokenId;

        when(tokens.findByIdAndRevokedFalse(tokenId)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.getUserFromToken(header))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Token expired");
                });
    }

    @Test
    void getUserFromToken_throwsUnauthorized_whenUserNotFound() {
        UUID userId   = UUID.randomUUID();
        UUID tokenId  = UUID.randomUUID();
        Instant now   = Instant.now();

        AuthToken token = AuthToken.builder()
                .id(tokenId)
                .userId(userId)
                .createdAt(now)
                .expiresAt(now.plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        String header = "Bearer " + tokenId;

        when(tokens.findByIdAndRevokedFalse(tokenId)).thenReturn(Optional.of(token));
        when(users.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserFromToken(header))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("User not found");
                });
    }
}

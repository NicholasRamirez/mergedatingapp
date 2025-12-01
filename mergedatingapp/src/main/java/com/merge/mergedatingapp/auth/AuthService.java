package com.merge.mergedatingapp.auth;

import com.merge.mergedatingapp.auth.dto.*;
import com.merge.mergedatingapp.users.User;
import com.merge.mergedatingapp.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenRepository tokens;

    public void register(RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        var user = User.builder()
                .email(req.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .createdAt(Instant.now())
                .build();
        users.save(user);
    }

    // REAL token: row in DB, UUID returned to client
    public TokenResponse login(LoginRequest req) {
        var user = users.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        var now = Instant.now();
        var token = AuthToken.builder()
                .userId(user.getId())
                .createdAt(now)
                .expiresAt(now.plus(7, ChronoUnit.DAYS))   // 7-day token for now
                .build();

        var saved = tokens.save(token);

        // This is what the frontend stores and sends back as Bearer <token>
        return new TokenResponse(saved.getId().toString());
    }

    // Parse Bearer <uuid> and look up token in DB
    public UserResponse getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String raw = authHeader.substring("Bearer ".length()).trim();

        UUID tokenId;
        try {
            tokenId = UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token format");
        }

        var token = tokens.findByIdAndRevokedFalse(tokenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token not found"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired");
        }

        var user = users.findById(token.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String raw = authHeader.substring("Bearer ".length()).trim();
        UUID tokenId;

        try {
            tokenId = UUID.fromString(raw);
        } catch (IllegalArgumentException e) {throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Invalid token format");
        }

        var token = tokens.findByIdAndRevokedFalse(tokenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Token not found or already revoked"));

        token.setRevoked(true);
        tokens.save(token);
    }
}

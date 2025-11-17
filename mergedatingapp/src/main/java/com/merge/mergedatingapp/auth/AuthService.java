package com.merge.mergedatingapp.auth;

import com.merge.mergedatingapp.auth.dto.*;
import com.merge.mergedatingapp.users.User;
import com.merge.mergedatingapp.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

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

    // For now: return a simple dev token "dev-<userId>"
    public TokenResponse login(LoginRequest req) {
        var user = users.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = "dev-" + user.getId();
        return new TokenResponse(token);
    }

    // Parse our dev token and return user info
    public userResponse getUserDevToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String token = authHeader.substring("Bearer ".length()).trim(); // e.g., "dev-<uuid>"
        if (!token.startsWith("dev-")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token format");
        }

        String idStr = token.substring("dev-".length()); // just the UUID
        UUID userId;
        try {
            userId = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token UUID");
        }

        var user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return new userResponse(user.getId(), user.getEmail());
    }
}

package com.merge.mergedatingapp.auth;

import com.merge.mergedatingapp.auth.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// REST controller for authentication related endpoints:
// register, login, get user from token, logout

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest req) {
        auth.register(req);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    // Resolve the current user from the Authorization Bearer token.
    @GetMapping("/user")
    public UserResponse user(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return auth.getUserFromToken(authHeader);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        auth.logout(authHeader);
    }
}
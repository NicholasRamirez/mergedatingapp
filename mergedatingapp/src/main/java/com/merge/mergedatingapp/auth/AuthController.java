package com.merge.mergedatingapp.auth;

import com.merge.mergedatingapp.auth.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    // For now, reads our "dev-<uuid>" token from Authorization header
    @GetMapping("/me")
    public MeResponse me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return auth.meFromDevToken(authHeader);
    }
}
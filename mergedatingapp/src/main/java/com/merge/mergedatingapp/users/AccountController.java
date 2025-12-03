package com.merge.mergedatingapp.users;

import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AuthService authService;
    private final AccountCommandService commands;

    // DELETE /api/account  (delete *my* account)
    @DeleteMapping
    public void deleteMyAccount(@RequestHeader("Authorization") String authHeader) {
        UserResponse me = authService.getUserFromToken(authHeader);
        commands.deleteAccount(me.userId());
    }

    // POST /api/account/block  (block another user)
    @PostMapping("/block")
    public void blockUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BlockUserRequest req
    ) {
        UserResponse me = authService.getUserFromToken(authHeader);
        commands.blockUser(me.userId(), req.blockedUserId());
    }

    public record BlockUserRequest(UUID blockedUserId) {}
}

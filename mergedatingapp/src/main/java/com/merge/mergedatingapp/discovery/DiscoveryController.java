package com.merge.mergedatingapp.discovery;

import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.dto.UserResponse;
import com.merge.mergedatingapp.discovery.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DiscoveryController {

    private final AuthService auth;
    private final DiscoveryService svc;

    private UUID uid(String header) {
        UserResponse user = auth.getUserFromToken(header);
        return user.userId();
    }

    @GetMapping("/discovery/next")
    public CandidateCard next(@RequestHeader("Authorization") String h) {
        return svc.next(uid(h));
    }

    @PostMapping("/discovery/pass")
    public void pass(@RequestHeader("Authorization") String h,
                     @Valid @RequestBody PassRequest req) {
        svc.pass(uid(h), req.candidateUserId());
    }

    @PostMapping("/matching/like")
    public LikeResponse like(@RequestHeader("Authorization") String h,
                             @Valid @RequestBody LikeRequest req) {
        return svc.like(uid(h), req.likedUserId());
    }
}

package com.merge.mergedatingapp.profiles;

import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.dto.UserResponse;
import com.merge.mergedatingapp.profiles.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// REST API for viewing and editing user's profile, including photos and prompts

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService svc;
    private final AuthService auth; // to parse the dev token and get userId

    // Extracts userId from authorization header (Bearer token).
    private UUID userIdFromAuth(String header) {
        UserResponse user = auth.getUserFromToken(header);
        return user.userId();
    }

    @GetMapping("/user")
    public ProfileResponse user(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return svc.getMyProfile(userIdFromAuth(authHeader));
    }

    @PutMapping("/user")
    public ProfileResponse update(@RequestHeader(name = "Authorization", required = false) String authHeader,
                                  @Valid @RequestBody ProfileUpdateRequest req) {
        return svc.updateBasics(userIdFromAuth(authHeader), req);
    }

    @GetMapping("/user/photos")
    public List<String> myPhotos(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return svc.getMyProfile(userIdFromAuth(authHeader)).photos();
    }

    @PostMapping("/user/photos")
    public void addPhoto(@RequestHeader(name = "Authorization", required = false) String authHeader,
                         @Valid @RequestBody PhotoRequest req) {
        svc.addPhoto(userIdFromAuth(authHeader), req);
    }

    @DeleteMapping("/user/photos/{photoId}")
    public void deletePhoto(@RequestHeader(name = "Authorization", required = false) String authHeader,
                            @PathVariable UUID photoId) {
        svc.removePhoto(userIdFromAuth(authHeader), photoId);
    }

    @GetMapping("/user/prompts")
    public List<ProfileResponse.PromptQA> myPrompts(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return svc.getMyProfile(userIdFromAuth(authHeader)).prompts();
    }

    @PutMapping("/user/prompts")
    public void upsertPrompts(@RequestHeader(name = "Authorization", required = false) String authHeader,
                              @Valid @RequestBody List<PromptAnswerRequest> list) {
        svc.upsertPrompts(userIdFromAuth(authHeader), list);
    }
}

package com.merge.mergedatingapp.profiles;

import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.dto.MeResponse;
import com.merge.mergedatingapp.profiles.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService svc;
    private final AuthService auth; // to parse the dev token and get userId

    private UUID userIdFromAuth(String header) {
        MeResponse me = auth.meFromDevToken(header);
        return me.userId();
    }

    @GetMapping("/me")
    public ProfileResponse me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return svc.getMyProfile(userIdFromAuth(authHeader));
    }

    @PutMapping("/me")
    public ProfileResponse update(@RequestHeader(name = "Authorization", required = false) String authHeader,
                                  @Valid @RequestBody ProfileUpdateRequest req) {
        return svc.updateBasics(userIdFromAuth(authHeader), req);
    }

    @GetMapping("/me/photos")
    public List<String> myPhotos(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return svc.getMyProfile(userIdFromAuth(authHeader)).photos();
    }

    @PostMapping("/me/photos")
    public void addPhoto(@RequestHeader(name = "Authorization", required = false) String authHeader,
                         @Valid @RequestBody PhotoRequest req) {
        svc.addPhoto(userIdFromAuth(authHeader), req);
    }

    @DeleteMapping("/me/photos/{photoId}")
    public void deletePhoto(@RequestHeader(name = "Authorization", required = false) String authHeader,
                            @PathVariable UUID photoId) {
        svc.removePhoto(userIdFromAuth(authHeader), photoId);
    }

    @GetMapping("/me/prompts")
    public List<ProfileResponse.PromptQA> myPrompts(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return svc.getMyProfile(userIdFromAuth(authHeader)).prompts();
    }

    @PutMapping("/me/prompts")
    public void upsertPrompts(@RequestHeader(name = "Authorization", required = false) String authHeader,
                              @Valid @RequestBody List<PromptAnswerRequest> list) {
        svc.upsertPrompts(userIdFromAuth(authHeader), list);
    }
}

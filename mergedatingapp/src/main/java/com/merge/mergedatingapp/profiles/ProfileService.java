package com.merge.mergedatingapp.profiles;

import com.merge.mergedatingapp.profiles.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Logic for profile information: profiles, photos, prompts.
// Manages when a profile becomes discoverable by other users.

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profiles;
    private final PhotoRepository photos;
    private final PromptAnswerRepository prompts;

    // Get or create a blank profile for user
    @Transactional
    public Profile getOrCreateForUser(UUID userId) {
        return profiles.findByUserId(userId).orElseGet(() -> {
            Profile profile = Profile.builder()
                    .userId(userId)
                    .discoverable(false)
                    .lastUpdated(Instant.now())
                    .build();
            return profiles.save(profile);
        });
    }

    // Get current user's profile, photos, and prompts as a DTO.
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(UUID userId) {
        Profile profile = getOrCreateForUser(userId);

        var photoUrls = photos.findByProfileIdOrderByPositionAsc(profile.getId())
                .stream().map(Photo::getUrl).toList();

        var qas = prompts.findByProfileId(profile.getId())
                .stream().map(pa -> new ProfileResponse.PromptQA(pa.getQuestion(), pa.getAnswer()))
                .toList();

        return new ProfileResponse(profile.getId(), profile.getUserId(), profile.getName(), profile.getBirthday(),
                profile.getGender(), profile.getPronouns(), profile.getRelationshipIntent(), profile.getHeightCm(),
                profile.getCity(), profile.isDiscoverable(), photoUrls, qas);
    }

    // Updates basic profile information (name, city, etc).
    @Transactional
    public ProfileResponse updateBasics(UUID userId, ProfileUpdateRequest req) {
        Profile profile = getOrCreateForUser(userId);

        profile.setName(req.name());
        profile.setCity(req.city());
        profile.setBirthday(req.birthday());

        if (req.gender() != null) profile.setGender(req.gender());
        if (req.pronouns() != null) profile.setPronouns(req.pronouns());
        if (req.relationshipIntent() != null) profile.setRelationshipIntent(req.relationshipIntent());

        profile.setHeightCm(req.heightCm());
        profile.setLastUpdated(Instant.now());
        profiles.save(profile);

        recalcDiscoverable(profile);
        return getMyProfile(userId);
    }

    @Transactional
    public void addPhoto(UUID userId, PhotoRequest req) {
        Profile profile = getOrCreateForUser(userId);

        photos.save(Photo.builder()
                .profileId(profile.getId())
                .url(req.url())
                .position(req.position())
                .build());

        recalcDiscoverable(profile);
    }

    @Transactional
    public void removePhoto(UUID userId, UUID photoId) {
        Profile profile = getOrCreateForUser(userId);

        var photo = photos.findById(photoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));

        if (!photo.getProfileId().equals(profile.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your photo");

        photos.delete(photo);
        recalcDiscoverable(profile);
    }

    // Replace prompts with the given list and rechecks completion.
    @Transactional
    public void upsertPrompts(UUID userId, List<PromptAnswerRequest> list) {
        Profile profile = getOrCreateForUser(userId);

        prompts.deleteByProfileId(profile.getId());

        for (var request : list) {
            prompts.save(PromptAnswer.builder()
                    .profileId(profile.getId())
                    .question(request.question())
                    .answer(request.answer())
                    .build());
        }

        recalcDiscoverable(profile);
    }

    private void recalcDiscoverable(Profile profile) {
        long photoCount = photos.countByProfileId(profile.getId());
        long promptCount = prompts.countByProfileId(profile.getId());

        boolean complete = profile.getName() != null && profile.getCity() != null && profile.getBirthday() != null
                && photoCount >= 1 && promptCount >= 2;

        profile.setDiscoverable(complete);
        profile.setLastUpdated(Instant.now());
        profiles.save(profile);
    }
}

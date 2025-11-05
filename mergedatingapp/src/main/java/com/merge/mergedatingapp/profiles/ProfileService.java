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
            Profile p = Profile.builder()
                    .userId(userId)
                    .discoverable(false)
                    .lastUpdated(Instant.now())
                    .build();
            return profiles.save(p);
        });
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(UUID userId) {
        Profile p = getOrCreateForUser(userId);
        var photoUrls = photos.findByProfileIdOrderByPositionAsc(p.getId())
                .stream().map(Photo::getUrl).toList();
        var qas = prompts.findByProfileId(p.getId())
                .stream().map(pa -> new ProfileResponse.PromptQA(pa.getQuestion(), pa.getAnswer()))
                .toList();
        return new ProfileResponse(p.getId(), p.getUserId(), p.getName(), p.getBirthday(),
                p.getGender(), p.getPronouns(), p.getRelationshipIntent(), p.getHeightCm(),
                p.getCity(), p.isDiscoverable(), photoUrls, qas);
    }

    @Transactional
    public ProfileResponse updateBasics(UUID userId, ProfileUpdateRequest req) {
        Profile p = getOrCreateForUser(userId);
        p.setName(req.name());
        p.setCity(req.city());
        p.setBirthday(req.birthday());
        if (req.gender() != null) p.setGender(req.gender());
        if (req.pronouns() != null) p.setPronouns(req.pronouns());
        if (req.relationshipIntent() != null) p.setRelationshipIntent(req.relationshipIntent());
        p.setHeightCm(req.heightCm());
        p.setLastUpdated(Instant.now());
        profiles.save(p);
        recalcDiscoverable(p);
        return getMyProfile(userId);
    }

    @Transactional
    public void addPhoto(UUID userId, PhotoRequest req) {
        Profile p = getOrCreateForUser(userId);
        photos.save(Photo.builder()
                .profileId(p.getId())
                .url(req.url())
                .position(req.position())
                .build());
        recalcDiscoverable(p);
    }

    @Transactional
    public void removePhoto(UUID userId, UUID photoId) {
        Profile p = getOrCreateForUser(userId);
        var ph = photos.findById(photoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));
        if (!ph.getProfileId().equals(p.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your photo");
        photos.delete(ph);
        recalcDiscoverable(p);
    }

    @Transactional
    public void upsertPrompts(UUID userId, List<PromptAnswerRequest> list) {
        Profile p = getOrCreateForUser(userId);
        prompts.deleteByProfileId(p.getId());
        for (var r : list) {
            prompts.save(PromptAnswer.builder()
                    .profileId(p.getId())
                    .question(r.question())
                    .answer(r.answer())
                    .build());
        }
        recalcDiscoverable(p);
    }

    private void recalcDiscoverable(Profile p) {
        long photoCount = photos.countByProfileId(p.getId());
        long promptCount = prompts.countByProfileId(p.getId());
        boolean complete = p.getName() != null && p.getCity() != null && p.getBirthday() != null
                && photoCount >= 1 && promptCount >= 2;
        p.setDiscoverable(complete);
        p.setLastUpdated(Instant.now());
        profiles.save(p);
    }
}

package com.merge.mergedatingapp.profilesTest;

import com.merge.mergedatingapp.profiles.*;
import com.merge.mergedatingapp.profiles.dto.PhotoRequest;
import com.merge.mergedatingapp.profiles.dto.ProfileResponse;
import com.merge.mergedatingapp.profiles.dto.ProfileUpdateRequest;
import com.merge.mergedatingapp.profiles.dto.PromptAnswerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.merge.mergedatingapp.profiles.Enums.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profiles;

    @Mock
    private PhotoRepository photos;

    @Mock
    private PromptAnswerRepository prompts;

    @InjectMocks
    private ProfileService service;

    private UUID userId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
    }

    @Test
    void getOrCreateForUser_createsNewProfileWhenNoneExists() {
        when(profiles.findByUserId(userId)).thenReturn(Optional.empty());

        // when saving, return a profile with ids set
        when(profiles.save(any(Profile.class)))
                .thenAnswer(inv -> {
                    Profile p = inv.getArgument(0);
                    p.setId(profileId);
                    return p;
                });

        Profile result = service.getOrCreateForUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(profileId, result.getId());
        verify(profiles).save(any(Profile.class));
    }

    @Test
    void getOrCreateForUser_returnsExistingProfileIfPresent() {
        Profile existing = Profile.builder()
                .id(profileId)
                .userId(userId)
                .name("Kyle")
                .build();

        when(profiles.findByUserId(userId)).thenReturn(Optional.of(existing));

        Profile result = service.getOrCreateForUser(userId);

        assertSame(existing, result);
        verify(profiles, never()).save(any());
    }

    @Test
    void getMyProfile_returnsAggregatedProfile() {
        Profile p = Profile.builder()
                .id(profileId)
                .userId(userId)
                .name("Jess")
                .city("Los Angeles")
                .birthday(LocalDate.of(2000, 1, 1))
                .gender(GenderType.FEMALE)
                .pronouns(PronounsType.SHE_HER)
                .relationshipIntent(RelationshipIntentType.LONG_TERM)
                .heightCm(165)
                .discoverable(true)
                .lastUpdated(Instant.now())
                .build();

        when(profiles.findByUserId(userId)).thenReturn(Optional.of(p));

        var photo1 = Photo.builder().id(UUID.randomUUID()).profileId(profileId).url("u1").position(0).build();
        var photo2 = Photo.builder().id(UUID.randomUUID()).profileId(profileId).url("u2").position(1).build();
        when(photos.findByProfileIdOrderByPositionAsc(profileId)).thenReturn(List.of(photo1, photo2));

        var pa1 = PromptAnswer.builder().id(UUID.randomUUID()).profileId(profileId).question("Q1").answer("A1").build();
        var pa2 = PromptAnswer.builder().id(UUID.randomUUID()).profileId(profileId).question("Q2").answer("A2").build();
        when(prompts.findByProfileId(profileId)).thenReturn(List.of(pa1, pa2));

        ProfileResponse resp = service.getMyProfile(userId);

        assertEquals(profileId, resp.profileId());
        assertEquals(userId, resp.userId());
        assertEquals("Jess", resp.name());
        assertEquals("Los Angeles", resp.city());
        assertEquals(2, resp.photos().size());
        assertEquals(List.of("u1", "u2"), resp.photos());
        assertEquals(2, resp.prompts().size());
        assertEquals("Q1", resp.prompts().get(0).question());
        assertEquals("A1", resp.prompts().get(0).answer());
    }

    @Test
    void updateBasics_updatesFields_andRecalculatesDiscoverable() {
        Profile p = Profile.builder()
                .id(profileId)
                .userId(userId)
                .discoverable(false)
                .lastUpdated(Instant.now())
                .build();

        when(profiles.findByUserId(userId)).thenReturn(Optional.of(p));
        when(profiles.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1 photo + 2 prompts => should become discoverable after update
        when(photos.countByProfileId(profileId)).thenReturn(1L);
        when(prompts.countByProfileId(profileId)).thenReturn(2L);

        ProfileUpdateRequest req = new ProfileUpdateRequest(
                "Kyle",
                "Los Angeles",
                LocalDate.of(1999, 5, 10),
                GenderType.MALE,
                PronounsType.HE_HIM,
                RelationshipIntentType.LONG_TERM,
                180
        );

        ProfileResponse resp = service.updateBasics(userId, req);

        assertEquals("Kyle", resp.name());
        assertEquals("Los Angeles", resp.city());
        assertEquals(LocalDate.of(1999, 5, 10), resp.birthday());
        assertEquals(GenderType.MALE, resp.gender());
        assertEquals(PronounsType.HE_HIM, resp.pronouns());
        assertEquals(RelationshipIntentType.LONG_TERM, resp.relationshipIntent());
        assertEquals(180, resp.heightCm());
        assertTrue(resp.discoverable(), "Profile should be discoverable when all requirements met");

        verify(profiles, atLeastOnce()).save(any(Profile.class));
        verify(photos).countByProfileId(profileId);
        verify(prompts).countByProfileId(profileId);
    }

    @Test
    void addPhoto_savesPhoto_andRecalculatesDiscoverable() {
        Profile p = Profile.builder()
                .id(profileId)
                .userId(userId)
                .name("Name")
                .city("City")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        when(profiles.findByUserId(userId)).thenReturn(Optional.of(p));
        when(photos.save(any(Photo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profiles.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(photos.countByProfileId(profileId)).thenReturn(1L);
        when(prompts.countByProfileId(profileId)).thenReturn(2L);

        PhotoRequest req = new PhotoRequest("http://x", 0);

        assertDoesNotThrow(() -> service.addPhoto(userId, req));

        verify(photos).save(any(Photo.class));
        verify(photos).countByProfileId(profileId);
        verify(prompts).countByProfileId(profileId);
    }

    @Test
    void removePhoto_throwsNotFound_whenPhotoMissing() {
        Profile p = Profile.builder()
                .id(profileId)
                .userId(userId)
                .build();

        when(profiles.findByUserId(userId)).thenReturn(Optional.of(p));
        when(photos.findById(any())).thenReturn(Optional.empty());

        UUID photoId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.removePhoto(userId, photoId)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void removePhoto_throwsForbidden_whenPhotoBelongsToAnotherProfile() {
        Profile myProfile = Profile.builder()
                .id(profileId)
                .userId(userId)
                .build();

        UUID otherProfileId = UUID.randomUUID();
        Photo othersPhoto = Photo.builder()
                .id(UUID.randomUUID())
                .profileId(otherProfileId)
                .url("x")
                .position(0)
                .build();

        when(profiles.findByUserId(userId)).thenReturn(Optional.of(myProfile));
        when(photos.findById(othersPhoto.getId())).thenReturn(Optional.of(othersPhoto));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.removePhoto(userId, othersPhoto.getId())
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void removePhoto_deletesPhoto_andRecalculatesDiscoverable() {
        Profile p = Profile.builder()
                .id(profileId)
                .userId(userId)
                .build();

        Photo ph = Photo.builder()
                .id(UUID.randomUUID())
                .profileId(profileId)
                .url("x")
                .position(0)
                .build();

        when(profiles.findByUserId(userId)).thenReturn(Optional.of(p));
        when(photos.findById(ph.getId())).thenReturn(Optional.of(ph));
        when(profiles.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(photos.countByProfileId(profileId)).thenReturn(0L);
        when(prompts.countByProfileId(profileId)).thenReturn(0L);

        assertDoesNotThrow(() -> service.removePhoto(userId, ph.getId()));

        verify(photos).delete(ph);
        verify(photos).countByProfileId(profileId);
        verify(prompts).countByProfileId(profileId);
    }

    @Test
    void upsertPrompts_replacesPrompts_andRecalculatesDiscoverable() {
        Profile p = Profile.builder()
                .id(profileId)
                .userId(userId)
                .name("Name")
                .city("City")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        when(profiles.findByUserId(userId)).thenReturn(Optional.of(p));
        when(prompts.save(any(PromptAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profiles.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(photos.countByProfileId(profileId)).thenReturn(1L);
        when(prompts.countByProfileId(profileId)).thenReturn(2L);

        List<PromptAnswerRequest> list = List.of(
                new PromptAnswerRequest("Q1", "A1"),
                new PromptAnswerRequest("Q2", "A2")
        );

        assertDoesNotThrow(() -> service.upsertPrompts(userId, list));

        verify(prompts).deleteByProfileId(profileId);
        verify(prompts, times(list.size())).save(any(PromptAnswer.class));
        verify(photos).countByProfileId(profileId);
        verify(prompts).countByProfileId(profileId);
    }
}
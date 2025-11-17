package com.merge.mergedatingapp.profilesTest;

import com.merge.mergedatingapp.profiles.*;
import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.dto.userResponse;
import com.merge.mergedatingapp.profiles.dto.PhotoRequest;
import com.merge.mergedatingapp.profiles.dto.ProfileResponse;
import com.merge.mergedatingapp.profiles.dto.ProfileUpdateRequest;
import com.merge.mergedatingapp.profiles.dto.PromptAnswerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.merge.mergedatingapp.profiles.Enums.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String AUTH_HEADER = "Bearer dev-" + USER_ID;

    @Mock
    private ProfileService profileService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ProfileController controller;

    private ProfileResponse sampleProfile() {
        return new ProfileResponse(
                UUID.randomUUID(),
                USER_ID,
                "Jess",
                LocalDate.of(2002, 5, 12),
                GenderType.FEMALE,
                PronounsType.SHE_HER,
                RelationshipIntentType.LONG_TERM,
                165,
                "Los Angeles",
                true,
                List.of("https://picsum.photos/800/1000?random=42"),
                List.of(new ProfileResponse.PromptQA("Tabs or spaces?", "Spaces"))
        );
    }

    @Test
    void user_returnsProfileForCurrentUser() {

        var profile = sampleProfile();
        when(authService.getUserDevToken(AUTH_HEADER))
                .thenReturn(new userResponse(USER_ID, "test@gmail.com"));
        when(profileService.getMyProfile(USER_ID)).thenReturn(profile);

        ProfileResponse result = controller.user(AUTH_HEADER);

        assertThat(result).isEqualTo(profile);
        verify(authService).getUserDevToken(AUTH_HEADER);
        verify(profileService).getMyProfile(USER_ID);
    }

    @Test
    void update_callsServiceAndReturnsUpdatedProfile() {

        var req = new ProfileUpdateRequest(
                "Kyle",
                "Los Angeles",
                LocalDate.of(2002, 1, 23),
                GenderType.MALE,
                PronounsType.HE_HIM,
                RelationshipIntentType.SHORT_TERM,
                180
        );
        var updated = new ProfileResponse(
                UUID.randomUUID(),
                USER_ID,
                req.name(),
                req.birthday(),
                req.gender(),
                req.pronouns(),
                req.relationshipIntent(),
                req.heightCm(),
                req.city(),
                true,
                List.of(),
                List.of()
        );

        when(authService.getUserDevToken(AUTH_HEADER))
                .thenReturn(new userResponse(USER_ID, "test@gmail.com"));
        when(profileService.updateBasics(USER_ID, req)).thenReturn(updated);

        ProfileResponse result = controller.update(AUTH_HEADER, req);

        assertThat(result).isEqualTo(updated);
        verify(authService).getUserDevToken(AUTH_HEADER);
        verify(profileService).updateBasics(USER_ID, req);
    }

    @Test
    void photos_returnsPhotoUrls() {
        var profile = sampleProfile();

        when(authService.getUserDevToken(AUTH_HEADER))
                .thenReturn(new userResponse(USER_ID, "test@gmail.com"));
        when(profileService.getMyProfile(USER_ID)).thenReturn(profile);

        var photos = controller.myPhotos(AUTH_HEADER);

        assertThat(photos).containsExactlyElementsOf(profile.photos());
        verify(profileService).getMyProfile(USER_ID);
    }

    @Test
    void addPhoto_delegatesToService() {
        var req = new PhotoRequest("https://picsum.photos/800/1000?random=10", 0);

        when(authService.getUserDevToken(AUTH_HEADER))
                .thenReturn(new userResponse(USER_ID, "test@gmail.com"));

        controller.addPhoto(AUTH_HEADER, req);

        verify(profileService).addPhoto(USER_ID, req);
    }

    @Test
    void deletePhoto_delegatesToService() {
        UUID photoId = UUID.randomUUID();

        when(authService.getUserDevToken(AUTH_HEADER))
                .thenReturn(new userResponse(USER_ID, "test@gmail.com"));

        controller.deletePhoto(AUTH_HEADER, photoId);

        verify(profileService).removePhoto(USER_ID, photoId);
    }

    @Test
    void prompts_returnsPromptQAs() {
        var profile = sampleProfile();

        when(authService.getUserDevToken(AUTH_HEADER))
                .thenReturn(new userResponse(USER_ID, "test@gmail.com"));
        when(profileService.getMyProfile(USER_ID)).thenReturn(profile);

        // when
        var prompts = controller.myPrompts(AUTH_HEADER);

        // then
        assertThat(prompts).containsExactlyElementsOf(profile.prompts());
        verify(profileService).getMyProfile(USER_ID);
    }

    @Test
    void upsertPrompts_delegatesToService() {
        var list = List.of(
                new PromptAnswerRequest("Q1", "A1"),
                new PromptAnswerRequest("Q2", "A2")
        );

        when(authService.getUserDevToken(AUTH_HEADER))
                .thenReturn(new userResponse(USER_ID, "test@gmail.com"));

        // when
        controller.upsertPrompts(AUTH_HEADER, list);

        // then
        verify(profileService).upsertPrompts(USER_ID, list);
    }
}

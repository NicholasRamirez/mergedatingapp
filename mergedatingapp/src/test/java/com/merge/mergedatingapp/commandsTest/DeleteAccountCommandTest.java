package com.merge.mergedatingapp.commandsTest;

import com.merge.mergedatingapp.auth.AuthTokenRepository;
import com.merge.mergedatingapp.chat.ChatMessageRepository;
import com.merge.mergedatingapp.commands.DeleteAccountCommand;
import com.merge.mergedatingapp.discovery.ChatThread;
import com.merge.mergedatingapp.discovery.ChatThreadRepository;
import com.merge.mergedatingapp.discovery.LikeRepository;
import com.merge.mergedatingapp.discovery.Match;
import com.merge.mergedatingapp.discovery.MatchRepository;
import com.merge.mergedatingapp.discovery.SeenCandidateRepository;
import com.merge.mergedatingapp.profiles.PhotoRepository;
import com.merge.mergedatingapp.profiles.Profile;
import com.merge.mergedatingapp.profiles.ProfileRepository;
import com.merge.mergedatingapp.profiles.PromptAnswerRepository;
import com.merge.mergedatingapp.users.BlockedUserRepository;
import com.merge.mergedatingapp.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteAccountCommandTest {

    @Mock ProfileRepository profiles;
    @Mock PhotoRepository photos;
    @Mock PromptAnswerRepository prompts;
    @Mock SeenCandidateRepository seenRepo;
    @Mock LikeRepository likeRepo;
    @Mock MatchRepository matchRepo;
    @Mock ChatThreadRepository chatRepo;
    @Mock ChatMessageRepository messageRepo;
    @Mock AuthTokenRepository tokenRepo;
    @Mock UserRepository users;
    @Mock BlockedUserRepository blockedRepo;

    private UUID userId;
    private UUID profileId;
    private UUID matchId;
    private UUID threadId;

    private Profile profile;
    private Match match;
    private ChatThread thread;

    private DeleteAccountCommand command;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        matchId = UUID.randomUUID();
        threadId = UUID.randomUUID();

        // Fake profile for this user
        profile = Profile.builder()
                .id(profileId)
                .userId(userId)
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .lastUpdated(Instant.now())
                .build();
        when(profiles.findByUserId(userId)).thenReturn(Optional.of(profile));

        // Fake match involving this user
        match = Match.builder()
                .id(matchId)
                .userA(userId)
                .userB(UUID.randomUUID())
                .createdAt(Instant.now())
                .active(true)
                .build();
        when(matchRepo.findByUserAOrUserB(userId, userId))
                .thenReturn(List.of(match));

        // Fake chat thread for that match
        thread = ChatThread.builder()
                .id(threadId)
                .matchId(matchId)
                .createdAt(Instant.now())
                .archived(false)
                .build();
        when(chatRepo.findByMatchId(matchId))
                .thenReturn(Optional.of(thread));

        // Construct command with all repositories
        command = new DeleteAccountCommand(
                profiles,
                photos,
                prompts,
                seenRepo,
                likeRepo,
                matchRepo,
                chatRepo,
                messageRepo,
                blockedRepo,
                tokenRepo,
                users
        );
    }

    @Test
    void execute_deletesUserAndRelatedData() {
        // Act
        command.execute(userId);

        // Profile + prompts + photos
        verify(prompts).deleteByProfileId(profileId);
        verify(photos).deleteByProfileId(profileId);
        verify(profiles).delete(profile);

        // Discovery / likes
        verify(seenRepo).deleteByViewerUserId(userId);
        verify(seenRepo).deleteByCandidateUserId(userId);
        verify(likeRepo).deleteByLikerId(userId);
        verify(likeRepo).deleteByLikedId(userId);

        // Matches + threads + messages
        verify(matchRepo).findByUserAOrUserB(userId, userId);
        verify(chatRepo).findByMatchId(matchId);
        verify(messageRepo).deleteByThreadId(threadId);
        verify(chatRepo).delete(thread);
        verify(matchRepo).delete(match);

        // Tokens + user
        verify(tokenRepo).deleteByUserId(userId);
        verify(users).deleteById(userId);
    }
}

package com.merge.mergedatingapp.discoveryTest;

import com.merge.mergedatingapp.discovery.*;
import com.merge.mergedatingapp.discovery.dto.CandidateCard;
import com.merge.mergedatingapp.discovery.dto.LikeResponse;
import com.merge.mergedatingapp.discovery.events.MatchCreatedEvent;
import com.merge.mergedatingapp.profiles.*;
import com.merge.mergedatingapp.profiles.Enums.*;
import com.merge.mergedatingapp.users.BlockedUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscoveryServiceTest {

    @Mock private ProfileRepository profiles;
    @Mock private PhotoRepository photos;
    @Mock private PromptAnswerRepository prompts;
    @Mock private SeenCandidateRepository seenRepo;
    @Mock private LikeRepository likeRepo;
    @Mock private MatchRepository matchRepo;
    @Mock private ChatThreadRepository chatRepo;
    @Mock private BlockedUserRepository blockedRepo;
    @Mock private CandidateOrderingStrategy orderingStrategy;
    @Mock private ApplicationEventPublisher events;

    @InjectMocks
    private DiscoveryService service;

    private UUID viewerId;
    private UUID candidateUserId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        viewerId = UUID.randomUUID();
        candidateUserId = UUID.randomUUID();
        profileId = UUID.randomUUID();
    }

    private Profile buildCandidateProfile() {
        return Profile.builder()
                .id(profileId)
                .userId(candidateUserId)
                .name("Jess")
                .city("Los Angeles")
                .birthday(LocalDate.of(2000, 5, 12))
                .gender(GenderType.FEMALE)
                .pronouns(PronounsType.SHE_HER)
                .relationshipIntent(RelationshipIntentType.LONG_TERM)
                .heightCm(165)
                .discoverable(true)
                .lastUpdated(Instant.now())
                .build();
    }

    @Test
    void next_returnsFirstUnseenDiscoverableCandidate() {
        Profile candidate = buildCandidateProfile();

        // profiles: contains our candidate
        when(profiles.findAll()).thenReturn(List.of(candidate));

        // viewer has not seen candidate yet
        when(seenRepo.existsByViewerUserIdAndCandidateUserId(viewerId, candidateUserId))
                .thenReturn(false);

        // Viewer has not blocked candidate
        when(blockedRepo.existsByBlockerIdAndBlockedId(viewerId, candidateUserId))
                .thenReturn(false);

        // photos + prompts
        when(photos.findByProfileIdOrderByPositionAsc(profileId))
                .thenReturn(List.of(
                        Photo.builder().url("https://example.com/p1.jpg").position(0).build()
                ));

        when(prompts.findByProfileId(profileId))
                .thenReturn(List.of(
                        PromptAnswer.builder().question("Tabs or spaces?").answer("Spaces").build(),
                        PromptAnswer.builder().question("Favorite algorithm?").answer("Dijkstra").build()
                ));

        when(orderingStrategy.orderCandidates(anyList(), any()))
                .thenAnswer(inv -> inv.getArgument(0)); // first arg = List<Profile>

        CandidateCard card = service.next(viewerId);

        assertThat(card.userId()).isEqualTo(candidateUserId);
        assertThat(card.name()).isEqualTo("Jess");
        assertThat(card.city()).isEqualTo("Los Angeles");
        assertThat(card.photos()).containsExactly("https://example.com/p1.jpg");
        assertThat(card.prompts()).hasSize(2);
        assertThat(card.genderType()).isEqualTo(GenderType.FEMALE);
        assertThat(card.pronounsType()).isEqualTo(PronounsType.SHE_HER);
        assertThat(card.relationshipIntent()).isEqualTo(RelationshipIntentType.LONG_TERM);
        assertThat(card.heightCm()).isEqualTo(165);

        verify(seenRepo, atLeastOnce())
                .existsByViewerUserIdAndCandidateUserId(viewerId, candidateUserId);
        verify(orderingStrategy).orderCandidates(anyList(), eq(viewerId));
    }

    @Test
    void next_skipsSeenCandidatesAndThrowsWhenNoneLeft() {
        Profile candidate = buildCandidateProfile();

        when(profiles.findAll()).thenReturn(List.of(candidate));
        // already seen => should be skipped and we end up with no candidates
        when(seenRepo.existsByViewerUserIdAndCandidateUserId(viewerId, candidateUserId))
                .thenReturn(true);

        when(orderingStrategy.orderCandidates(anyList(), any()))
                .thenAnswer(inv -> inv.getArgument(0)); // first arg = List<Profile>

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.next(viewerId));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(ex.getReason()).isEqualTo("No more candidates");
    }

    @Test
    void pass_createsNewSeenCandidateIfNoneExists() {
        when(seenRepo.findByViewerUserIdAndCandidateUserId(viewerId, candidateUserId))
                .thenReturn(Optional.empty());

        service.pass(viewerId, candidateUserId);

        ArgumentCaptor<SeenCandidate> captor = ArgumentCaptor.forClass(SeenCandidate.class);
        verify(seenRepo).save(captor.capture());

        SeenCandidate saved = captor.getValue();
        assertThat(saved.getViewerUserId()).isEqualTo(viewerId);
        assertThat(saved.getCandidateUserId()).isEqualTo(candidateUserId);
        assertThat(saved.getDecision()).isEqualTo(SeenCandidate.Decision.PASS);
    }

    @Test
    void pass_updatesExistingSeenCandidate() {
        SeenCandidate existing = SeenCandidate.builder()
                .viewerUserId(viewerId)
                .candidateUserId(candidateUserId)
                .decision(SeenCandidate.Decision.NONE)
                .build();

        when(seenRepo.findByViewerUserIdAndCandidateUserId(viewerId, candidateUserId))
                .thenReturn(Optional.of(existing));

        service.pass(viewerId, candidateUserId);

        assertThat(existing.getDecision()).isEqualTo(SeenCandidate.Decision.PASS);
        verify(seenRepo).save(existing);
    }

    @Test
    void like_whenNotMutual_returnsLikedAndDoesNotCreateMatch() {
        UUID liker = viewerId;
        UUID liked = candidateUserId;

        when(seenRepo.findByViewerUserIdAndCandidateUserId(liker, liked))
                .thenReturn(Optional.empty());
        when(likeRepo.existsByLikerIdAndLikedId(liker, liked)).thenReturn(false);
        // mutual check
        when(likeRepo.existsByLikerIdAndLikedId(liked, liker)).thenReturn(false);

        LikeResponse resp = service.like(liker, liked);

        assertThat(resp.status()).isEqualTo("LIKED");
        assertThat(resp.matchId()).isNull();
        assertThat(resp.threadId()).isNull();

        verify(likeRepo).save(any(LikeEntity.class));
        verify(matchRepo, never()).save(any());
        verify(chatRepo, never()).save(any());
    }

    @Test
    void like_whenMutual_createsMatchAndThreadAndReturnsMatched() {
        UUID liker = viewerId;
        UUID liked = candidateUserId;

        when(seenRepo.findByViewerUserIdAndCandidateUserId(liker, liked))
                .thenReturn(Optional.empty());
        when(likeRepo.existsByLikerIdAndLikedId(liker, liked)).thenReturn(false);
        // mutual this time
        when(likeRepo.existsByLikerIdAndLikedId(liked, liker)).thenReturn(true);

        // no existing match => save a new one
        UUID matchId = UUID.randomUUID();
        Match savedMatch = Match.builder()
                .id(matchId)
                .userA(liker.compareTo(liked) < 0 ? liker : liked)
                .userB(liker.compareTo(liked) < 0 ? liked : liker)
                .build();

        when(matchRepo.findByUserAAndUserB(any(), any())).thenReturn(Optional.empty());
        when(matchRepo.save(any(Match.class))).thenReturn(savedMatch);

        // no existing thread => save a new one
        UUID threadId = UUID.randomUUID();
        ChatThread savedThread = ChatThread.builder()
                .id(threadId)
                .matchId(matchId)
                .build();

        when(chatRepo.findByMatchId(matchId)).thenReturn(Optional.empty());
        when(chatRepo.save(any(ChatThread.class))).thenReturn(savedThread);

        LikeResponse resp = service.like(liker, liked);

        assertThat(resp.status()).isEqualTo("MATCHED");
        assertThat(resp.matchId()).isEqualTo(matchId);
        assertThat(resp.threadId()).isEqualTo(threadId);

        verify(matchRepo).save(any(Match.class));
        verify(chatRepo).save(any(ChatThread.class));
        verify(events).publishEvent(any(MatchCreatedEvent.class));
    }
}
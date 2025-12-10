package com.merge.mergedatingapp.discovery;

import com.merge.mergedatingapp.discovery.dto.*;
import com.merge.mergedatingapp.profiles.*;
import com.merge.mergedatingapp.discovery.events.*;
import com.merge.mergedatingapp.users.BlockedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DiscoveryService {

    private final ProfileRepository profiles;
    private final PhotoRepository photos;
    private final PromptAnswerRepository prompts;
    private final SeenCandidateRepository seenRepo;
    private final LikeRepository likeRepo;
    private final MatchRepository matchRepo;
    private final ChatThreadRepository chatRepo;

    private final BlockedUserRepository blockedRepo;
    private final CandidateOrderingStrategy candidateOrderingStrategy;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public CandidateCard next(UUID viewerUserId) {

        // Load all discoverable profiles that are NOT the viewer
        var discoverable = profiles.findAll().stream()
                .filter(profile -> profile.isDiscoverable())
                .filter(profile -> !profile.getUserId().equals(viewerUserId))
                .filter(profile -> !blockedRepo.existsByBlockerIdAndBlockedId(viewerUserId, profile.getUserId())
                        && !blockedRepo.existsByBlockerIdAndBlockedId(profile.getUserId(), viewerUserId))
                .filter(profile -> !seenRepo.existsByViewerUserIdAndCandidateUserId(viewerUserId, profile.getUserId()))
                .toList();

        // Delegate ordering to Strategy
        var ordered = candidateOrderingStrategy.orderCandidates(discoverable, viewerUserId);

        // Iterate in that order, skipping ones already seen
        for (var profile : ordered) {
            boolean alreadySeen = seenRepo.existsByViewerUserIdAndCandidateUserId(viewerUserId, profile.getUserId());
            if (alreadySeen) continue;

            var photoUrls = photos.findByProfileIdOrderByPositionAsc(profile.getId())
                    .stream().map(Photo::getUrl).toList();

            var qas = prompts.findByProfileId(profile.getId()).stream()
                    .limit(2)
                    .map(pa -> new CandidateCard.PromptQA(pa.getQuestion(), pa.getAnswer()))
                    .toList();

            return new CandidateCard(
                    profile.getUserId(),
                    profile.getName(),
                    profile.getCity(),
                    photoUrls,
                    qas,
                    profile.getGender(),
                    profile.getPronouns(),
                    profile.getRelationshipIntent(),
                    profile.getHeightCm()
            );
        }

        throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No more candidates");
    }

    @Transactional
    public void pass(UUID viewer, UUID candidate) {

        var seenCandidate = seenRepo.findByViewerUserIdAndCandidateUserId(viewer, candidate)
                .orElse(SeenCandidate.builder()
                        .viewerUserId(viewer)
                        .candidateUserId(candidate)
                        .build());
        seenCandidate.setDecision(SeenCandidate.Decision.PASS);
        seenRepo.save(seenCandidate);
    }

    @Transactional
    public LikeResponse like(UUID liker, UUID liked) {

        // Mark seen as LIKE
        var seenCandidate = seenRepo.findByViewerUserIdAndCandidateUserId(liker, liked)
                .orElse(SeenCandidate.builder().viewerUserId(liker).candidateUserId(liked).build());
        seenCandidate.setDecision(SeenCandidate.Decision.LIKE);
        seenRepo.save(seenCandidate);

        // Save like if not exists
        if (!likeRepo.existsByLikerIdAndLikedId(liker, liked)) {
            likeRepo.save(LikeEntity.builder().likerId(liker).likedId(liked).build());
        }

        // Mutual like?
        boolean mutual = likeRepo.existsByLikerIdAndLikedId(liked, liker);
        if (!mutual) return LikeResponse.liked();

        // Create deterministic match (userA < userB)
        UUID a = liker.compareTo(liked) < 0 ? liker : liked;
        UUID b = liker.compareTo(liked) < 0 ? liked : liker;

        var match = matchRepo.findByUserAAndUserB(a, b)
                .orElseGet(() -> matchRepo.save(Match.builder().userA(a).userB(b).build()));

        var thread = chatRepo.findByMatchId(match.getId())
                .orElseGet(() -> chatRepo.save(ChatThread.builder().matchId(match.getId()).build()));

        events.publishEvent(new MatchCreatedEvent(match.getId(), a, b, thread.getId(), Instant.now()));

        return LikeResponse.matched(match.getId(), thread.getId());
    }
}

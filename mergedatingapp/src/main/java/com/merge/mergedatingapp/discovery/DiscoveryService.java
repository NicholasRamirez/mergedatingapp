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
                .filter(p -> p.isDiscoverable())
                .filter(p -> !p.getUserId().equals(viewerUserId))
                .filter(p -> !blockedRepo.existsByBlockerIdAndBlockedId(viewerUserId, p.getUserId())
                        && !blockedRepo.existsByBlockerIdAndBlockedId(p.getUserId(), viewerUserId))
                .filter(p -> !seenRepo.existsByViewerUserIdAndCandidateUserId(viewerUserId, p.getUserId()))
                .toList();

        // Delegate ordering to Strategy
        var ordered = candidateOrderingStrategy.orderCandidates(discoverable, viewerUserId);

        // Iterate in that order, skipping ones already seen
        for (var p : ordered) {
            boolean alreadySeen = seenRepo.existsByViewerUserIdAndCandidateUserId(viewerUserId, p.getUserId());
            if (alreadySeen) continue;

            var photoUrls = photos.findByProfileIdOrderByPositionAsc(p.getId())
                    .stream().map(Photo::getUrl).toList();

            var qas = prompts.findByProfileId(p.getId()).stream()
                    .limit(2)
                    .map(pa -> new CandidateCard.PromptQA(pa.getQuestion(), pa.getAnswer()))
                    .toList();

            return new CandidateCard(
                    p.getUserId(),
                    p.getName(),
                    p.getCity(),
                    photoUrls,
                    qas,
                    p.getGender(),
                    p.getPronouns(),
                    p.getRelationshipIntent(),
                    p.getHeightCm()
            );
        }

        throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No more candidates");
    }

    @Transactional
    public void pass(UUID viewer, UUID candidate) {
        var sc = seenRepo.findByViewerUserIdAndCandidateUserId(viewer, candidate)
                .orElse(SeenCandidate.builder()
                        .viewerUserId(viewer)
                        .candidateUserId(candidate)
                        .build());
        sc.setDecision(SeenCandidate.Decision.PASS);
        seenRepo.save(sc);
    }

    @Transactional
    public LikeResponse like(UUID liker, UUID liked) {
        // mark seen as LIKE
        var sc = seenRepo.findByViewerUserIdAndCandidateUserId(liker, liked)
                .orElse(SeenCandidate.builder().viewerUserId(liker).candidateUserId(liked).build());
        sc.setDecision(SeenCandidate.Decision.LIKE);
        seenRepo.save(sc);

        // save like if not exists
        if (!likeRepo.existsByLikerIdAndLikedId(liker, liked)) {
            likeRepo.save(LikeEntity.builder().likerId(liker).likedId(liked).build());
        }

        // mutual?
        boolean mutual = likeRepo.existsByLikerIdAndLikedId(liked, liker);
        if (!mutual) return LikeResponse.liked();

        // create deterministic match (userA < userB)
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

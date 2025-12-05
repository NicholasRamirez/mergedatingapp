package com.merge.mergedatingapp.commands;

import com.merge.mergedatingapp.auth.AuthTokenRepository;
import com.merge.mergedatingapp.chat.ChatMessageRepository;
import com.merge.mergedatingapp.discovery.*;
import com.merge.mergedatingapp.profiles.*;
import com.merge.mergedatingapp.users.BlockedUserRepository;
import com.merge.mergedatingapp.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteAccountCommand implements UserCommand {

    private final ProfileRepository profiles;
    private final PhotoRepository photos;
    private final PromptAnswerRepository prompts;

    private final SeenCandidateRepository seenRepo;
    private final LikeRepository likeRepo;
    private final MatchRepository matchRepo;
    private final ChatThreadRepository chatRepo;
    private final ChatMessageRepository messageRepo;
    private final BlockedUserRepository blockedRepo;

    private final AuthTokenRepository tokenRepo;
    private final UserRepository users;

    @Override
    @Transactional
    public void execute(UUID userId) {

        // Profile + prompts + photos
        profiles.findByUserId(userId).ifPresent(profile -> {
            UUID profileId = profile.getId();
            prompts.deleteByProfileId(profileId);
            photos.deleteByProfileId(profileId);
            profiles.delete(profile);
        });

        // Discovery / matching / chat
        // DELETE FROM SeenCandidate
        seenRepo.deleteByViewerUserId(userId);
        seenRepo.deleteByCandidateUserId(userId);

        // DELETE FROM LikeEntity
        likeRepo.deleteByLikerId(userId);
        likeRepo.deleteByLikedId(userId);

        matchRepo.findByUserAOrUserB(userId, userId).forEach(match -> {
            chatRepo.findByMatchId(match.getId()).ifPresent(thread -> {
                messageRepo.deleteByThreadId(thread.getId());
                chatRepo.delete(thread);
            });
            matchRepo.delete(match);
        });

        // Blocks
        blockedRepo.deleteByBlockerIdOrBlockedId(userId, userId);

        // Auth tokens + user
        tokenRepo.deleteByUserId(userId);
        users.deleteById(userId);
    }
}
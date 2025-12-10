package com.merge.mergedatingapp.chat;

import com.merge.mergedatingapp.chat.dto.MessageRequest;
import com.merge.mergedatingapp.chat.dto.MessageResponse;
import com.merge.mergedatingapp.chat.dto.ThreadSummary;
import com.merge.mergedatingapp.discovery.ChatThread;
import com.merge.mergedatingapp.discovery.ChatThreadRepository;
import com.merge.mergedatingapp.discovery.MatchRepository;
import com.merge.mergedatingapp.users.BlockedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.merge.mergedatingapp.profiles.ProfileRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MatchRepository matches;
    private final ChatThreadRepository threads;
    private final ChatMessageRepository messages;
    private final ProfileRepository profiles;
    private final BlockedUserRepository blockedRepo;

    // List visible chat threads for user
    @Transactional(readOnly = true)
    public List<ThreadSummary> listThreads(UUID userId) {
        var matchesForUser = matches.findByUserAOrUserB(userId, userId);

        return matchesForUser.stream()
                .filter(match -> {
                    UUID partner = match.getUserA().equals(userId)
                            ? match.getUserB()
                            : match.getUserA();

                    boolean blockedEitherWay =
                            blockedRepo.existsByBlockerIdAndBlockedId(userId, partner) ||
                                    blockedRepo.existsByBlockerIdAndBlockedId(partner, userId);

                    return !blockedEitherWay;
                })
                .map(match -> {
                    var thread = threads.findByMatchId(match.getId()).orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Thread missing for match"
                            ));

                    UUID partner = match.getUserA().equals(userId)
                            ? match.getUserB()
                            : match.getUserA();

                    // Look up the partner's profile to get their display name
                    var profileOpt = profiles.findByUserId(partner);
                    String partnerName = profileOpt.map(profile -> profile.getName()).orElse(null);

                    return new ThreadSummary(
                            thread.getId(),
                            match.getId(),
                            partner,
                            partnerName
                    );
                })
                .toList();
    }

    // Return all messages in a thread.
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(UUID userId, UUID threadId) {
        var thread = threads.findById(threadId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found"));

        ensureParticipant(userId, thread);

        return messages.findByThreadIdOrderBySentAtAsc(threadId).stream()
                .map(message -> new MessageResponse(message.getId(), message.getThreadId(), message.getSenderId(),
                        message.getSentAt(), message.getContent()))
                .toList();
    }

    @Transactional
    public MessageResponse send(UUID userId, UUID threadId, MessageRequest req) {
        var thread = threads.findById(threadId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found"));

        ensureParticipant(userId, thread);

        var saved = messages.save(ChatMessage.builder()
                .threadId(threadId)
                .senderId(userId)
                .content(req.content())
                .build());

        return new MessageResponse(saved.getId(), saved.getThreadId(),
                saved.getSenderId(), saved.getSentAt(), saved.getContent());
    }

    // Makes sure user is one of participants in the match for chat thread.
    private void ensureParticipant(UUID userId, ChatThread t) {
        var match = matches.findById(t.getMatchId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Match missing for thread"));

        if (!(match.getUserA().equals(userId) || match.getUserB().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant");
        }
    }
}

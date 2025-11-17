package com.merge.mergedatingapp.chat;

import com.merge.mergedatingapp.chat.dto.MessageRequest;
import com.merge.mergedatingapp.chat.dto.MessageResponse;
import com.merge.mergedatingapp.chat.dto.ThreadSummary;
import com.merge.mergedatingapp.discovery.ChatThread;
import com.merge.mergedatingapp.discovery.ChatThreadRepository;
import com.merge.mergedatingapp.discovery.Match;
import com.merge.mergedatingapp.discovery.MatchRepository;
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

    @Transactional(readOnly = true)
    public List<ThreadSummary> listThreads(UUID userId) {
        var ms = matches.findByUserAOrUserB(userId, userId);
        return ms.stream().map(m -> {
            var t = threads.findByMatchId(m.getId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Thread missing for match"));
            UUID partner = m.getUserA().equals(userId) ? m.getUserB() : m.getUserA();

            // Look up the partner's profile to get their display name
            var profileOpt = profiles.findByUserId(partner);
            String partnerName = profileOpt.map(p -> p.getName()).orElse(null);

            return new ThreadSummary(t.getId(), m.getId(), partner, partnerName);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(UUID userId, UUID threadId) {
        var t = threads.findById(threadId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found"));
        ensureParticipant(userId, t);
        return messages.findByThreadIdOrderBySentAtAsc(threadId).stream()
                .map(m -> new MessageResponse(m.getId(), m.getThreadId(), m.getSenderId(), m.getSentAt(), m.getContent()))
                .toList();
    }

    @Transactional
    public MessageResponse send(UUID userId, UUID threadId, MessageRequest req) {
        var t = threads.findById(threadId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found"));
        ensureParticipant(userId, t);
        var saved = messages.save(ChatMessage.builder()
                .threadId(threadId)
                .senderId(userId)
                .content(req.content())
                .build());
        return new MessageResponse(saved.getId(), saved.getThreadId(), saved.getSenderId(), saved.getSentAt(), saved.getContent());
    }

    private void ensureParticipant(UUID userId, ChatThread t) {
        var match = matches.findById(t.getMatchId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Match missing for thread"));
        if (!(match.getUserA().equals(userId) || match.getUserB().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant");
        }
    }
}

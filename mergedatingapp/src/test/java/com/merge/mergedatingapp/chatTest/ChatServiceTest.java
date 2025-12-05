package com.merge.mergedatingapp.chatTest;

import com.merge.mergedatingapp.chat.ChatMessage;
import com.merge.mergedatingapp.chat.ChatMessageRepository;
import com.merge.mergedatingapp.chat.ChatService;
import com.merge.mergedatingapp.chat.dto.MessageRequest;
import com.merge.mergedatingapp.chat.dto.MessageResponse;
import com.merge.mergedatingapp.chat.dto.ThreadSummary;
import com.merge.mergedatingapp.discovery.ChatThread;
import com.merge.mergedatingapp.discovery.ChatThreadRepository;
import com.merge.mergedatingapp.discovery.Match;
import com.merge.mergedatingapp.discovery.MatchRepository;
import com.merge.mergedatingapp.profiles.Profile;
import com.merge.mergedatingapp.profiles.ProfileRepository;
import com.merge.mergedatingapp.users.BlockedUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    MatchRepository matches;

    @Mock
    ChatThreadRepository threads;

    @Mock
    ChatMessageRepository messages;

    @Mock
    ProfileRepository profiles;

    @Mock
    BlockedUserRepository blockedRepo;

    @InjectMocks
    ChatService svc;

    @Test
    void listThreads_returnsThreadSummariesWithPartnerName() {
        UUID userId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();

        Match match = Match.builder()
                .id(matchId)
                .userA(userId)
                .userB(partnerId)
                .build();

        ChatThread thread = ChatThread.builder()
                .id(threadId)
                .matchId(matchId)
                .build();

        Profile partnerProfile = Profile.builder()
                .userId(partnerId)
                .name("Jess")
                .build();

        when(matches.findByUserAOrUserB(userId, userId))
                .thenReturn(List.of(match));
        when(threads.findByMatchId(matchId))
                .thenReturn(Optional.of(thread));
        when(profiles.findByUserId(partnerId))
                .thenReturn(Optional.of(partnerProfile));

        List<ThreadSummary> result = svc.listThreads(userId);

        assertEquals(1, result.size());
        ThreadSummary ts = result.get(0);
        assertEquals(threadId, ts.threadId());
        assertEquals(matchId, ts.matchId());
        assertEquals(partnerId, ts.partnerUserId());
        assertEquals("Jess", ts.partnerName());
    }

    @Test
    void getMessages_returnsMessagesWhenUserIsParticipant() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();

        ChatThread thread = ChatThread.builder()
                .id(threadId)
                .matchId(matchId)
                .build();

        Match match = Match.builder()
                .id(matchId)
                .userA(userA)
                .userB(userB)
                .build();

        ChatMessage m1 = ChatMessage.builder()
                .id(UUID.randomUUID())
                .threadId(threadId)
                .senderId(userA)
                .content("hi")
                .sentAt(Instant.now())
                .build();

        ChatMessage m2 = ChatMessage.builder()
                .id(UUID.randomUUID())
                .threadId(threadId)
                .senderId(userB)
                .content("hello")
                .sentAt(Instant.now().plusSeconds(10))
                .build();

        when(threads.findById(threadId))
                .thenReturn(Optional.of(thread));
        when(matches.findById(matchId))
                .thenReturn(Optional.of(match));
        when(messages.findByThreadIdOrderBySentAtAsc(threadId))
                .thenReturn(List.of(m1, m2));

        List<MessageResponse> result = svc.getMessages(userA, threadId);

        assertEquals(2, result.size());
        assertEquals("hi", result.get(0).content());
        assertEquals("hello", result.get(1).content());
    }

    @Test
    void getMessages_throwsForbiddenWhenUserNotParticipant() {
        UUID userNotInMatch = UUID.randomUUID();
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();

        ChatThread thread = ChatThread.builder()
                .id(threadId)
                .matchId(matchId)
                .build();

        Match match = Match.builder()
                .id(matchId)
                .userA(userA)
                .userB(userB)
                .build();

        when(threads.findById(threadId))
                .thenReturn(Optional.of(thread));
        when(matches.findById(matchId))
                .thenReturn(Optional.of(match));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> svc.getMessages(userNotInMatch, threadId)
        );
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void send_savesMessageAndReturnsResponse() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();

        ChatThread thread = ChatThread.builder()
                .id(threadId)
                .matchId(matchId)
                .build();

        Match match = Match.builder()
                .id(matchId)
                .userA(userA)
                .userB(userB)
                .build();

        when(threads.findById(threadId))
                .thenReturn(Optional.of(thread));
        when(matches.findById(matchId))
                .thenReturn(Optional.of(match));

        // Capture the ChatMessage passed to save(...)
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);

        ChatMessage saved = ChatMessage.builder()
                .id(UUID.randomUUID())
                .threadId(threadId)
                .senderId(userA)
                .content("hey there")
                .sentAt(Instant.now())
                .build();

        when(messages.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> {
                    ChatMessage arg = invocation.getArgument(0);
                    // simulate DB assigning ID
                    saved.setContent(arg.getContent());
                    saved.setThreadId(arg.getThreadId());
                    saved.setSenderId(arg.getSenderId());
                    return saved;
                });

        MessageRequest req = new MessageRequest("hey there");
        MessageResponse resp = svc.send(userA, threadId, req);

        verify(messages).save(captor.capture());
        ChatMessage toSave = captor.getValue();
        assertEquals("hey there", toSave.getContent());
        assertEquals(threadId, toSave.getThreadId());
        assertEquals(userA, toSave.getSenderId());

        assertEquals(saved.getId(), resp.id());
        assertEquals(saved.getThreadId(), resp.threadId());
        assertEquals(saved.getSenderId(), resp.senderId());
        assertEquals(saved.getContent(), resp.content());
    }
}
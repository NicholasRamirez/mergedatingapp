package com.merge.mergedatingapp.chatTest;

import com.merge.mergedatingapp.chat.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository repo;

    @Test
    void findByThreadIdOrderBySentAtAsc_returnsInAscendingOrder() {
        UUID threadId = UUID.randomUUID();

        ChatMessage m2 = ChatMessage.builder()
                .threadId(threadId)
                .senderId(UUID.randomUUID())
                .content("second")
                .sentAt(Instant.now().plusSeconds(10))
                .build();

        ChatMessage m1 = ChatMessage.builder()
                .threadId(threadId)
                .senderId(UUID.randomUUID())
                .content("first")
                .sentAt(Instant.now())
                .build();

        repo.save(m2);
        repo.save(m1);

        List<ChatMessage> result = repo.findByThreadIdOrderBySentAtAsc(threadId);

        assertEquals(2, result.size());
        assertEquals("first", result.get(0).getContent());
        assertEquals("second", result.get(1).getContent());
    }
}
package com.merge.mergedatingapp.chatTest;

import com.merge.mergedatingapp.chat.*;
import com.merge.mergedatingapp.chat.dto.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageResponseTest {

    @Test
    void constructorAndGetters_work() {
        UUID id = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        Instant now = Instant.now();

        MessageResponse resp = new MessageResponse(id, threadId, senderId, now, "hey");

        assertEquals(id, resp.id());
        assertEquals(threadId, resp.threadId());
        assertEquals(senderId, resp.senderId());
        assertEquals(now, resp.sentAt());
        assertEquals("hey", resp.content());
    }
}
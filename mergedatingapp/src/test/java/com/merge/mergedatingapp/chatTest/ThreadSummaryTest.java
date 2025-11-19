package com.merge.mergedatingapp.chatTest;

import com.merge.mergedatingapp.chat.*;
import com.merge.mergedatingapp.chat.dto.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ThreadSummaryTest {

    @Test
    void constructorAndGetters_work() {
        UUID threadId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();

        ThreadSummary ts = new ThreadSummary(threadId, matchId, partnerId, "Jess");

        assertEquals(threadId, ts.threadId());
        assertEquals(matchId, ts.matchId());
        assertEquals(partnerId, ts.partnerUserId());
        assertEquals("Jess", ts.partnerName());
    }
}

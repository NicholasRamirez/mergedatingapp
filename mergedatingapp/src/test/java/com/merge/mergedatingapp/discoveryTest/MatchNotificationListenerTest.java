package com.merge.mergedatingapp.discoveryTest;

import com.merge.mergedatingapp.discovery.events.MatchCreatedEvent;
import com.merge.mergedatingapp.discovery.events.MatchNotificationListener;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MatchNotificationListenerTest {

    @Test
    void onMatchCreated_handlesEventWithoutError() {
        // Create listener and a sample event
        MatchNotificationListener listener = new MatchNotificationListener();

        MatchCreatedEvent event = new MatchCreatedEvent(
                UUID.randomUUID(),   // matchId
                UUID.randomUUID(),   // threadId
                UUID.randomUUID(),   // userAId
                UUID.randomUUID(),   // userBId
                Instant.now()        // occurredAt
        );

        // Just make sure it doesn't throw
        assertDoesNotThrow(() -> listener.onMatchCreated(event));
    }
}

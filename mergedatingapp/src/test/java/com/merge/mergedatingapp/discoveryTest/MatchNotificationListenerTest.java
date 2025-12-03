package com.merge.mergedatingapp.discoveryTest;

import com.merge.mergedatingapp.discovery.events.MatchCreatedEvent;
import com.merge.mergedatingapp.discovery.events.MatchNotificationListener;
import com.merge.mergedatingapp.notifications.NotificationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchNotificationListenerTest {

    @Mock
    NotificationFactory notificationFactory;

    @InjectMocks
    MatchNotificationListener listener;

    @Test
    void onMatchCreated_delegatesToNotificationFactory() {
        // Build a sample event
        UUID matchId  = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();
        UUID userAId  = UUID.randomUUID();
        UUID userBId  = UUID.randomUUID();

        MatchCreatedEvent event = new MatchCreatedEvent(
                matchId,
                threadId,
                userAId,
                userBId,
                Instant.now()
        );

        // Act
        listener.onMatchCreated(event);

        // Listener forwards to factory
        verify(notificationFactory).notifyMatch(
                userAId,
                userBId,
                matchId,
                threadId
        );
    }
}

package com.merge.mergedatingapp.discovery.events;

import com.merge.mergedatingapp.notifications.NotificationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchNotificationListener {

    private final NotificationFactory notificationFactory;

    @EventListener
    public void onMatchCreated(MatchCreatedEvent event) {
        // Delegate to factory (Factory Method decides which sender to use)
        notificationFactory.notifyMatch(
                event.userAId(),
                event.userBId(),
                event.matchId(),
                event.threadId()
        );
    }
}
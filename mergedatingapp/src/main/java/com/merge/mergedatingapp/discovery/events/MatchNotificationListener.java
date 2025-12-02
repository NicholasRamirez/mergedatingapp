package com.merge.mergedatingapp.discovery.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MatchNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(MatchNotificationListener.class);

    @EventListener
    public void onMatchCreated(MatchCreatedEvent event) {
        log.info("New match! matchId={}, threadId={}, userA={}, userB={}, at={}",
                event.matchId(), event.threadId(), event.userAId(), event.userBId(), event.occurredAt());
    }
}
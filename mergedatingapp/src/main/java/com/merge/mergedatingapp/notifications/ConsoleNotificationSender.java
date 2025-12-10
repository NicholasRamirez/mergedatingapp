package com.merge.mergedatingapp.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

// Concrete NotificationSender that logs match notifications.

public class ConsoleNotificationSender implements NotificationSender {

    private static final Logger log =
            LoggerFactory.getLogger(ConsoleNotificationSender.class);

    @Override
    public void sendMatchNotification(UUID userA, UUID userB, UUID matchId, UUID threadId) {
        log.info("Console notify: new match={} between userA={} and userB={}, thread={}",
                matchId, userA, userB, threadId);
    }
}

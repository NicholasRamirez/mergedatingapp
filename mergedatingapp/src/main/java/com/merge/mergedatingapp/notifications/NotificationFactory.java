package com.merge.mergedatingapp.notifications;

import java.util.UUID;

// Creator in the factory.
// Subclasses decide which NotificationSender to instantiate.

public abstract class NotificationFactory {

    // Choose a sender implementation for user.
    public abstract NotificationSender createSenderForUser(UUID userId);

    // Notify both sides of a new match.
    public void notifyMatch(UUID userA, UUID userB, UUID matchId, UUID threadId) {

        var senderA = createSenderForUser(userA);
        senderA.sendMatchNotification(userA, userB, matchId, threadId);

        var senderB = createSenderForUser(userB);
        senderB.sendMatchNotification(userB, userA, matchId, threadId);
    }
}

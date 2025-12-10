package com.merge.mergedatingapp.notifications;

import java.util.UUID;

// Abstraction for sending match notifications.

public interface NotificationSender {

     // Send a notification that a new match was created.
    void sendMatchNotification(UUID userA, UUID userB, UUID matchId, UUID threadId);
}

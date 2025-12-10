package com.merge.mergedatingapp.notifications;

import org.springframework.stereotype.Component;

import java.util.UUID;

// ConcreteCreator in the factory.
// Current: returns ConsoleNotificationSender. Meant to further allow different notification preferences.

@Component
public class DefaultNotificationFactory extends NotificationFactory {

    @Override
    public NotificationSender createSenderForUser(UUID userId) {
        return new ConsoleNotificationSender();
    }
}

package com.merge.mergedatingapp.notifications;

import org.springframework.stereotype.Component;

import java.util.UUID;

//ConcreteCreator: current default strategy is "log to console".
// Later this can look at user preferences.

@Component
public class DefaultNotificationFactory extends NotificationFactory {

    @Override
    public NotificationSender createSenderForUser(UUID userId) {
        // TODO later: read notification preferences for this user
        return new ConsoleNotificationSender();
    }
}

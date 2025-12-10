package com.merge.mergedatingapp.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Invoker for UserCommand Objects

@Component
@RequiredArgsConstructor
public class UserCommandExecutor {

    public void execute(UserCommand command, UUID userId) {
        command.execute(userId);
    }
}

package com.merge.mergedatingapp.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserCommandExecutor {

    public void execute(UserCommand command, UUID userId) {
        command.execute(userId);
    }
}

package com.merge.mergedatingapp.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserCommandExecutor {

    public void execute(UserCommand command) {
        var name = command.getClass().getSimpleName();
        log.info("Executing command: {}", name);
        command.execute();
        log.info("Finished command: {}", name);
    }
}

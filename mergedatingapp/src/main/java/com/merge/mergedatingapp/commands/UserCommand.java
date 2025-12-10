package com.merge.mergedatingapp.commands;

import java.util.UUID;

// Account related command operating on a single userId.

public interface UserCommand {
    void execute(UUID userId);
}

package com.merge.mergedatingapp.commands;

import java.util.UUID;

public interface UserCommand {
    void execute(UUID userId);
}

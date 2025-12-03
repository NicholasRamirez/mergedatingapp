package com.merge.mergedatingapp.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class DeleteAccountCommand implements UserCommand {

    private final UUID userId;

    @Override
    public void execute() {
        // TODO: implement real deletion logic (profiles, matches, messages, etc.)
        log.warn("DeleteAccountCommand: deleting account for user {}", userId);
    }
}
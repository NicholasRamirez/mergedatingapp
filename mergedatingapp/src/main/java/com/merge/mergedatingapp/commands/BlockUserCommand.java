package com.merge.mergedatingapp.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class BlockUserCommand implements UserCommand {

    private final UUID blockerId;
    private final UUID blockedId;

    @Override
    public void execute() {
        // TODO: implement real block logic (persist block, hide chats, etc.)
        log.info("BlockUserCommand: user {} is blocking user {}", blockerId, blockedId);
    }
}

package com.merge.mergedatingapp.commands;

import com.merge.mergedatingapp.users.BlockedUser;
import com.merge.mergedatingapp.users.BlockedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

// Command that holds the logic for blocking another user

@Component
@RequiredArgsConstructor
public class BlockUserCommand {

    private final BlockedUserRepository blockedRepo;

    @Transactional
    public void execute(UUID blockerId, UUID blockedId) {

        // no self-block
        if (blockerId.equals(blockedId)) {
            return;
        }

        // already blocked? no-op
        if (blockedRepo.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return;
        }

        BlockedUser entity = BlockedUser.builder()
                .id(UUID.randomUUID())
                .blockerId(blockerId)
                .blockedId(blockedId)
                .createdAt(Instant.now())
                .build();

        blockedRepo.save(entity);
    }
}

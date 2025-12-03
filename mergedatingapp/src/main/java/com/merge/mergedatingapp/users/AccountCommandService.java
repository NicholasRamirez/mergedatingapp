package com.merge.mergedatingapp.users;

import com.merge.mergedatingapp.commands.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountCommandService {

    private final UserCommandExecutor executor;

    public void blockUser(UUID blockerId, UUID blockedId) {
        var cmd = new BlockUserCommand(blockerId, blockedId);
        executor.execute(cmd);
    }

    public void deleteAccount(UUID userId) {
        var cmd = new DeleteAccountCommand(userId);
        executor.execute(cmd);
    }
}

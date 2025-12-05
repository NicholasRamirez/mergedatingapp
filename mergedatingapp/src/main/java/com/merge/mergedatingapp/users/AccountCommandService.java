package com.merge.mergedatingapp.users;

import com.merge.mergedatingapp.commands.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountCommandService {

    private final UserCommandExecutor executor;
    private final DeleteAccountCommand deleteAccountCommand;
    private final BlockUserCommand blockUserCommand;

    public void deleteAccount(UUID userId) {
        executor.execute(deleteAccountCommand, userId);
    }

    public void blockUser(UUID blockerId, UUID blockedId) {
        blockUserCommand.execute(blockerId, blockedId);
    }
}

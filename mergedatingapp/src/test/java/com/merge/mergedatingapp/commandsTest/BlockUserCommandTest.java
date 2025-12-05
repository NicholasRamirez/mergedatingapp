package com.merge.mergedatingapp.commandsTest;

import com.merge.mergedatingapp.users.BlockedUser;
import com.merge.mergedatingapp.users.BlockedUserRepository;
import com.merge.mergedatingapp.commands.BlockUserCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockUserCommandTest {

    @Mock
    private BlockedUserRepository blockedRepo;

    private UUID blockerId;
    private UUID blockedId;

    private BlockUserCommand cmd;

    @BeforeEach
    void setUp() {
        blockerId = UUID.randomUUID();
        blockedId  = UUID.randomUUID();

        // constructor only takes the repo
        cmd = new BlockUserCommand(blockedRepo);
    }

    @Test
    void execute_savesBlockedUser_whenNotAlreadyBlocked() {
        // Viewer has not blocked this user yet
        when(blockedRepo.existsByBlockerIdAndBlockedId(blockerId, blockedId))
                .thenReturn(false);

        cmd.execute(blockerId, blockedId);

        // One BlockedUser entity saved with correct ids
        ArgumentCaptor<BlockedUser> captor = ArgumentCaptor.forClass(BlockedUser.class);
        verify(blockedRepo).save(captor.capture());

        BlockedUser saved = captor.getValue();
        assertThat(saved.getBlockerId()).isEqualTo(blockerId);
        assertThat(saved.getBlockedId()).isEqualTo(blockedId);
    }

    @Test
    void execute_doesNothing_whenAlreadyBlocked() {
        when(blockedRepo.existsByBlockerIdAndBlockedId(blockerId, blockedId))
                .thenReturn(true);

        cmd.execute(blockerId, blockedId);

        // repository MUST NOT save a duplicate row
        verify(blockedRepo, never()).save(any());
    }

    @Test
    void execute_doesNothing_onSelfBlock() {
        UUID sameId = UUID.randomUUID();

        cmd.execute(sameId, sameId);

        verify(blockedRepo, never()).existsByBlockerIdAndBlockedId(any(), any());
        verify(blockedRepo, never()).save(any());
    }
}
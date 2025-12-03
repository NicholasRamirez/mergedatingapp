package com.merge.mergedatingapp.commandsTest;

import com.merge.mergedatingapp.commands.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCommandExecutorTest {

    @Mock
    UserCommand command;

    @Test
    void execute_invokesCommandExecute() {
        UserCommandExecutor executor = new UserCommandExecutor();

        executor.execute(command);

        verify(command).execute();
    }
}

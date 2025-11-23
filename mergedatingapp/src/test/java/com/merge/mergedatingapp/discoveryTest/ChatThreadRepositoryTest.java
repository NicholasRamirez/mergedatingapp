package com.merge.mergedatingapp.discoveryTest;

import com.merge.mergedatingapp.discovery.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ChatThreadRepositoryTest {

    @Autowired
    private ChatThreadRepository repo;

    @Test
    void findByMatchId_returnsThread() {
        UUID matchId = UUID.randomUUID();
        ChatThread saved = repo.save(ChatThread.builder().matchId(matchId).build());

        var found = repo.findByMatchId(matchId);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }
}
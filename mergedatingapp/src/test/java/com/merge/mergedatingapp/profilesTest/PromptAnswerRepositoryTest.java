package com.merge.mergedatingapp.profilesTest;

import com.merge.mergedatingapp.profiles.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PromptAnswerRepositoryTest {

    @Autowired
    private PromptAnswerRepository repo;

    @Test
    void findByProfileId_returnsAllAnswersForProfile() {
        UUID profileId = UUID.randomUUID();

        PromptAnswer a1 = PromptAnswer.builder()
                .profileId(profileId)
                .question("Q1")
                .answer("A1")
                .build();

        PromptAnswer a2 = PromptAnswer.builder()
                .profileId(profileId)
                .question("Q2")
                .answer("A2")
                .build();

        repo.save(a1);
        repo.save(a2);

        List<PromptAnswer> result = repo.findByProfileId(profileId);

        assertEquals(2, result.size());
    }

    @Test
    void countByProfileId_countsCorrectly() {
        UUID profileId = UUID.randomUUID();

        repo.save(PromptAnswer.builder().profileId(profileId).question("Q1").answer("A1").build());
        repo.save(PromptAnswer.builder().profileId(profileId).question("Q2").answer("A2").build());

        assertEquals(2, repo.countByProfileId(profileId));
    }

    @Test
    void deleteByProfileId_removesAnswers() {
        UUID profileId = UUID.randomUUID();

        repo.save(PromptAnswer.builder().profileId(profileId).question("Q1").answer("A1").build());
        repo.save(PromptAnswer.builder().profileId(profileId).question("Q2").answer("A2").build());

        repo.deleteByProfileId(profileId);

        assertEquals(0, repo.countByProfileId(profileId));
    }
}

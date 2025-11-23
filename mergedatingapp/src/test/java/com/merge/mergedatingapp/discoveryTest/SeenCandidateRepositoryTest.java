package com.merge.mergedatingapp.discoveryTest;

import com.merge.mergedatingapp.discovery.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SeenCandidateRepositoryTest {

    @Autowired SeenCandidateRepository repo;

    @Test
    void existsAndFindByViewerAndCandidate_works() {
        UUID viewer = UUID.randomUUID();
        UUID candidate = UUID.randomUUID();

        SeenCandidate sc = SeenCandidate.builder()
                .viewerUserId(viewer)
                .candidateUserId(candidate)
                .build();
        repo.save(sc);

        assertThat(repo.existsByViewerUserIdAndCandidateUserId(viewer, candidate)).isTrue();
        assertThat(repo.findByViewerUserIdAndCandidateUserId(viewer, candidate)).isPresent();
    }
}
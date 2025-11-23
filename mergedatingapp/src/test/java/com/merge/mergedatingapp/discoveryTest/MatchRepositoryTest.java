package com.merge.mergedatingapp.discoveryTest;

import com.merge.mergedatingapp.discovery.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MatchRepositoryTest {

    @Autowired
    private MatchRepository repo;

    @Test
    void findByUserAOrUserB_returnsMatchesForEitherSide() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        Match m1 = Match.builder().userA(user1).userB(user2).build();
        Match m2 = Match.builder().userA(other).userB(user1).build();
        Match m3 = Match.builder().userA(other).userB(other).build();

        repo.save(m1);
        repo.save(m2);
        repo.save(m3);

        List<Match> result = repo.findByUserAOrUserB(user1, user1);

        assertThat(result)
                .extracting(Match::getId)
                .hasSize(2);
    }
}

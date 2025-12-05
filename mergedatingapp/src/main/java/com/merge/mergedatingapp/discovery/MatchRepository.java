package com.merge.mergedatingapp.discovery;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    Optional<Match> findByUserAAndUserB(UUID a, UUID b);

    List<Match> findByUserAOrUserB(UUID userA, UUID userB);

    void deleteAll(Iterable<? extends Match> matches);
}

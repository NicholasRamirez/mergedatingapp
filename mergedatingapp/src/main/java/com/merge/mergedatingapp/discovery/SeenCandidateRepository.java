package com.merge.mergedatingapp.discovery;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SeenCandidateRepository extends JpaRepository<SeenCandidate, UUID> {
    Optional<SeenCandidate> findByViewerUserIdAndCandidateUserId(UUID viewer, UUID candidate);
    boolean existsByViewerUserIdAndCandidateUserId(UUID viewer, UUID candidate);
}

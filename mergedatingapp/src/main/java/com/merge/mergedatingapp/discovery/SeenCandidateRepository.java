package com.merge.mergedatingapp.discovery;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

// Repository for tracking which candidates a user has already seen.

public interface SeenCandidateRepository extends JpaRepository<SeenCandidate, UUID> {

    Optional<SeenCandidate> findByViewerUserIdAndCandidateUserId(UUID viewer, UUID candidate);

    boolean existsByViewerUserIdAndCandidateUserId(UUID viewer, UUID candidate);

    void deleteByViewerUserId(UUID viewerUserId);

    void deleteByCandidateUserId(UUID candidateUserId);
}

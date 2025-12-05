package com.merge.mergedatingapp.discovery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatThreadRepository extends JpaRepository<ChatThread, UUID> {
    Optional<ChatThread> findByMatchId(UUID matchId);

    List<ChatThread> findAllByMatchId(UUID matchId);

    void deleteAllByMatchId(UUID matchId); // optional helper
}

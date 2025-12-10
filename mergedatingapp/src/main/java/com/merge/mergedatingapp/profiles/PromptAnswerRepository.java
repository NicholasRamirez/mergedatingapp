package com.merge.mergedatingapp.profiles;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

// Repository for profile prompts (Q&A).

public interface PromptAnswerRepository extends JpaRepository<PromptAnswer, UUID> {

    List<PromptAnswer> findByProfileId(UUID profileId);

    long countByProfileId(UUID profileId);

    void deleteByProfileId(UUID profileId);
}

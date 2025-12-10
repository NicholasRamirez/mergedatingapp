package com.merge.mergedatingapp.discovery;

import com.merge.mergedatingapp.profiles.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

// Default candidate ordering strategy:
// Order candidates by lastUpdated timestamp.

@Component
public class LastUpdatedCandidateOrderingStrategy implements CandidateOrderingStrategy {

    @Override
    public List<Profile> orderCandidates(List<Profile> candidates, UUID viewerUserId) {
        return candidates.stream()

                // if lastUpdated is ever null, treat as very old
                .sorted(Comparator.comparing(
                        p -> p.getLastUpdated() != null ? p.getLastUpdated() : Instant.EPOCH
                ))
                .toList();
    }
}

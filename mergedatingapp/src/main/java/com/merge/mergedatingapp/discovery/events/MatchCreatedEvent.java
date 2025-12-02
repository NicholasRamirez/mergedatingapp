package com.merge.mergedatingapp.discovery.events;

import java.time.Instant;
import java.util.UUID;

public record MatchCreatedEvent(
        UUID matchId,
        UUID threadId,
        UUID userAId,
        UUID userBId,
        Instant occurredAt
) {}

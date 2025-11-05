package com.merge.mergedatingapp.chat.dto;

import java.util.UUID;

public record ThreadSummary(UUID threadId, UUID matchId, UUID partnerUserId) {}

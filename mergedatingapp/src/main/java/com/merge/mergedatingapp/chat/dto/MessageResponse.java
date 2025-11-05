package com.merge.mergedatingapp.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(UUID id, UUID threadId, UUID senderId, Instant sentAt, String content) {}

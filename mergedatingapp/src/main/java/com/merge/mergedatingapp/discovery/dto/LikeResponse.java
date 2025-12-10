package com.merge.mergedatingapp.discovery.dto;

import java.util.UUID;

public record LikeResponse(String status, UUID matchId, UUID threadId) {

    public static LikeResponse liked() {
        return new LikeResponse("LIKED", null, null);
    }

    public static LikeResponse matched(UUID matchId, UUID threadId) {
        return new LikeResponse("MATCHED", matchId, threadId);
    }
}

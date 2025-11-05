package com.merge.mergedatingapp.discovery.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LikeRequest(
        @NotNull
        @JsonProperty("likedUserId")                 // primary expected name
        @JsonAlias({"LikedUserId", "LIKEDUSERID"})   // accept common variants
        UUID likedUserId
) {}

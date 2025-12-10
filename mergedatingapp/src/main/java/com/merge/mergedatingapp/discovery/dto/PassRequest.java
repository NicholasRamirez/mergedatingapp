package com.merge.mergedatingapp.discovery.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PassRequest(
        @NotNull
        @JsonProperty("candidateUserId")
        @JsonAlias({"CandidateUserId","CANDIDATEUSERID"})
        UUID candidateUserId
) {}

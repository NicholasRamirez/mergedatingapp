package com.merge.mergedatingapp.profiles.dto;

import com.merge.mergedatingapp.profiles.Enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProfileUpdateRequest(
        @NotBlank String name,
        @NotBlank String city,
        @NotNull LocalDate birthday,
        GenderType gender,
        PronounsType pronouns,
        RelationshipIntentType relationshipIntent,
        Integer heightCm
) {}

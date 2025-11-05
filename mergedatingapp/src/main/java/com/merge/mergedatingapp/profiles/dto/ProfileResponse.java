package com.merge.mergedatingapp.profiles.dto;

import com.merge.mergedatingapp.profiles.Enums.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID profileId,
        UUID userId,
        String name,
        LocalDate birthday,
        GenderType gender,
        PronounsType pronouns,
        RelationshipIntentType relationshipIntent,
        Integer heightCm,
        String city,
        boolean discoverable,
        List<String> photos,
        List<PromptQA> prompts
) {
    public record PromptQA(String question, String answer) {}
}

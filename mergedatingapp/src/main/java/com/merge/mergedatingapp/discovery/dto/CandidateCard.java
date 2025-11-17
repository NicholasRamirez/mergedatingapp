package com.merge.mergedatingapp.discovery.dto;

import com.merge.mergedatingapp.profiles.Enums;

import java.util.List;
import java.util.UUID;

public record CandidateCard(
        UUID userId,
        String name,
        String city,
        List<String> photos,
        List<PromptQA> prompts,

        Enums.GenderType genderType,
        Enums.PronounsType pronounsType,
        Enums.RelationshipIntentType relationshipIntent,
        Integer heightCm
) {
    public record PromptQA(String question, String answer) {}
}

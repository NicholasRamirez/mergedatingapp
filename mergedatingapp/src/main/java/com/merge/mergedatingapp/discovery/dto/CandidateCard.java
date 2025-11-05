package com.merge.mergedatingapp.discovery.dto;

import java.util.List;
import java.util.UUID;

public record CandidateCard(
        UUID userId,
        String name,
        String city,
        List<String> photos,
        List<PromptQA> prompts
) {
    public record PromptQA(String question, String answer) {}
}

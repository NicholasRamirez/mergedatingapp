package com.merge.mergedatingapp.profiles.dto;

import jakarta.validation.constraints.NotBlank;

public record PromptAnswerRequest(
        @NotBlank String question,
        @NotBlank String answer
) {}

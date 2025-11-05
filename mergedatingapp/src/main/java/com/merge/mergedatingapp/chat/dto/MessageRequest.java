package com.merge.mergedatingapp.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(@NotBlank String content) {}

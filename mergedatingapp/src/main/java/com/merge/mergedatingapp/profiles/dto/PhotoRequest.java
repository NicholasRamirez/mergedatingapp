package com.merge.mergedatingapp.profiles.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PhotoRequest(
        @NotBlank String url,
        @Min(0) int position
) {}

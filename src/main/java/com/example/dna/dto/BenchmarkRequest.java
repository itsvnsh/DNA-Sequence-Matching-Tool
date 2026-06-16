package com.example.dna.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for the /api/benchmark endpoint.
 */
public record BenchmarkRequest(

        @NotBlank(message = "Genome text must not be empty")
        String text,

        @NotBlank(message = "Pattern must not be empty")
        String pattern
) {}

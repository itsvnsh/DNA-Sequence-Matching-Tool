package com.example.dna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for the /api/match endpoint.
 * Validates that text and pattern contain only valid DNA bases (A, T, C, G).
 */
public record MatchRequest(

        @NotBlank(message = "Genome text must not be empty")
        String text,

        @NotBlank(message = "Pattern must not be empty")
        String pattern,

        @NotBlank(message = "Algorithm name must not be empty")
        @Pattern(regexp = "(?i)NAIVE|KMP|RABIN-KARP|BOYER-MOORE|Z-ALGORITHM|AHO-CORASICK|ALL",
                 message = "Invalid algorithm. Allowed: NAIVE, KMP, RABIN-KARP, BOYER-MOORE, Z-ALGORITHM, AHO-CORASICK, ALL")
        String algo
) {}

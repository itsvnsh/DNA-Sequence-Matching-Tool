package com.example.dna.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for the /api/dna/utils endpoint.
 */
public record UtilsRequest(

        @NotBlank(message = "Operation must not be empty")
        String op,

        @NotBlank(message = "Sequence must not be empty")
        String seq,

        String seq2,

        @Min(value = 1, message = "k must be at least 1")
        int k
) {
    /** Provide default k=3 when not supplied */
    public UtilsRequest {
        if (k <= 0) k = 3;
    }
}

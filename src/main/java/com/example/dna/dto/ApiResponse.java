package com.example.dna.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generic API response wrapper providing consistent JSON structure.
 *
 * @param <T> the type of the response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String error
) {

    /** Factory: successful response */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** Factory: error response */
    public static <T> ApiResponse<T> fail(String error) {
        return new ApiResponse<>(false, null, error);
    }
}

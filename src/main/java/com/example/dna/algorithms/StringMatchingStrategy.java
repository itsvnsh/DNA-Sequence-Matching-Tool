package com.example.dna.algorithms;

import com.example.dna.model.MatchResult;

/**
 * Strategy interface for string-matching algorithms.
 * Implements the Strategy Design Pattern — each algorithm is a pluggable strategy
 * that conforms to this interface, allowing runtime algorithm selection without
 * modifying the service layer.
 */
public interface StringMatchingStrategy {

    /**
     * Search for all occurrences of the pattern in the text.
     *
     * @param text    the genome / text to search in
     * @param pattern the pattern to search for
     * @return a MatchResult containing positions, comparison count, and timing
     */
    MatchResult search(String text, String pattern);

    /** The display name of this algorithm (e.g. "KMP") */
    String getName();

    /** The time complexity string (e.g. "O(n+m)") */
    String getComplexity();
}

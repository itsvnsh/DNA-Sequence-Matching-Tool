package com.example.dna.algorithms;

import com.example.dna.model.MatchResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for all 6 string-matching algorithms.
 * Tests correctness, edge cases, and cross-validates that all algorithms
 * agree on the same set of match positions.
 */
class AlgorithmsTest {

    // ─── Instances of all strategies ─────────────────────────

    static final NaiveStrategy       NAIVE     = new NaiveStrategy();
    static final KmpStrategy         KMP       = new KmpStrategy();
    static final RabinKarpStrategy   RABIN     = new RabinKarpStrategy();
    static final BoyerMooreStrategy  BOYER     = new BoyerMooreStrategy();
    static final ZAlgorithmStrategy  ZALG      = new ZAlgorithmStrategy();
    static final AhoCorasickStrategy AHO       = new AhoCorasickStrategy();

    static Stream<StringMatchingStrategy> allStrategies() {
        return Stream.of(NAIVE, KMP, RABIN, BOYER, ZALG, AHO);
    }

    // ─── Basic match tests ───────────────────────────────────

    @ParameterizedTest(name = "{0}")
    @MethodSource("allStrategies")
    @DisplayName("Should find pattern at correct positions")
    void shouldFindPattern(StringMatchingStrategy strategy) {
        MatchResult result = strategy.search("ATGCATGCTTACGATGCAT", "ATGCAT");
        assertEquals(List.of(0, 13), result.getPositions(),
                strategy.getName() + " failed to find correct positions");
        assertEquals(2, result.getPositions().size());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allStrategies")
    @DisplayName("Should find single occurrence at start")
    void shouldFindAtStart(StringMatchingStrategy strategy) {
        MatchResult result = strategy.search("ATGCTTTT", "ATGC");
        assertEquals(List.of(0), result.getPositions());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allStrategies")
    @DisplayName("Should find single occurrence at end")
    void shouldFindAtEnd(StringMatchingStrategy strategy) {
        MatchResult result = strategy.search("TTTTATGC", "ATGC");
        assertEquals(List.of(4), result.getPositions());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allStrategies")
    @DisplayName("Should find overlapping matches")
    void shouldFindOverlappingMatches(StringMatchingStrategy strategy) {
        MatchResult result = strategy.search("AAAA", "AA");
        assertEquals(List.of(0, 1, 2), result.getPositions(),
                strategy.getName() + " failed on overlapping matches");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allStrategies")
    @DisplayName("Should return empty when no match")
    void shouldReturnEmptyWhenNoMatch(StringMatchingStrategy strategy) {
        MatchResult result = strategy.search("ATGATGATG", "CCC");
        assertTrue(result.getPositions().isEmpty());
        assertEquals(0, result.getPositions().size());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allStrategies")
    @DisplayName("Should find full text as pattern")
    void shouldMatchFullText(StringMatchingStrategy strategy) {
        MatchResult result = strategy.search("ATGC", "ATGC");
        assertEquals(List.of(0), result.getPositions());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allStrategies")
    @DisplayName("Should handle single character pattern")
    void shouldFindSingleCharPattern(StringMatchingStrategy strategy) {
        MatchResult result = strategy.search("ATAGATA", "A");
        assertEquals(List.of(0, 2, 4, 6), result.getPositions());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allStrategies")
    @DisplayName("Should handle pattern longer than text")
    void shouldHandlePatternLongerThanText(StringMatchingStrategy strategy) {
        MatchResult result = strategy.search("AT", "ATGCATGC");
        assertTrue(result.getPositions().isEmpty());
    }

    // ─── DNA-specific tests ──────────────────────────────────

    @ParameterizedTest(name = "{0}")
    @MethodSource("allStrategies")
    @DisplayName("Should find repetitive DNA motif")
    void shouldFindRepetitiveMotif(StringMatchingStrategy strategy) {
        String genome = "ATGATGATGATGATGATG";
        MatchResult result = strategy.search(genome, "ATG");
        // ATG appears at positions 0, 3, 6, 9, 12, 15
        assertEquals(6, result.getPositions().size());
        assertEquals(List.of(0, 3, 6, 9, 12, 15), result.getPositions());
    }

    // ─── Cross-validation ────────────────────────────────────

    @Test
    @DisplayName("All algorithms should agree on match positions")
    void allAlgorithmsShouldAgree() {
        String text = "ATGCATGCTTACGATCGATCGATCGTAGCTAGCATGCATGCTTACGATGCATCGATCGGCTAGCATGCATTTACGATCGATCGTAGCTAGCATGCATGCTTACGATCGATCGATCGTAGCTAGCATGCATGCTTACGATCGATCG";
        String pattern = "ATGCAT";

        List<Integer> expected = NAIVE.search(text, pattern).getPositions();
        assertFalse(expected.isEmpty(), "Should find at least one match for cross-validation");

        assertEquals(expected, KMP.search(text, pattern).getPositions(),     "KMP disagrees with Naive");
        assertEquals(expected, RABIN.search(text, pattern).getPositions(),   "Rabin-Karp disagrees with Naive");
        assertEquals(expected, BOYER.search(text, pattern).getPositions(),   "Boyer-Moore disagrees with Naive");
        assertEquals(expected, ZALG.search(text, pattern).getPositions(),    "Z-Algorithm disagrees with Naive");
        assertEquals(expected, AHO.search(text, pattern).getPositions(),     "Aho-Corasick disagrees with Naive");
    }

    @Test
    @DisplayName("All algorithms should count comparisons > 0")
    void allAlgorithmsShouldCountComparisons() {
        allStrategies().forEach(strategy -> {
            MatchResult result = strategy.search("ATGCATGCATGC", "ATGC");
            assertTrue(result.getComparisons() > 0,
                    strategy.getName() + " should count comparisons");
        });
    }

    // ─── KMP-specific: LPS array ─────────────────────────────

    @Test
    @DisplayName("KMP LPS array should be correct for ABCABD")
    void kmpLpsArrayCorrectness() {
        assertArrayEquals(new int[]{0, 0, 0, 1, 2, 0}, KmpStrategy.buildLPS("ABCABD"));
    }

    @Test
    @DisplayName("KMP LPS array for DNA pattern ATGCAT")
    void kmpLpsForDnaPattern() {
        int[] lps = KmpStrategy.buildLPS("ATGCAT");
        assertArrayEquals(new int[]{0, 0, 0, 0, 1, 2}, lps);
    }

    @Test
    @DisplayName("KMP LPS array for repetitive pattern AAA")
    void kmpLpsForRepetitive() {
        assertArrayEquals(new int[]{0, 1, 2}, KmpStrategy.buildLPS("AAA"));
    }

    // ─── Aho-Corasick multi-pattern ──────────────────────────

    @Test
    @DisplayName("Aho-Corasick should find multiple patterns simultaneously")
    void ahoCorasickMultiPattern() {
        MatchResult result = AHO.search("ATGCATGCTTACG", "ATG,ACG");
        assertTrue(result.getPositions().contains(0),  "Should find ATG at 0");
        assertTrue(result.getPositions().contains(4),  "Should find ATG at 4");
        assertTrue(result.getPositions().contains(10), "Should find ACG at 10");
    }

    // ─── Algorithm metadata ──────────────────────────────────

    @Test
    @DisplayName("All strategies should have non-null name and complexity")
    void strategiesHaveMetadata() {
        allStrategies().forEach(s -> {
            assertNotNull(s.getName(),       s.getClass().getSimpleName() + " name is null");
            assertNotNull(s.getComplexity(), s.getClass().getSimpleName() + " complexity is null");
        });
    }
}

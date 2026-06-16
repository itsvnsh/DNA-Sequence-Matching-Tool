package com.example.dna.service;

import com.example.dna.algorithms.*;
import com.example.dna.model.MatchResult;
import com.example.dna.model.SearchHistory;
import com.example.dna.repository.SearchHistoryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the MatcherService.
 * Uses Mockito to mock the SearchHistoryRepository.
 */
@ExtendWith(MockitoExtension.class)
class MatcherServiceTest {

    @Mock
    private SearchHistoryRepository historyRepository;

    private MatcherService service;

    @BeforeEach
    void setUp() {
        // Manually build the strategy map (simulating Spring injection)
        Map<String, StringMatchingStrategy> strategies = new LinkedHashMap<>();
        strategies.put("NAIVE",        new NaiveStrategy());
        strategies.put("KMP",          new KmpStrategy());
        strategies.put("RABIN-KARP",   new RabinKarpStrategy());
        strategies.put("BOYER-MOORE",  new BoyerMooreStrategy());
        strategies.put("Z-ALGORITHM",  new ZAlgorithmStrategy());
        strategies.put("AHO-CORASICK", new AhoCorasickStrategy());

        lenient().when(historyRepository.save(any(SearchHistory.class))).thenReturn(new SearchHistory());

        service = new MatcherService(strategies, historyRepository);
    }

    // ─── Algorithm dispatch ──────────────────────────────────

    @Test
    @DisplayName("runSelected should dispatch to correct algorithm")
    void runSelectedDispatchesCorrectly() {
        List<MatchResult> results = service.runSelected("ATGCATGC", "ATGC", "KMP");
        assertEquals(1, results.size());
        assertEquals("KMP", results.get(0).getName());
    }

    @Test
    @DisplayName("runSelected with ALL should return all algorithms")
    void runSelectedAllReturnsAll() {
        List<MatchResult> results = service.runSelected("ATGCATGC", "ATGC", "ALL");
        assertEquals(6, results.size());
    }

    @Test
    @DisplayName("runSelected should throw for unknown algorithm")
    void runSelectedThrowsForUnknown() {
        assertThrows(IllegalArgumentException.class,
                () -> service.runSelected("ATGC", "AT", "UNKNOWN"));
    }

    @Test
    @DisplayName("runSelected should sanitize input (lowercase, non-DNA chars)")
    void runSelectedSanitizesInput() {
        // lowercase + numbers should be stripped
        List<MatchResult> results = service.runSelected("atgc123atgc", "atgc", "NAIVE");
        assertEquals(2, results.get(0).getPositions().size());
    }

    @Test
    @DisplayName("runSelected should throw for empty input")
    void runSelectedThrowsForEmptyInput() {
        assertThrows(IllegalArgumentException.class,
                () -> service.runSelected("", "ATGC", "KMP"));
        assertThrows(IllegalArgumentException.class,
                () -> service.runSelected("ATGC", "", "KMP"));
    }

    // ─── Benchmark ───────────────────────────────────────────

    @Test
    @DisplayName("runAll should return results from all algorithms")
    void runAllReturnsAllAlgorithms() {
        List<MatchResult> results = service.runAll("ATGCATGCATGC", "ATGC");
        assertEquals(6, results.size());
        // All should find the same number of matches
        int expected = results.get(0).getPositions().size();
        results.forEach(r ->
                assertEquals(expected, r.getPositions().size(),
                        r.getName() + " has different match count"));
    }

    // ─── DNA Utilities ───────────────────────────────────────

    @Test
    @DisplayName("complement should return correct DNA complement")
    void complementIsCorrect() {
        assertEquals("TACG", service.complement("ATGC"));
        assertEquals("AAAAAA", service.complement("TTTTTT"));
    }

    @Test
    @DisplayName("reverseComplement should be reverse of complement")
    void reverseComplementIsCorrect() {
        assertEquals("GCAT", service.reverseComplement("ATGC"));
    }

    @Test
    @DisplayName("gcContent should compute correctly")
    void gcContentIsCorrect() {
        assertEquals(50.0, service.gcContent("ATGC"), 0.01);
        assertEquals(100.0, service.gcContent("GGCC"), 0.01);
        assertEquals(0.0, service.gcContent("AATT"), 0.01);
    }

    @Test
    @DisplayName("gcContent of empty string should be 0")
    void gcContentEmptyIsZero() {
        assertEquals(0.0, service.gcContent(""), 0.01);
    }

    @Test
    @DisplayName("hammingDistance should compute correctly")
    void hammingDistanceIsCorrect() {
        assertEquals(0, service.hammingDistance("ATGC", "ATGC"));
        assertEquals(1, service.hammingDistance("ATGC", "ATGA"));
        assertEquals(4, service.hammingDistance("ATGC", "TACG"));
    }

    @Test
    @DisplayName("hammingDistance should throw for different lengths")
    void hammingDistanceThrowsForDifferentLengths() {
        assertThrows(IllegalArgumentException.class,
                () -> service.hammingDistance("ATGC", "AT"));
    }

    @Test
    @DisplayName("editDistance should compute Levenshtein distance")
    void editDistanceIsCorrect() {
        assertEquals(0, service.editDistance("ATGC", "ATGC"));
        assertEquals(1, service.editDistance("ATGC", "ATG"));   // deletion
        assertEquals(1, service.editDistance("ATG", "ATGC"));    // insertion
        assertEquals(1, service.editDistance("ATGC", "ATGA"));   // substitution
    }

    @Test
    @DisplayName("kmerFrequency should count k-mers correctly")
    void kmerFrequencyIsCorrect() {
        Map<String, Integer> freq = service.kmerFrequency("ATGATGATG", 3);
        assertEquals(3, freq.get("ATG"));
        assertEquals(2, freq.get("TGA"));
        assertEquals(2, freq.get("GAT"));
    }

    @Test
    @DisplayName("detectMutations should find position differences")
    void detectMutationsIsCorrect() {
        List<String> mutations = service.detectMutations("ATGC", "ATGA");
        assertEquals(1, mutations.size());
        assertTrue(mutations.get(0).contains("pos4"));
    }

    @Test
    @DisplayName("detectMutations with identical sequences should return empty")
    void detectMutationsIdenticalIsEmpty() {
        assertTrue(service.detectMutations("ATGC", "ATGC").isEmpty());
    }

    @Test
    @DisplayName("getLPS should delegate to KmpStrategy.buildLPS")
    void getLPSIsCorrect() {
        assertArrayEquals(new int[]{0, 0, 0, 0, 1, 2}, service.getLPS("ATGCAT"));
    }

    // ─── History persistence ─────────────────────────────────

    @Test
    @DisplayName("runSelected should save to history")
    void runSelectedSavesHistory() {
        service.runSelected("ATGCATGC", "ATGC", "KMP");
        verify(historyRepository, atLeastOnce()).save(any(SearchHistory.class));
    }

    @Test
    @DisplayName("runAll should save all results to history")
    void runAllSavesHistory() {
        service.runAll("ATGCATGC", "ATGC");
        verify(historyRepository, times(6)).save(any(SearchHistory.class));
    }
}

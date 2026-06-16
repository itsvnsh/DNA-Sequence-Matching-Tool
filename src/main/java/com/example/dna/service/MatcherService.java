package com.example.dna.service;

import com.example.dna.algorithms.KmpStrategy;
import com.example.dna.algorithms.StringMatchingStrategy;
import com.example.dna.model.MatchResult;
import com.example.dna.model.SearchHistory;
import com.example.dna.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for DNA pattern matching operations.
 *
 * Uses the Strategy Pattern: all algorithms are auto-discovered via Spring's
 * component scanning and injected as a Map keyed by their bean name.
 * This eliminates the switch-case and makes adding new algorithms trivial.
 */
@Service
public class MatcherService {

    private final Map<String, StringMatchingStrategy> strategies;
    private final SearchHistoryRepository historyRepository;

    /**
     * Spring auto-injects all beans implementing StringMatchingStrategy,
     * keyed by their @Component name (e.g. "KMP", "NAIVE").
     */
    public MatcherService(Map<String, StringMatchingStrategy> strategies,
                          SearchHistoryRepository historyRepository) {
        this.strategies = strategies;
        this.historyRepository = historyRepository;
    }

    /**
     * Run the selected algorithm on sanitized input.
     * If algo = "ALL", runs all registered algorithms.
     */
    public List<MatchResult> runSelected(String text, String pattern, String algo) {
        text = sanitize(text);
        pattern = sanitize(pattern);

        if (text.isEmpty() || pattern.isEmpty())
            throw new IllegalArgumentException("Text and pattern must contain valid DNA bases (A, T, C, G)");

        List<MatchResult> results;
        if ("ALL".equalsIgnoreCase(algo)) {
            results = runAll(text, pattern);
        } else {
            StringMatchingStrategy strategy = strategies.get(algo.toUpperCase());
            if (strategy == null)
                throw new IllegalArgumentException("Unknown algorithm: " + algo
                        + ". Available: " + strategies.keySet());
            results = List.of(strategy.search(text, pattern));
        }

        // Save to history
        String finalText = text;
        String finalPattern = pattern;
        results.forEach(r -> saveHistory(r, finalText.length(), finalPattern.length()));

        return results;
    }

    /** Run all registered algorithms for benchmarking */
    public List<MatchResult> runAll(String text, String pattern) {
        text = sanitize(text);
        pattern = sanitize(pattern);

        String finalText = text;
        String finalPattern = pattern;
        List<MatchResult> results = strategies.values().stream()
                .sorted(Comparator.comparing(StringMatchingStrategy::getName))
                .map(s -> s.search(finalText, finalPattern))
                .toList();

        results.forEach(r -> saveHistory(r, finalText.length(), finalPattern.length()));
        return results;
    }

    // ─── DNA Utility Methods ─────────────────────────────────

    public String complement(String dna) {
        return dna.toUpperCase().chars()
                .mapToObj(c -> String.valueOf(switch ((char) c) {
                    case 'A' -> 'T'; case 'T' -> 'A';
                    case 'C' -> 'G'; case 'G' -> 'C';
                    default -> 'N';
                })).collect(Collectors.joining());
    }

    public String reverseComplement(String dna) {
        return new StringBuilder(complement(dna)).reverse().toString();
    }

    public double gcContent(String dna) {
        dna = dna.toUpperCase();
        long gc = dna.chars().filter(c -> c == 'G' || c == 'C').count();
        return dna.isEmpty() ? 0 : (gc * 100.0 / dna.length());
    }

    public int hammingDistance(String s1, String s2) {
        s1 = s1.toUpperCase();
        s2 = s2.toUpperCase();
        if (s1.length() != s2.length())
            throw new IllegalArgumentException("Sequences must be equal length for Hamming distance");
        int dist = 0;
        for (int i = 0; i < s1.length(); i++)
            if (s1.charAt(i) != s2.charAt(i)) dist++;
        return dist;
    }

    public int editDistance(String s1, String s2) {
        int n = s1.length(), m = s2.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                dp[i][j] = s1.charAt(i - 1) == s2.charAt(j - 1)
                        ? dp[i - 1][j - 1]
                        : 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
        return dp[n][m];
    }

    public int[] getLPS(String pattern) {
        return KmpStrategy.buildLPS(pattern.toUpperCase());
    }

    public Map<String, Integer> kmerFrequency(String dna, int k) {
        dna = dna.toUpperCase();
        Map<String, Integer> freq = new TreeMap<>();
        for (int i = 0; i <= dna.length() - k; i++)
            freq.merge(dna.substring(i, i + k), 1, Integer::sum);
        return freq;
    }

    public List<String> detectMutations(String original, String mutated) {
        original = original.toUpperCase();
        mutated = mutated.toUpperCase();
        List<String> mutations = new ArrayList<>();
        int len = Math.min(original.length(), mutated.length());
        for (int i = 0; i < len; i++)
            if (original.charAt(i) != mutated.charAt(i))
                mutations.add(String.format("pos%d:%c>%c", i + 1, original.charAt(i), mutated.charAt(i)));
        return mutations;
    }

    // ─── Helpers ─────────────────────────────────────────────

    private String sanitize(String input) {
        return input.toUpperCase().replaceAll("[^ATCG]", "");
    }

    private void saveHistory(MatchResult result, int textLength, int patternLength) {
        try {
            SearchHistory history = new SearchHistory();
            history.setAlgorithm(result.getName());
            history.setTextLength(textLength);
            history.setPatternLength(patternLength);
            history.setMatchCount(result.getPositions().size());
            history.setComparisons(result.getComparisons());
            history.setTimeNs(result.getTimeNs());
            history.setTimestamp(LocalDateTime.now());
            historyRepository.save(history);
        } catch (Exception ignored) {
            // Don't let history persistence failures break the main flow
        }
    }
}

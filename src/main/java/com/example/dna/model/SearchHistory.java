package com.example.dna.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity for persisting search history in the H2 database.
 * Each row represents one algorithm execution.
 */
@Entity
@Table(name = "search_history")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String algorithm;

    private int textLength;
    private int patternLength;
    private int matchCount;
    private int comparisons;
    private long timeNs;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // ─── Getters & Setters ───────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getAlgorithm()               { return algorithm; }
    public void setAlgorithm(String algorithm)  { this.algorithm = algorithm; }

    public int getTextLength()                 { return textLength; }
    public void setTextLength(int textLength)  { this.textLength = textLength; }

    public int getPatternLength()              { return patternLength; }
    public void setPatternLength(int patternLength) { this.patternLength = patternLength; }

    public int getMatchCount()                 { return matchCount; }
    public void setMatchCount(int matchCount)  { this.matchCount = matchCount; }

    public int getComparisons()                { return comparisons; }
    public void setComparisons(int comparisons) { this.comparisons = comparisons; }

    public long getTimeNs()                    { return timeNs; }
    public void setTimeNs(long timeNs)         { this.timeNs = timeNs; }

    public LocalDateTime getTimestamp()        { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

package com.example.dna.repository;

import com.example.dna.model.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for SearchHistory.
 * Provides CRUD + custom query methods for search history persistence.
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /** Get recent searches, ordered newest first */
    List<SearchHistory> findTop50ByOrderByTimestampDesc();

    /** Find history entries for a specific algorithm */
    List<SearchHistory> findByAlgorithmOrderByTimestampDesc(String algorithm);
}

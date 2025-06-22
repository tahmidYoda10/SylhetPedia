package com.sylhetpedia.backend.repository;

import com.sylhetpedia.backend.model.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    List<FoundItem> findByApprovedTrue();  // Return only approved found items
    List<FoundItem> findByApprovedFalse(); // Return only unapproved found items

    // Delete a found item by ID
    void deleteById(Long id);
}

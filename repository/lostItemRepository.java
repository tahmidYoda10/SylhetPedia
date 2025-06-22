package com.sylhetpedia.backend.repository;

import com.sylhetpedia.backend.model.lostItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface lostItemRepository extends JpaRepository<lostItem, Long> {

    // Get all approved lost items (approved = true)
    List<lostItem> findByApprovedTrue();

    // Get all unapproved lost items (approved = false)
    List<lostItem> findByApprovedFalse();

    // Delete a lost item by its ID (inherited from JpaRepository)
    void deleteById(Long id);
}

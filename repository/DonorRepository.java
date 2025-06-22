package com.sylhetpedia.backend.repository;

import com.sylhetpedia.backend.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    // Find donors who are approved (approved = true)
    List<Donor> findByApprovedTrue();

    // Find donors who are not approved (approved = false)
    List<Donor> findByApprovedFalse();

    // Find a donor by their ID (inherited from JpaRepository)
    Optional<Donor> findById(Long id);

    // Custom delete method, in case you need specific logic for deletion (e.g., logging or cascading)
    void deleteById(Long id); // This method is inherited from JpaRepository
}

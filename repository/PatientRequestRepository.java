package com.sylhetpedia.backend.repository;

import com.sylhetpedia.backend.model.PatientRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRequestRepository extends JpaRepository<PatientRequest, Long> {

    // Find all approved patient requests (approved = true)
    List<PatientRequest> findByApprovedTrue();

    // Find all pending patient requests (approved = false)
    List<PatientRequest> findByApprovedFalse();

    // Delete a patient request by its ID (inherited from JpaRepository)
    void deleteById(Long id);
}

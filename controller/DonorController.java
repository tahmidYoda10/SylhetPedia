package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.model.Donor;
import com.sylhetpedia.backend.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
public class DonorController {

    @Autowired
    private DonorRepository donorRepository;

    // Submit a donor request, defaulting the approval to false
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_DONOR')")  // Ensures only donors can submit requests
    public ResponseEntity<String> submitDonor(@RequestBody Donor donor) {
        donor.setApproved(false);  // Default to false until admin approves
        donorRepository.save(donor);
        return ResponseEntity.ok("Donor request submitted. Pending Admin Approval.");
    }

    // Get all approved donors
    @GetMapping("/approved")
    public ResponseEntity<List<Donor>> getApproved() {
        List<Donor> approvedDonors = donorRepository.findByApprovedTrue();
        return ResponseEntity.ok(approvedDonors);
    }

    // Get all pending donors
    @GetMapping("/pending")
    public ResponseEntity<List<Donor>> getPending() {
        List<Donor> pendingDonors = donorRepository.findByApprovedFalse();
        return ResponseEntity.ok(pendingDonors);
    }

    // Approve a donor request by ID (Only Admin can approve)
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can approve donors
    public ResponseEntity<String> approve(@PathVariable Long id) {
        Donor donor = donorRepository.findById(id).orElseThrow(() -> new RuntimeException("Donor not found"));
        donor.setApproved(true);
        donorRepository.save(donor);
        return ResponseEntity.ok("Donor request approved.");
    }

    // Delete a donor request by ID (Only Admin or the donor themselves can delete their request)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_DONOR') and @permissionEvaluator.hasPermissionToDelete(#id))")
    public ResponseEntity<String> deleteDonor(@PathVariable Long id) {
        Donor donor = donorRepository.findById(id).orElseThrow(() -> new RuntimeException("Donor not found"));

        // Check if the donor is approved
        if (!donor.isApproved()) {
            return ResponseEntity.status(400).body("Cannot delete a pending request.");
        }

        // Delete the donor request
        donorRepository.deleteById(id);
        return ResponseEntity.ok("Donor request deleted.");
    }
}

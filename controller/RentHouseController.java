package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.model.RentHouse;
import com.sylhetpedia.backend.repository.RentHouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rent-houses")
public class RentHouseController {

    @Autowired
    private RentHouseRepository rentHouseRepository;

    // Submit a rent house listing, defaulting the approval to false
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_USER')")  // Ensures only authenticated users can submit a listing
    public ResponseEntity<String> submitRentHouse(@RequestBody RentHouse rentHouse) {
        rentHouse.setApproved(false);  // Set approval to false by default (pending)
        rentHouseRepository.save(rentHouse);
        return ResponseEntity.ok("Rent house listing submitted. Pending Admin Approval.");
    }

    // Get all approved rent houses
    @GetMapping("/approved")
    public ResponseEntity<List<RentHouse>> getApprovedRentHouses() {
        List<RentHouse> approvedRentHouses = rentHouseRepository.findByApprovedTrue();
        return ResponseEntity.ok(approvedRentHouses);
    }

    // Get all pending rent houses
    @GetMapping("/pending")
    public ResponseEntity<List<RentHouse>> getPendingRentHouses() {
        List<RentHouse> pendingRentHouses = rentHouseRepository.findByApprovedFalse();
        return ResponseEntity.ok(pendingRentHouses);
    }

    // Approve a rent house listing by ID (Only Admin can approve)
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can approve rent house listings
    public ResponseEntity<String> approveRentHouse(@PathVariable Long id) {
        RentHouse rentHouse = rentHouseRepository.findById(id).orElseThrow(() -> new RuntimeException("Rent house not found"));
        rentHouse.setApproved(true);  // Mark as approved
        rentHouseRepository.save(rentHouse);
        return ResponseEntity.ok("Rent house listing approved.");
    }

    // Admin side: Decline a pending rent house listing by ID
    @PostMapping("/decline/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can decline rent house listings
    public ResponseEntity<String> declineRentHouse(@PathVariable Long id) {
        RentHouse rentHouse = rentHouseRepository.findById(id).orElseThrow(() -> new RuntimeException("Rent house not found"));

        // Set approval to false and save the rent house object
        rentHouse.setApproved(false);
        rentHouseRepository.save(rentHouse);

        return ResponseEntity.ok("Rent house listing declined.");
    }

    // Admin or user can delete a rent house listing (only approved ones)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @permissionEvaluator.hasPermissionToDelete(#id))")  // Admin or the user who created the request can delete
    public ResponseEntity<String> deleteRentHouse(@PathVariable Long id) {
        RentHouse rentHouse = rentHouseRepository.findById(id).orElseThrow(() -> new RuntimeException("Rent house not found"));

        // Check if the rent house is approved
        if (!rentHouse.isApproved()) {
            return ResponseEntity.status(400).body("Cannot delete a pending request.");
        }

        // Delete the rent house
        rentHouseRepository.deleteById(id); // Correctly using 'rentHouseRepository' to delete the rent house
        return ResponseEntity.ok("Rent house listing deleted.");
    }
}

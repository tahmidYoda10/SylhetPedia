package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.model.lostItem;
import com.sylhetpedia.backend.repository.lostItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lostitem")
public class lostitemController {

    @Autowired
    private lostItemRepository lostrepo;

    // Submit a request
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_LOOSER')") // Ensure only users with 'ROLE_LOOSER' can submit
    public ResponseEntity<String> submitLostItemReq(@RequestBody lostItem lost) {
        lost.setApproved(false); // Default approval status is false
        lostrepo.save(lost); // Correctly using 'lostrepo' to save the lost item
        return ResponseEntity.ok("Lost Item listing submitted. Pending Admin Approval.");
    }

    // Get all approved lost items
    @GetMapping("/approved")
    public ResponseEntity<List<lostItem>> getApprovedLostItems() {
        List<lostItem> approvedLostItems = lostrepo.findByApprovedTrue();
        return ResponseEntity.ok(approvedLostItems);
    }

    // Get all pending lost items
    @GetMapping("/pending")
    public ResponseEntity<List<lostItem>> getPendingLostItems() {
        List<lostItem> pendingLostItems = lostrepo.findByApprovedFalse();
        return ResponseEntity.ok(pendingLostItems);
    }

    // Admin side: Approve a pending lost item by ID
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can approve lost item listings
    public ResponseEntity<String> approveLostItem(@PathVariable Long id) {
        lostItem lostItem = lostrepo.findById(id).orElseThrow(() -> new RuntimeException("Lost item not found"));
        lostItem.setApproved(true);  // Mark as approved
        lostrepo.save(lostItem); // Save the updated lost item
        return ResponseEntity.ok("Lost item listing approved.");
    }

    // Admin side: Decline a pending lost item by ID
    @PostMapping("/decline/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can decline lost item listings
    public ResponseEntity<String> declineLostItem(@PathVariable Long id) {
        lostItem lostItem = lostrepo.findById(id).orElseThrow(() -> new RuntimeException("Lost item not found"));

        // Set approval to false and save the lost item object
        lostItem.setApproved(false);
        lostrepo.save(lostItem);

        return ResponseEntity.ok("Lost item listing declined.");
    }

    // Admin or user (finder) can delete a lost item request (only approved ones)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_LOOSER') and @permissionEvaluator.hasPermissionToDelete(#id))")  // Admin or the user who created the request can delete
    public ResponseEntity<String> deleteLostItem(@PathVariable Long id) {
        lostItem lostItem = lostrepo.findById(id).orElseThrow(() -> new RuntimeException("Lost item not found"));

        // Check if the lost item is approved
        if (!lostItem.isApproved()) {
            return ResponseEntity.status(400).body("Cannot delete a pending request.");
        }

        // Delete the lost item
        lostrepo.deleteById(id); // Correctly using 'lostrepo' to delete the lost item
        return ResponseEntity.ok("Lost item listing deleted.");
    }
}

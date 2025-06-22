package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.model.FoundItem;
import com.sylhetpedia.backend.repository.FoundItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/founditem")
public class FoundItemController {

    @Autowired
    private FoundItemRepository foundrepo; // injected repository, non-static

    // Submit a found item request
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_FINDER')") // Ensure only users with 'ROLE_FINDER' can submit
    public ResponseEntity<String> submitFoundItemReq(@RequestBody FoundItem found) {
        found.setApproved(false); // Default approval status is false
        foundrepo.save(found); // Save the found item in the repository
        return ResponseEntity.ok("Found Item listing submitted. Pending Admin Approval.");
    }

    // Get all approved found items
    @GetMapping("/approved")
    public ResponseEntity<List<FoundItem>> getApprovedFoundItems() {
        List<FoundItem> approvedFoundItems = foundrepo.findByApprovedTrue();
        return ResponseEntity.ok(approvedFoundItems);
    }

    // Get all pending found items
    @GetMapping("/pending")
    public ResponseEntity<List<FoundItem>> getPendingFoundItems() {
        List<FoundItem> pendingFoundItems = foundrepo.findByApprovedFalse();
        return ResponseEntity.ok(pendingFoundItems);
    }

    // Admin side: Approve a pending found item by ID
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can approve found item listings
    public ResponseEntity<String> approveFoundItem(@PathVariable Long id) {
        FoundItem foundItem = foundrepo.findById(id).orElseThrow(() -> new RuntimeException("Found item not found"));
        foundItem.setApproved(true);  // Mark the item as approved
        foundrepo.save(foundItem); // Save the updated found item
        return ResponseEntity.ok("Found item listing approved.");
    }

    // Admin side: Decline a pending found item by ID
    @PostMapping("/decline/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can decline found item listings
    public ResponseEntity<String> declineFoundItem(@PathVariable Long id) {
        FoundItem foundItem = foundrepo.findById(id).orElseThrow(() -> new RuntimeException("Found item not found"));

        // Set approval to false and save the found item object
        foundItem.setApproved(false);
        foundrepo.save(foundItem);

        return ResponseEntity.ok("Found item listing declined.");
    }

    // Admin or finder: Delete a found item request (only approved ones)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_FINDER') and @permissionEvaluator.hasPermissionToDelete(#id))")  // Admin or the user can delete
    public ResponseEntity<String> deleteFoundItem(@PathVariable Long id) {
        // Correctly using foundrepo to find and delete the found item
        FoundItem foundItem = foundrepo.findById(id).orElseThrow(() -> new RuntimeException("Found item not found"));

        // Check if the found item is approved
        if (!foundItem.isApproved()) {
            return ResponseEntity.status(400).body("Cannot delete a pending request.");
        }

        // Delete the found item
        foundrepo.deleteById(id); // Correctly using foundrepo to delete the found item
        return ResponseEntity.ok("Found item listing deleted.");
    }
}

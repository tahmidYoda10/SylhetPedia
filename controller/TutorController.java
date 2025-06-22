package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.model.Tutor;
import com.sylhetpedia.backend.repository.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutor")
public class TutorController {

    @Autowired
    private TutorRepository tutorRepository;

    // Submit a tutor request, defaulting the approval to false
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_TUTOR')")  // Ensures only tutors can submit requests
    public ResponseEntity<String> submitTutorRequest(@RequestBody Tutor tutor) {
        tutor.setApproved(false);  // Default to false until admin approves
        tutorRepository.save(tutor);
        return ResponseEntity.ok("Tutor request submitted. Pending Admin Approval.");
    }

    // Get all approved tutors
    @GetMapping("/approved")
    public ResponseEntity<List<Tutor>> getApprovedTutors() {
        List<Tutor> approvedTutors = tutorRepository.findByApprovedTrue();
        return ResponseEntity.ok(approvedTutors);
    }

    // Get all pending tutors
    @GetMapping("/pending")
    public ResponseEntity<List<Tutor>> getPendingTutors() {
        List<Tutor> pendingTutors = tutorRepository.findByApprovedFalse();
        return ResponseEntity.ok(pendingTutors);
    }

    // Approve a tutor by their ID (Only Admin can approve)
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can approve tutors
    public ResponseEntity<String> approveTutor(@PathVariable Long id) {
        Tutor tutor = tutorRepository.findById(id).orElseThrow(() -> new RuntimeException("Tutor not found"));
        tutor.setApproved(true);
        tutorRepository.save(tutor);
        return ResponseEntity.ok("Tutor approved.");
    }

    // Admin side: Decline a pending tutor request by ID
    @PostMapping("/decline/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can decline tutor requests
    public ResponseEntity<String> declineTutor(@PathVariable Long id) {
        Tutor tutor = tutorRepository.findById(id).orElseThrow(() -> new RuntimeException("Tutor not found"));

        // Set approval to false and save the tutor object
        tutor.setApproved(false);
        tutorRepository.save(tutor);

        return ResponseEntity.ok("Tutor request declined.");
    }

    // Admin or the user who submitted the request can delete the approved tutor request
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_TUTOR') and @permissionEvaluator.hasPermissionToDelete(#id))")  // Admin or the user who created the request can delete
    public ResponseEntity<String> deleteTutor(@PathVariable Long id) {
        Tutor tutor = tutorRepository.findById(id).orElseThrow(() -> new RuntimeException("Tutor not found"));

        // Check if the tutor request is approved
        if (!tutor.isApproved()) {
            return ResponseEntity.status(400).body("Cannot delete a pending request.");
        }

        // Delete the tutor request
        tutorRepository.deleteById(id);  // Delete the tutor request from the repository
        return ResponseEntity.ok("Tutor request deleted.");
    }
}


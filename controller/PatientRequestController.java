package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.model.PatientRequest;
import com.sylhetpedia.backend.repository.PatientRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/requests")
public class PatientRequestController {

    @Autowired
    private PatientRequestRepository patientRequestRepo;

    // Submit a patient request, defaulting the approval to false
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_PATIENT')")  // Ensures only PATIENTs can submit requests
    public ResponseEntity<String> submitPatientRequest(@RequestBody PatientRequest patientRequest) {
        patientRequest.setApproved(false);  // Setting the approval flag to false initially
        patientRequestRepo.save(patientRequest);
        return ResponseEntity.ok("Patient request submitted. Pending Admin Approval.");
    }

    // Get all approved patient requests
    @GetMapping("/approved")
    public ResponseEntity<List<PatientRequest>> getApprovedRequests() {
        List<PatientRequest> approvedRequests = patientRequestRepo.findByApprovedTrue();
        return ResponseEntity.ok(approvedRequests);
    }

    // Get all pending patient requests
    @GetMapping("/pending")
    public ResponseEntity<List<PatientRequest>> getPendingRequests() {
        List<PatientRequest> pendingRequests = patientRequestRepo.findByApprovedFalse();
        return ResponseEntity.ok(pendingRequests);
    }

    // Approve a patient request by ID (Only Admin can approve)
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can approve requests
    public ResponseEntity<String> approveRequest(@PathVariable Long id) {
        PatientRequest patientRequest = patientRequestRepo.findById(id).orElseThrow(() -> new RuntimeException("Patient request not found"));
        patientRequest.setApproved(true);
        patientRequestRepo.save(patientRequest);
        return ResponseEntity.ok("Patient request approved.");
    }

    // Admin side: Decline a pending patient request by ID
    @PostMapping("/decline/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")  // Only admins can decline patient requests
    public ResponseEntity<String> declineRequest(@PathVariable Long id) {
        PatientRequest patientRequest = patientRequestRepo.findById(id).orElseThrow(() -> new RuntimeException("Patient request not found"));

        // Set approval to false and save the patient request object
        patientRequest.setApproved(false);
        patientRequestRepo.save(patientRequest);

        return ResponseEntity.ok("Patient request declined.");
    }

    // Admin or the user who submitted the request can delete the approved request
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_PATIENT') and @permissionEvaluator.hasPermissionToDelete(#id))")  // Admin or the patient can delete
    public ResponseEntity<String> deletePatientRequest(@PathVariable Long id) {
        PatientRequest patientRequest = patientRequestRepo.findById(id).orElseThrow(() -> new RuntimeException("Patient request not found"));

        // Check if the patient request is approved
        if (!patientRequest.isApproved()) {
            return ResponseEntity.status(400).body("Cannot delete a pending request.");
        }

        // Delete the patient request
        patientRequestRepo.deleteById(id);  // Delete the patient request from the repository
        return ResponseEntity.ok("Patient request deleted.");
    }
}

package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.service.OtpService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse> sendOtp(@RequestBody EmailRequest emailRequest) {
        try {
            otpService.generateOtp(emailRequest.getEmail());
            return ResponseEntity.ok(new ApiResponse("OTP sent to your email", true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse("Failed to send OTP: " + e.getMessage(), false));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody VerifyRequest request) {
        try {
            boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
            if (isValid) {
                return ResponseEntity.ok(new ApiResponse("OTP verified successfully", true));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse("Invalid or expired OTP", false));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse("OTP verification failed: " + e.getMessage(), false));
        }
    }

    @Data
    public static class EmailRequest {
        private String email;
    }

    @Data
    public static class VerifyRequest {
        private String email;
        private String otp;
    }

    @Data
    public static class ApiResponse {
        private final String message;
        private final boolean success;
    }
}

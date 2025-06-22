package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.model.Otp;
import com.sylhetpedia.backend.model.OtpVerificationRequest;
import com.sylhetpedia.backend.model.RefreshToken;
import com.sylhetpedia.backend.model.User;
import com.sylhetpedia.backend.repository.OtpRepository;
import com.sylhetpedia.backend.repository.RefreshTokenRepository;
import com.sylhetpedia.backend.repository.UserRepository;
import com.sylhetpedia.backend.security.JwtUtil;
import com.sylhetpedia.backend.service.OtpService;
import com.sylhetpedia.backend.service.RefreshTokenService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private OtpService otpService;

    // Admin SignUp
    @PostMapping("/admin/signup")
    public ResponseEntity<ApiResponse> adminSignup(@RequestBody AdminSignupRequest adminRequest) {
        try {
            // List of allowed email addresses
            List<String> allowedEmails = Arrays.asList("admin3@example.com", "admin4@example.com");

            // Check if the email is in the allowed emails list
            if (!allowedEmails.contains(adminRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse("This email is not allowed for signup", false));
            }

            // Check if email already exists in the database
            if (userRepository.findByEmail(adminRequest.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse("Email already exists", false));
            }

            // Encrypt password using BCryptPasswordEncoder
            User admin = new User();
            admin.setEmail(adminRequest.getEmail());
            admin.setUsername(adminRequest.getEmail());  // Using email as the username
            admin.setPassword(passwordEncoder.encode(adminRequest.getPassword())); // Hashing the password

            // Set the role to ADMIN
            admin.setRole(User.Role.ADMIN);

            // Save admin user to the database
            userRepository.save(admin);

            return ResponseEntity.ok(new ApiResponse("Admin registered successfully", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Server error: " + e.getMessage(), false));
        }
    }

    // Admin Login
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            if (userOpt.get().getRole() == User.Role.ADMIN) {
                String accessToken = jwtUtil.generateAccessToken(request.getUsername());
                RefreshToken refreshToken = refreshTokenService.createToken(request.getUsername());
                return ResponseEntity.ok(new JwtLoginResponse("Admin login successful", accessToken, refreshToken.getToken()));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse("You are not authorized", false));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Invalid credentials", false));
    }

    // Admin Logout
    @Transactional
    @PostMapping("/admin/logout")
    public ResponseEntity<ApiResponse> adminLogout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract the token from the Authorization header (assuming Bearer token format)
            String token = authorizationHeader.substring(7);

            // Delete the refresh token associated with the admin's token
            boolean tokenDeleted = refreshTokenService.deleteTokenByToken(token);  // Using deleteTokenByToken logic

            if (tokenDeleted) {
                return ResponseEntity.ok(new ApiResponse("Admin logout successful", true));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse("Invalid or expired token", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Admin logout failed: " + e.getMessage(), false));
        }
    }

    // User SignUp
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> register(@RequestBody User user) {
        try {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse("Email already exists", false));
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            otpService.generateOtp(user.getEmail());
            return ResponseEntity.ok(new ApiResponse("Registered successfully. Please verify your email using the OTP.", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Server error: " + e.getMessage(), false));
        }
    }

    // User Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            String accessToken = jwtUtil.generateAccessToken(request.getEmail());
            RefreshToken refreshToken = refreshTokenService.createToken(request.getEmail());
            return ResponseEntity.ok(new JwtLoginResponse("Login successful", accessToken, refreshToken.getToken()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Invalid credentials", false));
    }

    // Request OTP for password reset
    @PostMapping("/request-reset-otp")
    public ResponseEntity<ApiResponse> requestResetOtp(@RequestBody EmailRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Email not found", false));
        }
        otpService.generateOtp(request.getEmail());
        return ResponseEntity.ok(new ApiResponse("OTP sent to email", true));
    }

    // Verify OTP for password reset
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody OtpVerificationRequest request) {
        Optional<Otp> otpRecord = otpRepository.findByEmailAndOtp(request.getEmail(), request.getOtp());
        if (otpRecord.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Invalid OTP or email", false));
        }
        if (otpRecord.get().getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpRecord.get());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("OTP expired", false));
        }
        return ResponseEntity.ok(new ApiResponse("OTP verified successfully", true));
    }

    // Change Password without OTP
    @Transactional
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("User not found with the given email", false));
        }
        User user = optionalUser.get();
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("New password and confirm password do not match", false));
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse("Password reset successful", true));
    }

    // Logout
    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7);
            boolean tokenDeleted = refreshTokenService.deleteTokenByToken(token);
            if (tokenDeleted) {
                return ResponseEntity.ok(new ApiResponse("Logout successful", true));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse("Invalid or expired token", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Logout failed: " + e.getMessage(), false));
        }
    }

    // Refresh Access Token
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        Optional<RefreshToken> tokenOpt = refreshTokenService.validateToken(request.getRefreshToken());
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Invalid or expired refresh token", false));
        }
        String newAccessToken = jwtUtil.generateAccessToken(tokenOpt.get().getEmail());
        return ResponseEntity.ok(new JwtAccessResponse("Access token refreshed", newAccessToken));
    }

    // DTO classes
    @Data
    public static class AdminSignupRequest {
        private String email;
        private String username;
        private String password;
    }

    @Data
    public static class AdminLoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class JwtLoginResponse {
        private String message;
        private String accessToken;
        private String refreshToken;
    }

    @Data
    public static class TokenRefreshRequest {
        private String refreshToken;
    }

    @Data
    @AllArgsConstructor
    public static class JwtAccessResponse {
        private String message;
        private String accessToken;
    }

    @Data
    public static class ResetPasswordRequest {
        private String email;
        private String newPassword;
        private String confirmPassword;
    }

    @Data
    public static class EmailRequest {
        private String email;
    }

    @Data
    @AllArgsConstructor
    public static class ApiResponse {
        private String message;
        private Boolean success;
    }
}

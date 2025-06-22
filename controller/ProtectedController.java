package com.sylhetpedia.backend.controller;

import com.sylhetpedia.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProtectedController {

    @Autowired
    private JwtUtil jwtUtil;  // Inject JwtUtil

    @GetMapping("/protected-endpoint")
    public ResponseEntity<String> protectedEndpoint(@RequestHeader("Authorization") String authorizationHeader) {
        // Check if the token is valid
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: No token provided");
        }

        String token = authorizationHeader.substring(7); // Extract token from Bearer string
        if (jwtUtil.validateToken(token)) {  // Call validateToken() using the injected jwtUtil
            return ResponseEntity.ok("You are authenticated! Access granted.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid token");
        }
    }
}

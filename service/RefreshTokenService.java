package com.sylhetpedia.backend.service;

import com.sylhetpedia.backend.model.RefreshToken;
import com.sylhetpedia.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // Method to create a new refresh token
    public RefreshToken createToken(String email) {
        // Delete any existing refresh token for the user before creating a new one
        refreshTokenRepository.deleteByEmail(email);

        // Create a new refresh token
        RefreshToken token = new RefreshToken();
        token.setEmail(email);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusDays(7));  // Token expiry is 7 days from now

        // Save and return the token
        return refreshTokenRepository.save(token);
    }

    // Method to validate the refresh token
    public Optional<RefreshToken> validateToken(String token) {
        // Fetch the token from the repository and check if it is still valid (not expired)
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    // Method to delete a refresh token by its token string
    public boolean deleteTokenByToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isPresent()) {
            refreshTokenRepository.delete(refreshTokenOpt.get());
            return true;  // Token deleted successfully
        }

        return false;  // Token not found or already deleted
    }

    // Method to delete refresh token by email (used in logout functionality)
    public void deleteByEmail(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }
}

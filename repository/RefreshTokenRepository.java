package com.sylhetpedia.backend.repository;

import com.sylhetpedia.backend.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Repository to interact with RefreshToken entities
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Find RefreshToken by its token
    Optional<RefreshToken> findByToken(String token);

    // Find RefreshToken by the user's email
    Optional<RefreshToken> findByEmail(String email);

    // Delete RefreshToken by the user's email (for logout functionality)
    void deleteByEmail(String email);
}

package com.sylhetpedia.backend.repository;

import com.sylhetpedia.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);  // Existing method
    Optional<User> findByUsername(String username);  // New method to find by username
}

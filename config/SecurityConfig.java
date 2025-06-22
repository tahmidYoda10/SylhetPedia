package com.sylhetpedia.backend.config;

import com.sylhetpedia.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Constructor Injection of the JWT filter
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Permit all these endpoints for unauthenticated access
                        .requestMatchers("/api/auth/**", "/api/otp/**", "/api/home/**").permitAll()

                        // Restrict access based on roles
                        .requestMatchers("/api/donors/submit", "/api/patients/requests/submit")
                        .hasAnyRole("DONOR", "PATIENT")  // Donors and Patients can submit requests

                        .requestMatchers("/api/donors/approve/**", "/api/patients/requests/approve/**")
                        .hasRole("ADMIN")  // Only ADMIN can approve requests

                        .requestMatchers("/api/donors/approved", "/api/patients/requests/approved")
                        .hasAnyRole("DONOR", "ADMIN")  // Donor can view approved list, Admin can also view

                        .requestMatchers("/api/donors/pending", "/api/patients/requests/pending")
                        .hasRole("ADMIN")  // Only Admin can view pending requests

                        // Fallback: Any other request requires authentication
                        .anyRequest().authenticated()
                )
                // Register the JWT filter before the UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(httpBasic -> httpBasic.disable())  // Disable HTTP basic authentication
                .formLogin(form -> form.disable())  // Disable form-based login
                .build();
    }

    // Password encoder for hashing passwords (needed for authentication)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

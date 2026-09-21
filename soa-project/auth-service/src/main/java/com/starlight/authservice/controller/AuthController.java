package com.starlight.authservice.controller;

import com.starlight.authservice.entity.User;
import com.starlight.authservice.repository.UserRepository;
import com.starlight.authservice.security.JwtUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        // Check whether email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {

            return ResponseEntity.badRequest()
                    .body("Email already registered");
        }

        // Encrypt password before saving
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        // Save user into database
        User savedUser = userRepository.save(user);

        // Do not return password in response
        savedUser.setPassword(null);

        return ResponseEntity.ok(savedUser);
    }

    // Login existing user
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        // Find user by email
        User existingUser = userRepository
                .findByEmail(user.getEmail())
                .orElse(null);

        // User not found
        if (existingUser == null) {

            return ResponseEntity.status(401)
                    .body("Invalid email or password");
        }

        // Check password
        boolean passwordMatches = passwordEncoder.matches(
                user.getPassword(),
                existingUser.getPassword()
        );

        // Wrong password
        if (!passwordMatches) {

            return ResponseEntity.status(401)
                    .body("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(
                existingUser.getEmail()
        );

        // Return token to client
        return ResponseEntity.ok(token);
    }
}
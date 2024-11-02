package com.ims.controllers;

import com.ims.models.CustomUserDetails;
import com.ims.models.LoginResponse;
import com.ims.models.User;
import com.ims.models.dtos.LoginUserDto;
import com.ims.repository.UserRepository;
import com.ims.services.AuthenticationService;
import com.ims.services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserDto loginRequest) {
        try {
            log.info("Login attempt for email: {}", loginRequest.getEmail());
            String token = authService.login(loginRequest);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            log.error("Login error for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Add a test endpoint to verify the setup
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        try {
            return ResponseEntity.ok(Map.of(
                    "message", "Auth endpoint is accessible",
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Test endpoint error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/auth/debug")
    public ResponseEntity<?> debug(@RequestParam String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Don't expose the actual password in production!
            return ResponseEntity.ok(Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "passwordEncoded", user.getPassword().startsWith("$2a$")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
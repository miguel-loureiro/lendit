package com.ims.services;

import com.ims.models.User;
import com.ims.models.dtos.request.LoginUserDto;
import com.ims.repository.UserRepository;
import com.ims.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String login(LoginUserDto loginRequest) {
        try {
            log.info("Starting authentication process for email: {}", loginRequest.getEmail());

            // First verify the user exists
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

            log.info("User found in database: {}", user.getUsername());

            // Verify password manually first
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.error("Password doesn't match for user: {}", user.getEmail());
                throw new BadCredentialsException("Invalid password");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            log.info("Authentication successful for user: {}", user.getEmail());
            return jwtService.generateToken(user);

        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", loginRequest.getEmail());
            throw e;
        } catch (BadCredentialsException e) {
            log.error("Bad credentials: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
}
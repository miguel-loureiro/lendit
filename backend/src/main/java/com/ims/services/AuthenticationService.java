package com.ims.services;

import com.ims.models.Role;
import com.ims.models.User;
import com.ims.models.dtos.request.LoginUserDto;
import com.ims.models.dtos.request.SignupUserDto;
import com.ims.models.dtos.response.LoginResponse;
import com.ims.models.dtos.response.UserSignedDto;
import com.ims.repository.UserRepository;
import com.ims.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationServiceException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginUserDto loginRequest) {
        log.info("Starting authentication process for username: {}", loginRequest.getUsername());
        try {
            // First verify the user exists
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            log.info("User found in database: {}", user.getUsername());

            // Verify password manually first
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.error("Password doesn't match for user: {}", user.getEmail());
                throw new BadCredentialsException("Invalid credentials");
            }

            // Perform the authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtService.generateToken(user);

            log.info("Authentication successful for user: {}", user.getUsername());

            return LoginResponse.builder()
                    .token(jwt)
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build();

        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new BadCredentialsException("Invalid credentials");
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            throw new AuthenticationServiceException("Authentication failed", e);
        }
    }

    public LoginResponse signup(SignupUserDto signupRequest) {
        log.info("Starting signup process for username: {}", signupRequest.getUsername());
        try {
            // Check if user already exists with the given username
            if(userRepository.findByUsername(signupRequest.getUsername()).isPresent()){
                log.error("User already exists with username: {}", signupRequest.getUsername());
                throw new IllegalArgumentException("Email already registered");
            }

            // Check if user already exists with the given email
            if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
                log.error("User already exists with email: {}", signupRequest.getEmail());
                throw new IllegalArgumentException("Email already registered");
            }

            // Create new user entity
            User user = new User(
                    signupRequest.getUsername(),
                    signupRequest.getEmail(),
                    passwordEncoder.encode(signupRequest.getPassword()),
                    Role.CLIENT
            );

            // Set profile image if provided
            if (signupRequest.getProfileImage() != null) {
                user.setProfileImage(signupRequest.getProfileImage());
            }

            // Save the user
            User savedUser = userRepository.save(user);
            log.info("User successfully created with email: {}", savedUser.getEmail());

            // Perform login
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signupRequest.getUsername(),
                            signupRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtService.generateToken(savedUser);
            log.info("Authentication successful for new user: {}", savedUser.getUsername());

            // Return login response with token
            return LoginResponse.builder()  // Assuming LoginResponse still uses builder pattern
                    .token(jwt)
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .username(savedUser.getUsername())
                    .role(savedUser.getRole())
                    .build();

        } catch (IllegalArgumentException e) {
            log.error("Signup failed: {}", e.getMessage());
            throw e;
        } catch (BadCredentialsException e) {
            log.error("Authentication failed after signup for user: {}", signupRequest.getEmail());
            throw new BadCredentialsException("Authentication failed after signup");
        } catch (Exception e) {
            log.error("Unexpected error during signup/login", e);
            throw new AuthenticationServiceException("Signup/Login failed", e);
        }
    }
}
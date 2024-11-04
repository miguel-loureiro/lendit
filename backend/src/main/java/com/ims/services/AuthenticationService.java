package com.ims.services;

import com.ims.models.User;
import com.ims.models.dtos.request.LoginUserDto;
import com.ims.models.dtos.response.LoginResponse;
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
        log.info("Starting authentication process for email: {}", loginRequest.getEmail());
        try {
            // First verify the user exists
            User user = userRepository.findByEmail(loginRequest.getEmail())
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
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtService.generateToken(user);

            log.info("Authentication successful for user: {}", user.getEmail());

            return LoginResponse.builder()
                    .token(jwt)
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build();

        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            throw new AuthenticationServiceException("Authentication failed", e);
        }
    }
}
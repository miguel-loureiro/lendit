package com.ims.services;

import com.ims.models.Role;
import com.ims.models.User;
import com.ims.models.dtos.request.LoginUserDto;
import com.ims.models.dtos.response.LoginResponse;
import com.ims.repository.UserRepository;
import com.ims.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private LoginUserDto validLoginRequest;
    private User user;
    private final String JWT_TOKEN = "testJwtToken";

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginUserDto("test@example.com", "Password123@");
        user = new User("testUser", "test@example.com", "encodedPassword", Role.CLIENT);
        user.setId(1);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // Arrange
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), user.getPassword())).thenReturn(true);

        // Create a specific instance of UsernamePasswordAuthenticationToken
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(validLoginRequest.getEmail(), validLoginRequest.getPassword());

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(authRequest)).thenReturn(authentication);
        when(jwtService.generateToken(user)).thenReturn(JWT_TOKEN);

        // Act
        LoginResponse response = authenticationService.login(validLoginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getRole(), response.getRole());

        verify(userRepository).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), user.getPassword());
        verify(authenticationManager).authenticate(authRequest);
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowBadCredentialsException() {
        // Arrange
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.login(validLoginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), user.getPassword());
        verifyNoInteractions(authenticationManager, jwtService);
    }

    @Test
    void login_WithNonexistentUser_ShouldThrowBadCredentialsException() {
        // Arrange
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.login(validLoginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByEmail(validLoginRequest.getEmail());
        verifyNoInteractions(passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void login_WithUnexpectedException_ShouldThrowAuthenticationServiceException() {
        // Arrange
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        AuthenticationServiceException exception = assertThrows(
                AuthenticationServiceException.class,
                () -> authenticationService.login(validLoginRequest)
        );

        assertEquals("Authentication failed", exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception.getCause());
        verify(userRepository).findByEmail(validLoginRequest.getEmail());
        verifyNoInteractions(passwordEncoder, authenticationManager, jwtService);
    }
}
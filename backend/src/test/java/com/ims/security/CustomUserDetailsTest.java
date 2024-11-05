package com.ims.security;


import com.ims.models.Role;
import com.ims.models.User;
import com.ims.repository.UserRepository;
import com.ims.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testUser", "test@example.com", "encodedPassword", Role.CLIENT);
    }

    @Test
    void loadUserByUsername_WithExistingEmail_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        // Assert
        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_" + user.getRole().name())
        ));

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void loadUserByUsername_WithNonExistingEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String nonExistingEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(nonExistingEmail)
        );

        assertEquals("User not found with email: " + nonExistingEmail, exception.getMessage());
        verify(userRepository).findByEmail(nonExistingEmail);
    }

    @Test
    void loadUserByUsername_WithNullEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(null)
        );

        assertEquals("User not found with email: null", exception.getMessage());
        verify(userRepository).findByEmail(null);
    }
}
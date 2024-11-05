package com.ims.services;

import com.ims.models.Role;
import com.ims.models.User;
import com.ims.models.dtos.request.RegisterUserDto;
import com.ims.models.dtos.request.UpdateUserDto;
import com.ims.models.dtos.response.UserResponseDto;
import com.ims.models.dtos.response.UserUpdateResponseDto;
import com.ims.repository.UserRepository;
import com.ims.security.AuthenticationFacade;
import com.ims.security.CustomUserDetails;
import com.ims.security.UserSecurity;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;

    private RegisterUserDto validRegisterUserDto;
    private User savedUser;
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private UpdateUserDto updatedUserDto;

    @BeforeEach
    void setUp() {

        User existingUser = new User("currentUsername", "current@example.com", "encodedPassword", Role.CLIENT);
        existingUser.setId(2);

        validRegisterUserDto = new RegisterUserDto(
                "testUser",
                "test@example.com",
                "Password123@",
                Role.CLIENT
        );

        savedUser = new User(
                validRegisterUserDto.getUsername(),
                validRegisterUserDto.getEmail().toLowerCase(),
                ENCODED_PASSWORD,
                validRegisterUserDto.getRole()
        );
        // Set an ID to simulate saved user
        savedUser.setId(1);

        updatedUserDto = UpdateUserDto.builder()
                .username("newUsername")
                .email("new@example.com")
                .password("newPassword")
                .profileImage("newImage.jpg")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_WithValidData_ShouldReturnUserResponseDto() {
        // Arrange
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponseDto result = userService.createUser(validRegisterUserDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(validRegisterUserDto.getUsername(), result.getUsername());
        assertEquals(validRegisterUserDto.getEmail().toLowerCase(), result.getEmail());
        assertEquals(validRegisterUserDto.getRole(), result.getRole());

        verify(passwordEncoder).encode(validRegisterUserDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithProfileImage_ShouldSetProfileImage() {
        // Arrange
        validRegisterUserDto.setProfileImage("profile.jpg");
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponseDto result = userService.createUser(validRegisterUserDto);

        // Assert
        verify(userRepository).save(argThat(user ->
                user.getProfileImage().equals("profile.jpg")
        ));
    }

    @Test
    void createUser_WithDataIntegrityViolation_ShouldThrowIllegalStateException() {
        // Arrange
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userService.createUser(validRegisterUserDto)
        );

        assertTrue(exception.getMessage().contains("Could not create user"));
        assertInstanceOf(DataIntegrityViolationException.class, exception.getCause());
    }

    @Test
    void createUser_WithUnexpectedException_ShouldThrowRuntimeException() {
        // Arrange
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.createUser(validRegisterUserDto)
        );

        assertEquals("Failed to create user", exception.getMessage());
    }

    @Test
    void createUser_ShouldLowercaseEmail() {
        // Arrange
        validRegisterUserDto.setEmail("TEST@EXAMPLE.COM");
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponseDto result = userService.createUser(validRegisterUserDto);

        // Assert
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("test@example.com")
        ));
    }

    /*
    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedResponse() {
        // Arrange
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");

        // Act
        UserUpdateResponseDto result = userService.updateUser(existingUser.getId(), updatedUserDto);

        // Assert
        assertNotNull(result);
        assertEquals(updatedUserDto.getUsername(), result.getUsername());
        assertEquals(updatedUserDto.getEmail().toLowerCase(), result.getEmail());
        assertEquals("newEncodedPassword", result.getPassword());
    }

    @Test
    void updateUser_WithDataIntegrityViolation_ShouldThrowIllegalStateException() {
        // Arrange
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userService.updateUser(existingUser.getId(), updatedUserDto)
        );

        assertTrue(exception.getMessage().contains("Could not update user"));
        assertTrue(exception.getCause() instanceof DataIntegrityViolationException);
    }

    @Test
    void updateUser_WithNonExistentUser_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(existingUser.getId(), updatedUserDto)
        );

        assertEquals("User not found with ID: " + existingUser.getId(), exception.getMessage());
    }

    @Test
    void updateUser_UnauthorizedUser_ShouldThrowAccessDeniedException() {
        // Arrange
        when(userSecurity.isCurrentUser(existingUser.getId())).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                userService.updateUser(existingUser.getId(), updatedUserDto));
    }

    @Test
    void deleteUser_WithExistingUser_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(existingUser.getId())).thenReturn(true);

        // Act
        userService.deleteUser(existingUser.getId());

        // Assert
        verify(userRepository).deleteById(existingUser.getId());
    }

    @Test
    void deleteUser_WithNonExistentUser_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(userRepository.existsById(existingUser.getId())).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.deleteUser(existingUser.getId())
        );

        assertEquals("User not found with ID: " + existingUser.getId(), exception.getMessage());
    }

    @Test
    void deleteUser_WithDataIntegrityViolation_ShouldThrowIllegalStateException() {
        // Arrange
        when(userRepository.existsById(existingUser.getId())).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Constraint violation")).when(userRepository).deleteById(existingUser.getId());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userService.deleteUser(existingUser.getId())
        );

        assertTrue(exception.getMessage().contains("Could not delete user due to existing dependencies"));
    }
     */
}
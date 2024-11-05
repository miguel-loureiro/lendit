package com.ims.services;

import com.ims.models.Role;
import com.ims.models.User;
import com.ims.models.dtos.request.RegisterUserDto;
import com.ims.models.dtos.response.UserResponseDto;
import com.ims.repository.UserRepository;
import com.ims.security.AuthenticationFacade;
import com.ims.security.UserSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private UserSecurity userSecurity;

    @InjectMocks
    private UserService userService;

    private RegisterUserDto validRegisterUserDto;
    private User savedUser;
    private static final String ENCODED_PASSWORD = "encodedPassword123";

    @BeforeEach
    void setUp() {
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
}
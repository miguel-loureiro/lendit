package com.ims.services;

import com.ims.models.Role;
import com.ims.models.User;
import com.ims.models.dtos.request.CreateUserDto;
import com.ims.models.dtos.request.UpdateUserDto;
import com.ims.models.dtos.response.UserCreatedDto;
import com.ims.models.dtos.response.UserUpdatedDto;
import com.ims.repository.UserRepository;
import com.ims.security.AuthenticationFacade;
import com.ims.security.UserSecurity;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserSecurity userSecurity;

    private CreateUserDto validCreateUserDto;
    private User savedUser;
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private UpdateUserDto updatedUserDto;

    @BeforeEach
    void setUp() {

        User existingUser = new User("currentUsername", "current@example.com", "encodedPassword", Role.CLIENT);
        existingUser.setId(2);

        validCreateUserDto = new CreateUserDto(
                "testUser",
                "test@example.com",
                "Password123@",
                Role.CLIENT
        );

        savedUser = new User(
                validCreateUserDto.getUsername(),
                validCreateUserDto.getEmail().toLowerCase(),
                ENCODED_PASSWORD,
                validCreateUserDto.getRole()
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



    private void mockCurrentUser(User user) {
        Authentication authentication = mock(Authentication.class);
        // Return User directly instead of CustomUserDetails
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.isAuthenticated()).thenReturn(true);  // Add this line
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    // Updated helper method if you want to reuse it in other tests
    private void setUpSecurityContext(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createUser_WithValidData_ShouldReturnUserResponseDto() {
        // Arrange
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserCreatedDto result = userService.createUser(validCreateUserDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(validCreateUserDto.getUsername(), result.getUsername());
        assertEquals(validCreateUserDto.getEmail().toLowerCase(), result.getEmail());
        assertEquals(validCreateUserDto.getRole(), result.getRole());

        verify(passwordEncoder).encode(validCreateUserDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithProfileImage_ShouldSetProfileImage() {
        // Arrange
        validCreateUserDto.setProfileImage("profile.jpg");
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserCreatedDto result = userService.createUser(validCreateUserDto);

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
                () -> userService.createUser(validCreateUserDto)
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
                () -> userService.createUser(validCreateUserDto)
        );

        assertEquals("Failed to create user", exception.getMessage());
    }

    @Test
    void createUser_ShouldLowercaseEmail() {
        // Arrange
        validCreateUserDto.setEmail("TEST@EXAMPLE.COM");
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserCreatedDto result = userService.createUser(validCreateUserDto);

        // Assert
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("test@example.com")
        ));
    }

    @Test
    void updateUser_WithRegularUserUpdatingOwnProfile_ShouldSucceed() {
        // Arrange
        User regularUser = new User("regular", "regular@example.com", "encodedPassword", Role.CLIENT);
        regularUser.setId(1);

        // Mock authentication facade to return regular user auth
        Authentication regularAuth = new UsernamePasswordAuthenticationToken(
                regularUser,
                regularUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );
        when(authenticationFacade.getAuthentication()).thenReturn(regularAuth);

        // Mock security check for own profile
        when(userSecurity.isCurrentUser(regularUser.getId())).thenReturn(true);

        // Mock repository calls
        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.encode(updatedUserDto.getPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        // Act
        UserUpdatedDto result = userService.updateUser(regularUser.getId(), updatedUserDto);

        // Assert
        assertNotNull(result);
        verify(authenticationFacade).getAuthentication();
        verify(userSecurity).isCurrentUser(regularUser.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WithSuperUser_ShouldUpdateAnyProfile() {
        // Arrange
        // Create super user
        User superUser = new User("superuser", "super@example.com", "encodedPassword", Role.SUPER);
        superUser.setId(1);

        // Create target user to be updated
        User targetUser = new User("targetUser", "target@example.com", "encodedPassword", Role.CLIENT);
        targetUser.setId(2);
        targetUser.setProfileImage("oldImage.jpg");

        // Set up super user authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                superUser,
                superUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER"))
        );
        when(authenticationFacade.getAuthentication()).thenReturn(authentication);

        // No need to verify isCurrentUser for super user
        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(passwordEncoder.encode(updatedUserDto.getPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        // Act
        UserUpdatedDto result = userService.updateUser(targetUser.getId(), updatedUserDto);

        // Assert
        assertNotNull(result);
        assertEquals(targetUser.getId(), result.getId());
        assertEquals(updatedUserDto.getUsername(), result.getUsername());
        assertEquals(updatedUserDto.getEmail().toLowerCase(), result.getEmail());

        // Verify that isCurrentUser was never called for super user
        verify(userSecurity, never()).isCurrentUser(any());
    }

    @Test
    void updateUser_WithRegularUserTryingToUpdateOtherProfile_ShouldThrowException() {
        // Arrange
        User regularUser = new User("regular", "regular@example.com", "encodedPassword", Role.CLIENT);
        regularUser.setId(1);

        User targetUser = new User("target", "target@example.com", "encodedPassword", Role.MANAGER);
        targetUser.setId(2);

        Authentication regularAuth = new UsernamePasswordAuthenticationToken(
                regularUser,
                regularUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );
        when(authenticationFacade.getAuthentication()).thenReturn(regularAuth);
        when(userSecurity.isCurrentUser(targetUser.getId())).thenReturn(false);

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUser(targetUser.getId(), updatedUserDto)
        );

        assertEquals("You are not authorized to update this user", exception.getMessage());
        verify(authenticationFacade).getAuthentication();
        verify(userSecurity).isCurrentUser(targetUser.getId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_SuccessfulDeletion() {
        // Arrange
        Integer userId = 1;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userSecurity.isCurrentUser(userId)).thenReturn(true);
        when(authenticationFacade.getAuthentication())
                .thenReturn(new TestingAuthenticationToken(null, null, "ROLE_CLIENT"));

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_WithNonExistentUser_ShouldThrowEntityNotFoundException() {
        // Arrange
        Integer userId = 1;
        when(userRepository.existsById(userId)).thenReturn(false);
        when(authenticationFacade.getAuthentication())
                .thenReturn(new TestingAuthenticationToken(null, null, "ROLE_MANAGER"));
        when(userSecurity.isCurrentUser(userId)).thenReturn(true);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.deleteUser(userId)
        );

        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_UnauthorizedUser_ShouldThrowAccessDeniedException() {
        when(userSecurity.isCurrentUser(1)).thenReturn(false);
        when(authenticationFacade.getAuthentication()).thenReturn(new TestingAuthenticationToken(null, null, "ROLE_CLIENT"));

        assertThrows(AccessDeniedException.class, () -> userService.deleteUser(1));
    }

    @Test
    void deleteUser_WithDependencies_ShouldThrowIllegalStateException() {
        // Arrange
        Integer userId = 1;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(authenticationFacade.getAuthentication())
                .thenReturn(new TestingAuthenticationToken(null, null, "ROLE_MANAGER"));
        when(userSecurity.isCurrentUser(userId)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class).when(userRepository).deleteById(userId);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userService.deleteUser(userId)
        );

        assertEquals("Could not delete user due to existing dependencies", exception.getMessage());
    }
}
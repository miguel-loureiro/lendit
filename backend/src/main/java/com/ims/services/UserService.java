package com.ims.services;

import com.ims.models.dtos.request.UpdateUserDto;
import com.ims.models.dtos.response.UserResponseDto;
import com.ims.models.dtos.response.UserUpdateResponseDto;
import com.ims.security.AuthenticationFacade;
import com.ims.models.User;
import com.ims.models.dtos.request.RegisterUserDto;
import com.ims.repository.UserRepository;
import com.ims.security.UserSecurity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;
    private final UserSecurity userSecurity;

    @Transactional
    public UserResponseDto createUser(RegisterUserDto registerUserDto) {
        log.info("Starting user registration process for email: {}", registerUserDto.getEmail());
        validateNewUser(registerUserDto);

        try {
            // Create a new User entity from RegisterUserDto
            User newUser = new User(
                    registerUserDto.getUsername(),
                    registerUserDto.getEmail().toLowerCase(),
                    passwordEncoder.encode(registerUserDto.getPassword()),
                    registerUserDto.getRole()
            );

            if (registerUserDto.getProfileImage() != null) {
                newUser.setProfileImage(registerUserDto.getProfileImage());
            }

            // Save the User entity
            User savedUser = userRepository.save(newUser);
            log.info("Successfully created new user with ID: {}", savedUser.getId());

            // Convert and return UserResponseDto
            return mapUserToResponseDto(registerUserDto, savedUser);

        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation while creating user", e);
            throw new IllegalStateException("Could not create user due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Error creating user", e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @Transactional
    public UserUpdateResponseDto updateUser(Integer id, UpdateUserDto updatedUser) {
        log.info("Attempting to update user with ID: {}", id);

        validateUserUpdatePermissions(id);
        User existingUser = getUserById(id);
        validateUserUpdate(updatedUser, existingUser);

        try {
            // Update fields in the existing user
            updateUserFields(existingUser, updatedUser);

            // Save the updated user
            User savedUser = userRepository.save(existingUser);
            log.info("Successfully updated user with ID: {}", savedUser.getId());

            // Convert and return UserUpdateResponseDto
            return mapUserToUpdateResponseDto(updatedUser, savedUser);

        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation while updating user", e);
            throw new IllegalStateException("Could not update user due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Error updating user with ID: {}", id, e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Transactional
    public void deleteUser(Integer id) {
        log.info("Attempting to delete user with ID: {}", id);

        validateUserDeletionPermissions(id);

        try {
            if (!userRepository.existsById(id)) {
                throw new EntityNotFoundException("User not found with ID: " + id);
            }

            userRepository.deleteById(id);
            log.info("Successfully deleted user with ID: {}", id);

        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation while deleting user", e);
            throw new IllegalStateException("Could not delete user due to existing dependencies", e);
        } catch (Exception e) {
            log.error("Error deleting user with ID: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    private static UserResponseDto mapUserToResponseDto(RegisterUserDto registerUserDto, User savedUser) {
        return UserResponseDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .password(registerUserDto.getPassword())
                .role(savedUser.getRole())
                .build();
    }

    private static UserUpdateResponseDto mapUserToUpdateResponseDto(UpdateUserDto updatedUser, User savedUser) {
        return UserUpdateResponseDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .password(updatedUser.getPassword())
                .role(savedUser.getRole())
                .build();
    }
    private void validateNewUser(RegisterUserDto registerUserDto) {
        userRepository.findByEmail(registerUserDto.getEmail()).ifPresent(user -> {
            log.warn("Attempt to register with existing email: {}", registerUserDto.getEmail());
            throw new IllegalArgumentException("Email already exists");
        });

        userRepository.findByUsername(registerUserDto.getUsername()).ifPresent(user -> {
            log.warn("Attempt to register with existing username: {}", registerUserDto.getUsername());
            throw new IllegalArgumentException("Username already exists");
        });
    }

    private void validateUserUpdatePermissions(Integer userId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        boolean isManager = hasSuperRole(authentication);

        if (!isManager && !userSecurity.isCurrentUser(userId)) {
            log.warn("Unauthorized attempt to update user ID: {}", userId);
            throw new AccessDeniedException("You are not authorized to update this user");
        }
    }

    private void validateUserDeletionPermissions(Integer userId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        boolean isManager = hasSuperRole(authentication);

        if (!isManager && !userSecurity.isCurrentUser(userId)) {
            log.warn("Unauthorized attempt to delete user ID: {}", userId);
            throw new AccessDeniedException("You are not authorized to delete this user");
        }
    }

    private void validateUserUpdate(UpdateUserDto updatedUser, User existingUser) {
        if (!updatedUser.getEmail().equals(existingUser.getEmail())) {
            userRepository.findByEmail(updatedUser.getEmail()).ifPresent(user -> {
                throw new IllegalArgumentException("Email already exists");
            });
        }

        if (!updatedUser.getUsername().equals(existingUser.getUsername())) {
            userRepository.findByUsername(updatedUser.getUsername()).ifPresent(user -> {
                throw new IllegalArgumentException("Username already exists");
            });
        }
    }

    private void updateUserFields(User existingUser, UpdateUserDto updatedUser) {
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail().toLowerCase());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        if (updatedUser.getProfileImage() != null) {
            existingUser.setProfileImage(updatedUser.getProfileImage());
        }
    }

    private User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
    }

    private boolean hasSuperRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SUPER"));
    }
}
package com.ims.services;

import com.ims.exceptions.UnauthorizedException;
import com.ims.security.AuthenticationFacade;
import com.ims.security.CustomUserDetails;
import com.ims.models.Role;
import com.ims.models.User;
import com.ims.models.dtos.request.RegisterUserDto;
import com.ims.models.dtos.response.UserDto;
import com.ims.repository.UserRepository;
import com.ims.security.UserSecurity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;
    private final UserSecurity userSecurity;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationFacade authenticationFacade, UserSecurity userSecurity) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationFacade = authenticationFacade;
        this.userSecurity = userSecurity;
    }

    @Transactional
    public User createUser (RegisterUserDto registerUserDto) {
        // Check if the username or email already exists
        if (userRepository.findByEmail(registerUserDto.getEmail())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(registerUserDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        // Create a new User entity from the DTO
        User newUser  = new User();
        newUser .setUsername(registerUserDto.getUsername());
        newUser .setEmail(registerUserDto.getEmail());
        newUser .setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        newUser .setRole(registerUserDto.getRole());

        // Save the new user to the repository
        return userRepository.save(newUser );
    }


    @Transactional
    public User updateUser (Integer id, User updatedUser ) {
        Authentication authentication = authenticationFacade.getAuthentication();

        // Check if the current user has the MANAGER role
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MANAGER"));

        // If not a manager, ensure the user is updating their own profile
        if (!isManager) {
            CustomUserDetails currentUser  = (CustomUserDetails) authentication.getPrincipal();
            if (!userSecurity.isCurrentUser (currentUser.user().getId())) {
                throw new UnauthorizedException("You are not allowed to update this user.");
            }
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User  not found"));

        // Update user fields
        user.setUsername(updatedUser .getUsername());
        user.setEmail(updatedUser .getEmail());
        if (updatedUser .getPassword() != null && !updatedUser .getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser .getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser (Integer id) {
        Authentication authentication = authenticationFacade.getAuthentication();

        // Check if the current user has the MANAGER role
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MANAGER"));

        // If not a manager, ensure the user is deleting their own account
        if (!isManager) {
            CustomUserDetails currentUser  = (CustomUserDetails) authentication.getPrincipal();
            if (!userSecurity.isCurrentUser (currentUser.user().getId())) {
                throw new UnauthorizedException("You are not allowed to delete this user.");
            }
        }

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User  not found");
        }
        userRepository.deleteById(id);
    }
}
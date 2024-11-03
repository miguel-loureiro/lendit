package com.ims.services;

import com.ims.models.CustomUserDetails;
import com.ims.models.Item;
import com.ims.models.Role;
import com.ims.models.User;
import com.ims.models.dtos.RegisterUserDto;
import com.ims.models.dtos.UserDto;
import com.ims.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<Page<UserDto>> getAllUsers(int page, int size) {

        Sort usernameSort = Sort.by("username");
        Pageable paging = PageRequest.of(page, size, usernameSort.ascending());

        Page<User> usersPage = userRepository.findAll(paging);
        Page<UserDto> userDtosPage = usersPage.map(UserDto::new);

        return ResponseEntity.ok(userDtosPage);
    }

    public ResponseEntity<UserDto> createUser(RegisterUserDto input) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == "anonymousUser") {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Object principal = authentication.getPrincipal();

        CustomUserDetails currentUserDetails = (CustomUserDetails) principal;
        User currentUser = currentUserDetails.user();
        Role newUserRole = input.getRole();

        User targetUser = new User();
        targetUser.setUsername(input.getUsername());
        targetUser.setRole(newUserRole);

        if (!hasPermissionToCreateUser(currentUser, targetUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        boolean userExists = userRepository.findByEmail(input.getEmail()).isPresent() ||
                userRepository.findByUsername(input.getUsername()).isPresent();

        if (userExists) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("Error-Message", "User already in the database")
                    .body(null);
        }

        User user = new User(input.getUsername(), input.getEmail(),
                passwordEncoder.encode(input.getPassword()), input.getRole());
        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserDto(savedUser));
    }


    public Optional<User> getUserByIdentifier(String identifier, String Role) {

        switch (Role) {

            case "id":
                try {

                    return Optional.of(userRepository.getReferenceById(Integer.parseInt(identifier)));
                } catch (EntityNotFoundException | NumberFormatException e) {

                    return Optional.empty();
                }
            case "username":

                return userRepository.findByUsername(identifier);
            case "email":

                return userRepository.findByEmail(identifier);
            default:
                throw new IllegalArgumentException("Invalid identifier Role: " + Role);
        }
    }

    private boolean hasPermissionToCreateUser(User currentUser, User targetUser) {

        Role currentUserRole = currentUser.getRole();
        Role targetUserRole = targetUser.getRole();
        boolean isSameUser = currentUser.getUsername().equals(targetUser.getUsername());

        return switch (currentUserRole) {
            case SUPER -> !isSameUser;
            case MANAGER -> isSameUser || targetUserRole == Role.CLIENT;
            default -> false;
        };
    }

    private boolean hasPermissionToDeleteUser(User currentUser, User targetUser) {

        Role currentUserRole = currentUser.getRole();
        Role targetUserRole = targetUser.getRole();
        boolean isSameUser = currentUser.getUsername().equals(targetUser.getUsername());

        return switch (currentUserRole) {
            case SUPER -> !isSameUser;
            case MANAGER -> isSameUser || targetUserRole == Role.CLIENT;
            case CLIENT -> isSameUser;
            default -> false;
        };
    }

    private boolean hasPermissionToUpdateUser(User currentUser, User targetUser) {

        Role currentUserRole = currentUser.getRole();
        Role targetUserRole = targetUser.getRole();
        boolean isSameUser = currentUser.getUsername().equals(targetUser.getUsername());

        return switch (currentUserRole) {
            case SUPER -> !isSameUser;
            case MANAGER-> isSameUser || targetUserRole == Role.CLIENT;
            case CLIENT-> isSameUser;
            default -> false;
        };
    }
}
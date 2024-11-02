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

    public ResponseEntity<Void> deleteUser(String identifier, String Role) throws IOException {

        Optional<User> currentUserOpt = getCurrentUser();

        if (currentUserOpt.isEmpty()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = currentUserOpt.get();

        Optional<User> userToDeleteOpt = getUserByIdentifier(identifier, Role);

        if (userToDeleteOpt.isEmpty()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User userToDelete = userToDeleteOpt.get();

        if (!hasPermissionToDeleteUser(currentUser, userToDelete)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userRepository.delete(userToDelete);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<UserDto> updateUser(String identifier, String Role, UserDto input) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == "anonymousUser") {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Optional<User> currentUserOpt = getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User currentUser = currentUserOpt.get();

        Optional<User> userToUpdateOpt = getUserByIdentifier(identifier, Role);
        if (userToUpdateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User userToUpdate = userToUpdateOpt.get();

        if (!hasPermissionToUpdateUser(currentUser, userToUpdate)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userToUpdate.setUsername(input.getUsername());
        userToUpdate.setEmail(input.getEmail());
        userToUpdate.setRole(input.getRole());
        userToUpdate.setProfileImage(input.getProfileImage());

        if (input.getItems() != null) {
            userToUpdate.setItems(
                    input.getItems().stream()
                            .map(itemDto -> new Item(itemDto.getDesignation(), itemDto.getCategory()))
                            .collect(Collectors.toSet())
            );
        }

        User updatedUser = userRepository.save(userToUpdate);
        UserDto updatedUserDto = new UserDto(updatedUser);

        return ResponseEntity.ok(updatedUserDto);
    }

    public ResponseEntity<Void> changeUserPassword(String username, String newPassword) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == "anonymousUser") {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Optional<User> currentUserOpt = getCurrentUser();

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = currentUserOpt.get();

        if (!currentUser.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);

        return ResponseEntity.ok().build();
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

    public Optional<User> getCurrentUser() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) principal;

            return userRepository.findByIdWithItems(userDetails.user().getId());
        }

        return Optional.empty();
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
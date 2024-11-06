package com.ims.controllers;

import com.ims.exceptions.ForbiddenException;
import com.ims.exceptions.InvalidUserRoleException;
import com.ims.models.Role;
import com.ims.models.dtos.request.CreateUserDto;
import com.ims.models.dtos.request.UpdateUserDto;
import com.ims.models.dtos.response.UserCreatedDto;
import com.ims.models.dtos.response.UserUpdatedDto;
import com.ims.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create a new user
    @PostMapping("/new")
    public ResponseEntity<UserCreatedDto> createUser (@RequestBody @Valid CreateUserDto createUserDto) {
        if(createUserDto.getRole() == Role.SUPER) {
            throw new InvalidUserRoleException("Cannot create SUPER user");
        }
        UserCreatedDto createdUser  = userService.createUser(createUserDto);
        return new ResponseEntity<>(createdUser , HttpStatus.CREATED);
    }

    // Update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<UserUpdatedDto> updateUser (@PathVariable Integer id, @RequestBody @Valid UpdateUserDto updateUserDto) {
        if(id == 1) {
            throw new ForbiddenException("Cannot update SUPER user");
        }
        UserUpdatedDto updatedUser  = userService.updateUser(id, updateUserDto);
        return new ResponseEntity<>(updatedUser , HttpStatus.OK);
    }

    // Delete a user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable Integer id) {
        if(id == 1) {
            throw new ForbiddenException("Cannot delete SUPER user");
        }
        userService.deleteUser (id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

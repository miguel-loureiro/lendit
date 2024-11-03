package com.ims.controllers;

import com.ims.models.User;
import com.ims.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create a new user
    @PostMapping
    public ResponseEntity<User> createUser (@RequestBody User newUser ) {
        // Assuming you have a method in UserService to handle user creation
        User createdUser  = userService.createUser (newUser );
        return new ResponseEntity<>(createdUser , HttpStatus.CREATED);
    }

    // Update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser (@PathVariable Integer id, @RequestBody User updatedUser ) {
        User user = userService.updateUser (id, updatedUser );
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // Delete a user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable Integer id) {
        userService.deleteUser (id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
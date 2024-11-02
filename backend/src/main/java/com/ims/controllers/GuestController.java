package com.ims.controllers;

import com.ims.repository.UserRepository;
import com.ims.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/guest")
@RestController
public class GuestController {

    private final UserService userService;
    private final UserRepository userRepository;

    public GuestController(UserService userService, UserRepository userRepository) {

        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/items")
    public ResponseEntity<String> getPublicProducts() {
        return ResponseEntity.ok("lots and lots of products");
    }
}

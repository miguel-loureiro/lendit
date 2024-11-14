package com.ims.controllers;

import com.ims.models.dtos.request.LoginUserDto;
import com.ims.models.dtos.request.SignupUserDto;
import com.ims.models.dtos.response.LoginResponse;
import com.ims.models.dtos.response.UserSignedDto;
import com.ims.repository.UserRepository;
import com.ims.security.JwtService;
import com.ims.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthenticationController {
    private final AuthenticationService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginUserDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid SignupUserDto request) {
        return ResponseEntity.ok(authService.signup(request));
    }
}
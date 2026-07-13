package com.raj.identity.user.controller;

import com.raj.identity.user.dto.request.CreateUserRequest;
import com.raj.identity.user.dto.response.UserResponse;
import com.raj.identity.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(request));

    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable UUID userId) {

        return userService.getUser(userId);

    }

}
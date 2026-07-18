package com.raj.identity.user.service;

import com.raj.identity.user.dto.request.CreateUserRequest;
import com.raj.identity.user.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse register(CreateUserRequest request);

    UserResponse getUser(UUID userId);

}
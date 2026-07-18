package com.raj.identity.user.mapper;

import com.raj.identity.user.dto.response.UserResponse;
import com.raj.identity.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        return new UserResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getStatus()

        );

    }

}
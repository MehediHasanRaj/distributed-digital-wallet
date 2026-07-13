package com.raj.identity.user.service;

import com.raj.identity.user.dto.request.CreateUserRequest;
import com.raj.identity.user.dto.response.UserResponse;
import com.raj.identity.user.entity.User;
import com.raj.identity.user.exception.UserAlreadyExistsException;
import com.raj.identity.user.exception.UserNotFoundException;
import com.raj.identity.user.mapper.UserMapper;
import com.raj.identity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse register(CreateUserRequest request) {

        String email = normalizeEmail(request.email());

        validateEmailDoesNotExist(email);

        User user = new User();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        User saved = userRepository.save(user);

        log.info("User registered successfully: {}", saved.getUserId());

        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse getUser(UUID userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return userMapper.toResponse(user);
    }

    private void validateEmailDoesNotExist(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

    }

    private String normalizeEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);

    }

}
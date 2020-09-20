package io.github.brenovit.messagingstompwebsocket.user.controller;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.brenovit.messagingstompwebsocket.exception.BadRequestException;
import io.github.brenovit.messagingstompwebsocket.exception.EmailAlreadyExistsException;
import io.github.brenovit.messagingstompwebsocket.exception.UsernameAlreadyExistsException;
import io.github.brenovit.messagingstompwebsocket.user.model.Profile;
import io.github.brenovit.messagingstompwebsocket.user.model.Role;
import io.github.brenovit.messagingstompwebsocket.user.model.User;
import io.github.brenovit.messagingstompwebsocket.user.payload.ApiResponse;
import io.github.brenovit.messagingstompwebsocket.user.payload.JwtAuthenticationResponse;
import io.github.brenovit.messagingstompwebsocket.user.payload.LoginRequest;
import io.github.brenovit.messagingstompwebsocket.user.payload.SignUpRequest;
import io.github.brenovit.messagingstompwebsocket.user.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired private UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String token = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

    @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@Valid @RequestBody SignUpRequest payload) {
        log.info("creating user {}", payload.getUsername());

        User user = User
                .builder()
                .username(payload.getUsername())
                .email(payload.getEmail())
                .password(payload.getPassword())
                .userProfile(Profile
                        .builder()
                        .displayName(payload.getName())
                        .profilePictureUrl(payload.getProfilePicUrl())
                        .build())
                .build();

        try {
            userService.registerUser(user, Role.USER);
        } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e) {
            throw new BadRequestException(e.getMessage());
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(user.getUsername()).toUri();

        return ResponseEntity
                .created(location)
                .body(new ApiResponse(true,"User registered successfully"));
    }
}
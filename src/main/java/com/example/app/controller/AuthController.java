package com.example.app.controller;

import com.example.app.dto.AuthRequest;
import com.example.app.models.User;
import com.example.app.models.enums.UserRole;
import com.example.app.security.JwtUtil;
import com.example.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        String token = JwtUtil.generateToken(user);
        UserRole role = user.getRole();

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", String.valueOf(role));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<Map<String, String>> register(@RequestBody AuthRequest request) {
        User user = userService.register(request.getFirstName(), request.getLastName(), request.getUsername(), request.getPassword(), request.getEmail());
        String token = JwtUtil.generateToken(user);
        UserRole role = user.getRole();

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", String.valueOf(role));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

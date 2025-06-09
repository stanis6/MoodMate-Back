package com.example.app.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
}


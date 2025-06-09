package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ConfirmationResponse {
    private UUID childId;
    private String username;
    private String firstName;
    private String lastName;
}

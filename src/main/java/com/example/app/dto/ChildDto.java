package com.example.app.dto;
import lombok.Data;
import java.util.UUID;

@Data
public class ChildDto {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
}

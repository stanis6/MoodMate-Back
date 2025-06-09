package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class ChildAccountRequest {
    private String parentEmail;
    private String childFirstName;
    private String childLastName;
}
package com.example.app.dto;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ClassroomDto {
    private UUID id;
    private String name;
    private UUID teacherId;
    private List<ChildDto> students;
}

package com.example.app.service;

import com.example.app.dto.ChildDto;
import com.example.app.dto.ClassroomDto;
import com.example.app.models.Classroom;
import com.example.app.models.User;
import com.example.app.models.enums.UserRole;
import com.example.app.repository.ClassroomRepository;
import com.example.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClassroomService {
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    public ClassroomService(ClassroomRepository cr, UserRepository ur) {
        this.classroomRepository = cr;
        this.userRepository = ur;
    }

    public ClassroomDto getClassroomByTeacherId(UUID teacherId) {
        Classroom c = classroomRepository.findByTeacherId(teacherId)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("not found"));

        List<User> children = userRepository.findByClassroomIdAndRoleOrderByLastNameAsc(c.getId(), UserRole.CHILD);

        ClassroomDto dto = new ClassroomDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setTeacherId(c.getTeacherId());

        List<ChildDto> childDtos = children.stream().map(u -> {
            ChildDto cd = new ChildDto();
            cd.setId(u.getId());
            cd.setUsername(u.getUsername());
            cd.setFirstName(u.getFirstName());
            cd.setLastName(u.getLastName());
            return cd;
        }).toList();

        dto.setStudents(childDtos);
        return dto;
    }
}

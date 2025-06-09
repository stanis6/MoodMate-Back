package com.example.app.events;

import com.example.app.dto.ChildDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class ChildCreatedEvent extends ApplicationEvent {
    private final UUID teacherId;
    private final ChildDto child;

    public ChildCreatedEvent(Object source, UUID teacherId, ChildDto child) {
        super(source);
        this.teacherId = teacherId;
        this.child = child;
    }
}

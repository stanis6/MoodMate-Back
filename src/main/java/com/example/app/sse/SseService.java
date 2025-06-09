package com.example.app.sse;

import com.example.app.dto.ChildDto;
import com.example.app.events.ChildCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(UUID teacherId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters
                .computeIfAbsent(teacherId, id -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(teacherId, emitter));
        emitter.onTimeout   (() -> removeEmitter(teacherId, emitter));
        return emitter;
    }

    @EventListener
    public void onChildCreated(ChildCreatedEvent event) {
        UUID teacherId = event.getTeacherId();
        ChildDto child  = event.getChild();

        List<SseEmitter> list = emitters.getOrDefault(teacherId, new CopyOnWriteArrayList<>());
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-child")
                        .data(child));
            } catch (Exception ex) {
                removeEmitter(teacherId, emitter);
            }
        }
    }

    private void removeEmitter(UUID teacherId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(teacherId);
        if (list != null) {
            list.remove(emitter);
        }
    }
}
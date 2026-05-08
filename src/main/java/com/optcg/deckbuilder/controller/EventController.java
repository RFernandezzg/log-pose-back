package com.optcg.deckbuilder.controller;

import com.optcg.deckbuilder.model.dto.event.EventDTO;
import com.optcg.deckbuilder.security.UserDetailsImpl;
import com.optcg.deckbuilder.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@RequestBody EventDTO eventDTO,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(eventService.createEvent(eventDTO, userDetails.getUsername()));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<EventDTO> registerAttendee(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(eventService.registerAttendee(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/unregister")
    public ResponseEntity<EventDTO> unregisterAttendee(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(eventService.unregisterAttendee(id, userDetails.getUsername()));
    }
}

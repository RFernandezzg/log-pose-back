package com.optcg.deckbuilder.service;

import com.optcg.deckbuilder.model.dto.event.EventDTO;
import com.optcg.deckbuilder.model.entity.Event;
import com.optcg.deckbuilder.model.entity.User;
import com.optcg.deckbuilder.repository.EventRepository;
import com.optcg.deckbuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public List<EventDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return convertToDTO(event);
    }

    @Transactional
    public EventDTO createEvent(EventDTO eventDTO, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = Event.builder()
                .name(eventDTO.getName())
                .description(eventDTO.getDescription())
                .dateTime(eventDTO.getDateTime())
                .location(eventDTO.getLocation())
                .creator(creator)
                .build();

        return convertToDTO(eventRepository.save(event));
    }

    @Transactional
    public EventDTO registerAttendee(Long eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (event.getAttendees().contains(user)) {
            throw new RuntimeException("User already registered for this event");
        }

        event.getAttendees().add(user);
        Event savedEvent = eventRepository.save(event);

        // Notify creator
        emailService.sendEventRegistrationNotification(event.getCreator(), user, event);

        return convertToDTO(savedEvent);
    }

    @Transactional
    public EventDTO unregisterAttendee(Long eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!event.getAttendees().contains(user)) {
            throw new RuntimeException("User not registered for this event");
        }

        event.getAttendees().remove(user);
        Event savedEvent = eventRepository.save(event);

        // Check if unregistration is within 1 day of event start
        if (LocalDateTime.now().plusDays(1).isAfter(event.getDateTime())) {
            emailService.sendEventUnregistrationNotification(event.getCreator(), user, event);
        }

        return convertToDTO(savedEvent);
    }

    private EventDTO convertToDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .dateTime(event.getDateTime())
                .location(event.getLocation())
                .creator(event.getCreator() != null ? EventDTO.CreatorDTO.builder()
                        .id(event.getCreator().getId())
                        .username(event.getCreator().getUsername())
                        .avatarUrl(event.getCreator().getAvatarUrl())
                        .build() : null)
                .attendees(event.getAttendees() != null ? event.getAttendees().stream()
                        .map(user -> EventDTO.AttendeeDTO.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .avatarUrl(user.getAvatarUrl())
                                .build())
                        .collect(Collectors.toSet()) : new HashSet<>())
                .attendeeCount(event.getAttendees() != null ? event.getAttendees().size() : 0)
                .build();
    }
}

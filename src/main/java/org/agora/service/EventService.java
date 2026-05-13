package org.agora.service;

import lombok.RequiredArgsConstructor;
import org.agora.dto.EventRequest;
import org.agora.dto.EventResponse;
import org.agora.entity.Event;
import org.agora.entity.User;
import org.agora.repository.EventRepository;
import org.agora.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = Event.builder()
                .eventId(UUID.randomUUID().toString())
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .dateTime(request.dateTime())
                .seatQuota(request.seatQuota())
                .seatsAvailable(request.seatQuota())
                .price(request.price())
                .isActive(true)
                .creator(organizer)
                .createAt(ZonedDateTime.now())
                .build();

        Event savedEvent = eventRepository.save(event);

        return mapToResponse(savedEvent);
    }

    private EventResponse mapToResponse(Event event) {
        return new EventResponse(
                event.getEventId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getDateTime(),
                event.getSeatQuota(),
                event.getSeatsAvailable(),
                event.getPrice(),
                event.getIsActive(),
                event.getCreator().getName()
        );
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public EventResponse getDetailEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new EventResponse(
                event.getEventId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getDateTime(),
                event.getSeatQuota(),
                event.getSeatsAvailable(),
                event.getPrice(),
                event.getIsActive(),
                event.getCreator().getName()
        );
    }

    @Transactional
    public EventResponse updateEvent(String eventId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        validateOrganizer(event);

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setDateTime(request.dateTime());
        event.setSeatQuota(request.seatQuota());

        return mapToResponse(eventRepository.save(event));
    }

    private void validateOrganizer(Event event) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!event.getCreator().getEmail().equals(currentUsername)) {
            throw new RuntimeException("You are not authorized to modify this event");
        }
    }

    @Transactional
    public void deactivateEvent(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        validateOrganizer(event);

        if (!event.getIsActive()) {
            throw new RuntimeException("Event is already inactive");
        }

        event.setIsActive(false);

        eventRepository.save(event);
    }
}
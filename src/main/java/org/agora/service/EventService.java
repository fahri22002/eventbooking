package org.agora.service;

import lombok.RequiredArgsConstructor;
import org.agora.dto.EventRequest;
import org.agora.dto.EventResponse;
import org.agora.entity.Event;
import org.agora.entity.User;
import org.agora.repository.EventRepository;
import org.agora.repository.UserRepository;
import org.agora.exception.ResourceNotFoundException;
import org.agora.exception.ForbiddenAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Service layer responsible for handling event-related business logic.
 * Manages the lifecycle of events, including creation, retrieval, modification, and soft-deletion,
 * while enforcing authorization and data integrity rules.
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new event and associates it with the currently authenticated user.
     * @param request the event details provided by the user.
     * @return the created event mapped to an {@link EventResponse} DTO.
     */
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

    /**
     * Retrieves a paginated list of all active and upcoming events.
     * @param pageable pagination and sorting instructions.
     * @return a paginated result of upcoming events.
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findByIsActiveTrueAndDateTimeAfter(ZonedDateTime.now(), pageable)
                .map(this::mapToResponse);
    }

    /**
     * Retrieves the detailed information of a specific event.
     * @param id the unique identifier of the event.
     * @return the event details.
     * @throws ResourceNotFoundException if the event does not exist.
     */
    @Transactional(readOnly = true)
    public EventResponse getDetailEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

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

    /**
     * Updates an existing event's details. Only the event creator can perform this action.
     * Cannot update inactive or past events.
     * @param eventId the unique identifier of the event to update.
     * @param request the updated event data.
     * @return the updated event response.
     * @throws ForbiddenAccessException if the user is not the creator.
     */
    @Transactional
    public EventResponse updateEvent(String eventId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        validateOrganizer(event);

        if (!event.getIsActive()) {
            throw new RuntimeException("Cannot update an inactive event");
        }

        if (event.getDateTime().isBefore(ZonedDateTime.now())) {
            throw new RuntimeException("Cannot update an event that has already passed");
        }

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setDateTime(request.dateTime());
        event.setSeatQuota(request.seatQuota());

        return mapToResponse(eventRepository.save(event));
    }

    /**
     * Soft-deletes an event by setting its status to inactive.
     * Only the event creator can perform this action.
     * @param eventId the unique identifier of the event to deactivate.
     * @throws ForbiddenAccessException if the user is not the creator.
     */
    @Transactional
    public void deactivateEvent(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        validateOrganizer(event);

        if (!event.getIsActive()) {
            throw new RuntimeException("Event is already inactive");
        }

        event.setIsActive(false);

        eventRepository.save(event);
    }

    /**
     * Retrieves a paginated list of all events created by the currently authenticated user.
     * @param pageable pagination and sorting instructions.
     * @return a paginated result of the user's events.
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getMyEvents(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return eventRepository.findByCreator_Email(email, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Performs a dynamic search and filtering of events based on multiple criteria.
     * @param title       keyword to filter by title.
     * @param location    keyword to filter by location.
     * @param creatorName keyword to filter by organizer name.
     * @param startDate   the beginning of the time range.
     * @param endDate     the end of the time range.
     * @param showPast    flag to include events that have already passed.
     * @param pageable    pagination and sorting instructions.
     * @return a paginated and filtered list of events.
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvents(
            String title, String location, String creatorName,
            ZonedDateTime startDate, ZonedDateTime endDate,
            boolean showPast, Pageable pageable) {

        String safeTitle = (title == null) ? "" : title;
        String safeLocation = (location == null) ? "" : location;
        String safeCreatorName = (creatorName == null) ? "" : creatorName;

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime safeStartDate = (startDate != null) ? startDate : now;
        ZonedDateTime safeEndDate = (endDate != null) ? endDate : now.plusYears(10);

        return eventRepository.searchEvents(
                safeTitle, safeLocation, safeCreatorName, showPast, now,
                safeStartDate,
                safeEndDate,
                pageable
        ).map(this::mapToResponse);
    }

    /**
     * Helper method to validates that the currently authenticated user is the creator of the specified event.
     * @param event the event to check authorization against.
     * @throws ForbiddenAccessException if the user email does not match the creator's email.
     */
    private void validateOrganizer(Event event) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!event.getCreator().getEmail().equals(currentUsername)) {
            throw new ForbiddenAccessException("You are not authorized to modify this event");
        }
    }

    /**
     * Helper method to map an {@link Event} entity to an {@link EventResponse} DTO.
     * @param event the event entity to map.
     * @return the mapped response object.
     */
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
}
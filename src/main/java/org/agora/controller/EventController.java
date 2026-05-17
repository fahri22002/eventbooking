package org.agora.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agora.dto.EventRequest;
import org.agora.dto.EventResponse;
import org.agora.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;

/**
 * REST controller for managing events.
 * Handles event creation, retrieval, advanced searching, updates, and soft-deletion.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Creates a new event.
     * @param request the event details to be created.
     * @return a {@link ResponseEntity} containing the created event and a 201 (Created) status.
     */
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    /**
     * Retrieves a paginated list of upcoming events.
     * @param pageable pagination and sorting information.
     * @return a {@link ResponseEntity} containing a paginated list of events.
     */
    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @PageableDefault(sort = "dateTime", direction = Sort.Direction.ASC, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.getAllEvents(pageable));
    }

    /**
     * Retrieves the full details of a specific event by its ID.
     * @param id the unique identifier of the event.
     * @return a {@link ResponseEntity} containing the event details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getDetail(@PathVariable("id") String id) {
        return ResponseEntity.ok(eventService.getDetailEvent(id));
    }

    /**
     * Updates the details of an existing event.
     * @param id      the unique identifier of the event to update.
     * @param request the updated event details.
     * @return a {@link ResponseEntity} containing the updated event.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable("id") String id,
            @Valid @RequestBody EventRequest request
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    /**
     * Soft-deletes an event by marking it as inactive.
     * @param id the unique identifier of the event to deactivate.
     * @return a {@link ResponseEntity} with a 204 (No Content) status upon successful deactivation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateEvent(@PathVariable("id") String id) {
        eventService.deactivateEvent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a paginated list of events created by the authenticated user.
     * @param pageable pagination and sorting information.
     * @return a {@link ResponseEntity} containing a paginated list of the user's created events.
     */
    @GetMapping("/my-events")
    public ResponseEntity<Page<EventResponse>> getMyEvents(
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.getMyEvents(pageable));
    }

    /**
     * Searches and filters events based on various dynamic query parameters.
     * @param title       optional keyword to filter by event title.
     * @param location    optional keyword to filter by event location.
     * @param creatorName optional keyword to filter by the creator's name.
     * @param startDate   optional start date for filtering the event schedule.
     * @param endDate     optional end date for filtering the event schedule.
     * @param showPast    flag to indicate whether past events should be included in the results.
     * @param pageable    pagination and sorting information.
     * @return a {@link ResponseEntity} containing the filtered and paginated search results.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<EventResponse>> searchEvents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String creatorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(defaultValue = "false") boolean showPast,
            @PageableDefault(sort = "dateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.searchEvents(
                title, location, creatorName, startDate, endDate, showPast, pageable
        ));
    }
}
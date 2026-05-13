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

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // FR-03: Create Event
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    // FR-05: Get All Events with Pagination & Sorting
    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @PageableDefault(sort = "dateTime", direction = Sort.Direction.ASC, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.getAllEvents(pageable));
    }

    // FR-04: Update Event
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable("id") String id,
            @Valid @RequestBody EventRequest request
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    // FR-06: Delete Event
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
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

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @PageableDefault(sort = "dateTime", direction = Sort.Direction.ASC, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.getAllEvents(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getDetail(@PathVariable("id") String id) {
        return ResponseEntity.ok(eventService.getDetailEvent(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable("id") String id,
            @Valid @RequestBody EventRequest request
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateEvent(@PathVariable("id") String id) {
        eventService.deactivateEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-events")
    public ResponseEntity<Page<EventResponse>> getMyEvents(
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.getMyEvents(pageable));
    }

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
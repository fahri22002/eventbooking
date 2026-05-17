package org.agora.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agora.dto.BookingRequest;
import org.agora.dto.BookingResponse;
import org.agora.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing event ticket bookings.
 * Handles the creation, retrieval, and cancellation of user reservations.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Creates a new ticket booking for a specific event.
     * @param request the booking details including event ID and ticket quantity.
     * @return a {@link ResponseEntity} containing the booking confirmation and a 201 (Created) status.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    /**
     * Retrieves a paginated list of all bookings made by the authenticated user.
     * @param pageable the pagination and sorting information.
     * @return a {@link ResponseEntity} containing a paginated list of the user's bookings.
     */
    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getUserBookings(
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.getUserBookings(pageable));
    }

    /**
     * Cancels an existing booking and restores the available seats to the event.
     * @param id the unique identifier of the booking to cancel.
     * @return a {@link ResponseEntity} with a 204 (No Content) status upon successful cancellation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable("id") String id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
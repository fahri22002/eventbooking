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

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getUserBookings(
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.getUserBookings(pageable));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable("id") String id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build(); // Mengembalikan 204 No Content
    }
}
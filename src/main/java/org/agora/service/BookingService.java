package org.agora.service;

import lombok.RequiredArgsConstructor;
import org.agora.dto.BookingRequest;
import org.agora.dto.BookingResponse;
import org.agora.entity.Booking;
import org.agora.entity.Event;
import org.agora.entity.User;
import org.agora.repository.BookingRepository;
import org.agora.repository.EventRepository;
import org.agora.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getIsActive()) {
            throw new RuntimeException("Event is no longer active");
        }

        boolean hasActiveBooking = bookingRepository.existsByUser_EmailAndEvent_EventIdAndStatus(
                currentUserEmail, request.eventId(), "CONFIRMED"
        );

        if (hasActiveBooking) {
            throw new RuntimeException("You already have a confirmed booking for this event.");
        }

        int updatedRows = eventRepository.decreaseSeatQuota(event.getEventId(), request.quantity());

        if (updatedRows == 0) {
            throw new RuntimeException("Booking failed: Not enough seats available or event not found.");
        }

        String refCode;
        boolean isUnique = false;
        // Format: Using TimeStamp
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss");

        do {
            String timestamp = ZonedDateTime.now().format(formatter);
            String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            refCode = "BKG-" + timestamp + "-" + randomPart;

            if (!bookingRepository.existsByBookingReference(refCode)) {
                isUnique = true;
            }
        } while (!isUnique);

        Booking booking = Booking.builder()
                .bookingId(UUID.randomUUID().toString())
                .event(event)
                .user(user)
                .quantity(request.quantity())
                .bookingReference(refCode)
                .status("CONFIRMED")
                .createAt(ZonedDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponse(savedBooking, event);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(Pageable pageable) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return bookingRepository.findByUser_Email(currentUserEmail, pageable)
                .map(booking -> mapToResponse(booking, booking.getEvent()));
    }

    private BookingResponse mapToResponse(Booking booking, Event event) {
        BigDecimal totalHarga = event.getPrice().multiply(BigDecimal.valueOf(booking.getQuantity()));
        return new BookingResponse(
                booking.getBookingId(),
                event.getTitle(),
                booking.getBookingReference(),
                booking.getQuantity(),
                totalHarga,
                booking.getStatus(),
                booking.getCreateAt(),
                event.getDateTime()
        );
    }

    @Transactional
    public void cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!booking.getUser().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You are not authorized to cancel this booking");
        }

        if ("CANCELED".equals(booking.getStatus())) {
            throw new RuntimeException("This booking is already canceled");
        }

        ZonedDateTime eventTime = booking.getEvent().getDateTime();
        ZonedDateTime cancelDeadline = eventTime.minusHours(24);

        if (ZonedDateTime.now().isAfter(cancelDeadline)) {
            throw new RuntimeException("Cancellation is only allowed up to 24 hours before the event starts.");
        }

        booking.setStatus("CANCELED");
        bookingRepository.save(booking);

        eventRepository.increaseSeatQuota(booking.getEvent().getEventId(), booking.getQuantity());
    }
}